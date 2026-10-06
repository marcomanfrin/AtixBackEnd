package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.rapportini.*;
import marcomanfrin.atixbackend.entities.*;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.enums.SignatureSource;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.UserType;
import marcomanfrin.atixbackend.exceptions.ForbiddenException;
import marcomanfrin.atixbackend.exceptions.InvalidWorkflowTransitionException;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.exceptions.ValidationException;
import marcomanfrin.atixbackend.repositories.*;
import marcomanfrin.atixbackend.specifications.RapportinoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class RapportinoService {

    private static final String PNG_DATA_URL_PREFIX = "data:image/png;base64,";
    private static final int MAX_SIGNATURE_BYTES = 512 * 1024;
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};
    private static final String VOID_PREFIX = "[ANNULLATO] ";

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("interventionDate"), Sort.Order.desc("number"));

    private final RapportinoRepository rapportinoRepository;
    private final ChecklistTemplateItemRepository checklistTemplateItemRepository;
    private final RapportinoSignatureRepository signatureRepository;
    private final RapportinoSignatureRequestRepository signatureRequestRepository;
    private final WorkRepository workRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PlantRepository plantRepository;
    private final WorksiteReferenceRepository worksiteReferenceRepository;
    private final RapportinoNumberGenerator numberGenerator;
    private final RapportinoStateMachine stateMachine;
    private final RapportinoPdfService pdfService;
    private final WorkReportRepository workReportRepository;
    private final WorkReportEntryRepository workReportEntryRepository;

    public RapportinoService(RapportinoRepository rapportinoRepository,
                             ChecklistTemplateItemRepository checklistTemplateItemRepository,
                             RapportinoSignatureRepository signatureRepository,
                             RapportinoSignatureRequestRepository signatureRequestRepository,
                             WorkRepository workRepository,
                             UserRepository userRepository,
                             ClientRepository clientRepository,
                             PlantRepository plantRepository,
                             WorksiteReferenceRepository worksiteReferenceRepository,
                             RapportinoNumberGenerator numberGenerator,
                             RapportinoStateMachine stateMachine,
                             RapportinoPdfService pdfService,
                             WorkReportRepository workReportRepository,
                             WorkReportEntryRepository workReportEntryRepository) {
        this.rapportinoRepository = rapportinoRepository;
        this.checklistTemplateItemRepository = checklistTemplateItemRepository;
        this.signatureRepository = signatureRepository;
        this.signatureRequestRepository = signatureRequestRepository;
        this.workRepository = workRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.plantRepository = plantRepository;
        this.worksiteReferenceRepository = worksiteReferenceRepository;
        this.numberGenerator = numberGenerator;
        this.stateMachine = stateMachine;
        this.pdfService = pdfService;
        this.workReportRepository = workReportRepository;
        this.workReportEntryRepository = workReportEntryRepository;
    }

    // ---------- Create ----------

    @Transactional
    public RapportinoDetailResponse create(RapportinoCreateRequest request) {
        User currentUser = currentUser();

        Work work = workRepository.findById(request.workId())
                .orElseThrow(() -> new ValidationException("Work not found with id: " + request.workId()));

        Rapportino rapportino = new Rapportino();
        rapportino.setWork(work);
        rapportino.setStatus(RapportinoStatus.DRAFT);
        rapportino.setInterventionDate(request.interventionDate() != null ? request.interventionDate() : LocalDate.now());
        rapportino.setLocale(request.locale() != null ? request.locale() : "it");
        rapportino.setCreatedBy(currentUser);
        rapportino.setTechnician(request.technicianId() != null ? findUser(request.technicianId()) : currentUser);

        if (request.replacesId() != null) {
            Rapportino replaced = getVisible(request.replacesId(), currentUser);
            if (replaced.getStatus() != RapportinoStatus.VOID) {
                throw new InvalidWorkflowTransitionException("Only a VOID rapportino can be reissued");
            }
            if (!replaced.getWork().getId().equals(work.getId())) {
                throw new ValidationException("A reissued rapportino must belong to the same work order");
            }
            rapportino.setReplaces(replaced);
        }

        // Precompilazione dalla commessa: cliente finale (o cliente Atix), impianto, primo referente
        rapportino.setClient(work.getFinalClient() != null ? work.getFinalClient() : work.getAtixClient());
        rapportino.setPlant(work.getPlant());
        if (!work.getWorksiteReferenceAssignments().isEmpty()) {
            rapportino.setWorksiteReference(work.getWorksiteReferenceAssignments().get(0).getWorksiteReference());
        }
        refreshSnapshot(rapportino);

        // Checklist precompilata con le voci attive del template, tutte non spuntate
        List<RapportinoChecklistAnswer> answers = new ArrayList<>();
        List<ChecklistTemplateItem> template = checklistTemplateItemRepository.findByActiveTrueOrderByPositionAsc();
        for (int i = 0; i < template.size(); i++) {
            ChecklistTemplateItem item = template.get(i);
            answers.add(new RapportinoChecklistAnswer(item, item.labelFor(rapportino.getLocale()), false, null, i));
        }
        rapportino.replaceChecklistAnswers(answers);

        rapportino.setNumber(numberGenerator.nextNumber());

        return toDetail(rapportinoRepository.save(rapportino));
    }

    // ---------- Update ----------

    @Transactional
    public RapportinoDetailResponse update(UUID id, RapportinoUpdateRequest request) {
        releaseExpiredSignatureRequests();
        Rapportino rapportino = getVisible(id, currentUser());
        assertEditable(rapportino);

        if (request.interventionDate() != null) rapportino.setInterventionDate(request.interventionDate());
        if (request.technicianId() != null) rapportino.setTechnician(findUser(request.technicianId()));

        if (request.clientId() != null) {
            rapportino.setClient(clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new NotFoundException("Client not found with id: " + request.clientId())));
        }
        if (request.plantId() != null) {
            rapportino.setPlant(plantRepository.findById(request.plantId())
                    .orElseThrow(() -> new NotFoundException("Plant not found with id: " + request.plantId())));
        }
        if (request.worksiteReferenceId() != null) {
            rapportino.setWorksiteReference(worksiteReferenceRepository.findById(request.worksiteReferenceId())
                    .orElseThrow(() -> new NotFoundException("Worksite reference not found with id: " + request.worksiteReferenceId())));
        }

        if (request.typeMaintenance() != null) rapportino.setTypeMaintenance(request.typeMaintenance());
        if (request.typeCallOut() != null) rapportino.setTypeCallOut(request.typeCallOut());
        if (request.typeQuote() != null) rapportino.setTypeQuote(request.typeQuote());
        if (request.typeWarranty() != null) rapportino.setTypeWarranty(request.typeWarranty());
        if (request.description() != null) rapportino.setDescription(request.description());
        if (request.workHours() != null) rapportino.setWorkHours(request.workHours());
        if (request.travelHours() != null) rapportino.setTravelHours(request.travelHours());
        if (request.travelKm() != null) rapportino.setTravelKm(request.travelKm());
        if (request.meal() != null) rapportino.setMeal(request.meal());
        if (request.parking() != null) rapportino.setParking(request.parking());
        if (request.workState() != null) rapportino.setWorkState(request.workState());
        if (request.locale() != null) rapportino.setLocale(request.locale());

        if (request.checklistAnswers() != null) {
            rapportino.replaceChecklistAnswers(buildAnswers(request.checklistAnswers(), rapportino.getLocale()));
        }
        if (request.materials() != null) {
            List<RapportinoMaterial> materials = new ArrayList<>();
            for (int i = 0; i < request.materials().size(); i++) {
                MaterialRequest m = request.materials().get(i);
                materials.add(new RapportinoMaterial(m.description().trim(), m.quantity(), i));
            }
            rapportino.replaceMaterials(materials);
        }

        refreshSnapshot(rapportino);
        return toDetail(rapportinoRepository.save(rapportino));
    }

    // ---------- Delete ----------

    @Transactional
    public void delete(UUID id) {
        releaseExpiredSignatureRequests();
        Rapportino rapportino = getVisible(id, currentUser());
        if (rapportino.getStatus() != RapportinoStatus.DRAFT) {
            throw new InvalidWorkflowTransitionException("Only a DRAFT rapportino can be deleted (current status: " + rapportino.getStatus() + ")");
        }
        // Le richieste di firma scadute/revocate referenziano il rapportino
        signatureRequestRepository.deleteAll(signatureRequestRepository.findByRapportinoId(rapportino.getId()));
        rapportinoRepository.delete(rapportino);
    }

    // ---------- Void ----------

    // Autorizzazione (ADMIN/OWNER/ADMINISTRATION) applicata nel controller
    @Transactional
    public RapportinoDetailResponse voidRapportino(UUID id) {
        User currentUser = currentUser();
        Rapportino rapportino = getVisible(id, currentUser);
        stateMachine.validateTransition(rapportino.getStatus(), RapportinoStatus.VOID);

        rapportino.setStatus(RapportinoStatus.VOID);
        rapportino.setVoidedAt(LocalDateTime.now());
        rapportino.setVoidedBy(currentUser);
        reverseHoursProjection(rapportino);

        return toDetail(rapportinoRepository.save(rapportino));
    }

    // ---------- Signature (on-site) ----------

    @Transactional
    public RapportinoDetailResponse signOnSite(UUID id, RapportinoSignRequest request) {
        releaseExpiredSignatureRequests();
        Rapportino rapportino = getVisible(id, currentUser());
        if (rapportino.getStatus() != RapportinoStatus.DRAFT) {
            // AWAITING_SIGNATURE si firma solo tramite il link remoto (o revocandolo prima)
            throw new InvalidWorkflowTransitionException("Rapportino " + rapportino.getNumber()
                    + " cannot be signed on site in status " + rapportino.getStatus());
        }
        applySignature(rapportino, request, SignatureSource.LOCAL, null, null);
        return toDetail(rapportino);
    }

    /**
     * Transizione di firma condivisa da firma sul posto e remota: firma, ore nel WorkReport e PDF
     * nella stessa transazione. Se il PDF fallisce la transazione fa rollback e il rapportino non risulta firmato.
     */
    @Transactional
    public void applySignature(Rapportino rapportino, RapportinoSignRequest request,
                               SignatureSource source, String signerIp, String signerUserAgent) {
        validateSignRequest(request);
        stateMachine.validateTransition(rapportino.getStatus(), RapportinoStatus.SIGNED);
        if (signatureRepository.existsByRapportinoId(rapportino.getId())) {
            throw new InvalidWorkflowTransitionException("Rapportino " + rapportino.getNumber() + " is already signed");
        }

        LocalDateTime now = LocalDateTime.now();
        rapportino.setSignerName(request.signerName().trim());
        rapportino.setSignedAt(now);
        rapportino.setPrivacyAcceptedAt(now);
        rapportino.setSignatureSource(source);
        rapportino.setStatus(RapportinoStatus.SIGNED);
        rapportinoRepository.save(rapportino);

        signatureRepository.save(new RapportinoSignature(rapportino, request.signatureImage(), now, signerIp, signerUserAgent));

        projectHours(rapportino);
        pdfService.generateAndArchive(rapportino, request.signatureImage());
        rapportinoRepository.save(rapportino);
    }

    @Transactional(readOnly = true)
    public PdfDocument getDocument(UUID id) {
        Rapportino rapportino = getVisible(id, currentUser());
        return new PdfDocument(rapportino.getNumber() + ".pdf", pdfService.loadDocument(rapportino));
    }

    public record PdfDocument(String filename, byte[] content) {}

    // ---------- Read ----------

    @Transactional
    public RapportinoDetailResponse getById(UUID id) {
        releaseExpiredSignatureRequests();
        return toDetail(getVisible(id, currentUser()));
    }

    @Transactional
    public Page<RapportinoListItemResponse> list(UUID workId, UUID technicianId, RapportinoStatus status,
                                                 LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        User currentUser = currentUser();
        releaseExpiredSignatureRequests();

        Specification<Rapportino> spec = Specification.allOf(
                RapportinoSpecification.hasWork(workId),
                RapportinoSpecification.hasTechnician(technicianId),
                RapportinoSpecification.hasStatus(status),
                RapportinoSpecification.interventionDateFrom(dateFrom),
                RapportinoSpecification.interventionDateTo(dateTo),
                RapportinoSpecification.visibleTo(seesEverything(currentUser) ? null : currentUser.getId())
        );

        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        return rapportinoRepository.findAll(spec, sorted).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public List<ChecklistTemplateItemResponse> getChecklistTemplate() {
        return checklistTemplateItemRepository.findByActiveTrueOrderByPositionAsc().stream()
                .map(i -> new ChecklistTemplateItemResponse(i.getId(), i.getCode(), i.getLabelIt(), i.getLabelEn(), i.getPosition()))
                .toList();
    }

    /**
     * Scadenza "lazy" dei link di firma: i rapportini AWAITING_SIGNATURE senza richieste ancora valide
     * tornano DRAFT (e modificabili). Chiamato prima di ogni lettura/modifica, niente job schedulato.
     */
    @Transactional
    public void releaseExpiredSignatureRequests() {
        for (Rapportino rapportino : rapportinoRepository.findAwaitingWithoutUsableRequest(LocalDateTime.now())) {
            stateMachine.validateTransition(rapportino.getStatus(), RapportinoStatus.DRAFT);
            rapportino.setStatus(RapportinoStatus.DRAFT);
            rapportinoRepository.save(rapportino);
        }
    }

    // ---------- Access helpers (usati anche da firma e PDF) ----------

    // ADMIN, OWNER e utenti ADMINISTRATION vedono tutto; gli altri solo i rapportini di cui sono tecnico o autore
    public static boolean seesEverything(User user) {
        return user.getRole() == UserRole.ADMIN
                || user.getRole() == UserRole.OWNER
                || user.getUserType() == UserType.ADMINISTRATION;
    }

    public Rapportino getVisible(UUID id, User user) {
        Rapportino rapportino = rapportinoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rapportino not found with id: " + id));
        if (!seesEverything(user) && !isOwnedBy(rapportino, user)) {
            throw new ForbiddenException("You are not allowed to access this rapportino");
        }
        return rapportino;
    }

    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    // ---------- Internals ----------

    // Una riga di WorkReportEntry per rapportino (idempotente su rapportino_id): ore lavoro + ore viaggio
    private void projectHours(Rapportino rapportino) {
        if (workReportEntryRepository.findByRapportinoId(rapportino.getId()).isPresent()) {
            return;
        }
        Work work = rapportino.getWork();
        WorkReport workReport = workReportRepository.findByWork(work)
                .orElseGet(() -> {
                    WorkReport newReport = new WorkReport();
                    newReport.setWork(work);
                    return workReportRepository.save(newReport);
                });

        WorkReportEntry entry = new WorkReportEntry(
                workReport,
                entryDescription(rapportino),
                rapportino.getTotalHours(),
                rapportino.getInterventionDate(),
                rapportino.getTechnician()
        );
        entry.setRapportino(rapportino);
        workReport.addEntry(entry);
        workReportEntryRepository.save(entry);
        recomputeTotalHours(workReport);
    }

    // Annullamento: la riga resta per tracciabilita' ma con 0 ore, cosi' non conta piu' nel totale
    private void reverseHoursProjection(Rapportino rapportino) {
        workReportEntryRepository.findByRapportinoId(rapportino.getId()).ifPresent(entry -> {
            entry.setHours(BigDecimal.ZERO);
            if (!entry.getDescription().startsWith(VOID_PREFIX)) {
                entry.setDescription(truncate(VOID_PREFIX + entry.getDescription(), 1000));
            }
            workReportEntryRepository.save(entry);
            recomputeTotalHours(entry.getReport());
        });
    }

    private void recomputeTotalHours(WorkReport workReport) {
        BigDecimal total = workReport.getEntries().stream()
                .map(WorkReportEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        workReport.setTotalHours(total);
        workReportRepository.save(workReport);
    }

    private static String entryDescription(Rapportino rapportino) {
        String description = rapportino.getDescription() == null || rapportino.getDescription().isBlank()
                ? "Rapportino " + rapportino.getNumber()
                : "Rapportino " + rapportino.getNumber() + " - " + rapportino.getDescription().trim();
        return truncate(description, 1000);
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    // La validazione dei campi base e' nei vincoli del DTO; qui il contenuto dell'immagine
    static void validateSignRequest(RapportinoSignRequest request) {
        List<String> errors = new ArrayList<>();
        if (request.signerName() == null || request.signerName().isBlank()) {
            errors.add("signerName: Signer name is required");
        }
        if (!request.privacyAccepted()) {
            errors.add("privacyAccepted: Privacy consent is required");
        }
        String image = request.signatureImage();
        if (image == null || !image.startsWith(PNG_DATA_URL_PREFIX)) {
            errors.add("signatureImage: must be a PNG data URL");
        } else {
            try {
                byte[] bytes = Base64.getDecoder().decode(image.substring(PNG_DATA_URL_PREFIX.length()));
                if (bytes.length > MAX_SIGNATURE_BYTES) {
                    errors.add("signatureImage: image is too large");
                } else if (bytes.length < PNG_MAGIC.length
                        || !java.util.Arrays.equals(java.util.Arrays.copyOf(bytes, PNG_MAGIC.length), PNG_MAGIC)) {
                    errors.add("signatureImage: not a valid PNG image");
                }
            } catch (IllegalArgumentException e) {
                errors.add("signatureImage: invalid base64 content");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static boolean isOwnedBy(Rapportino rapportino, User user) {
        return (rapportino.getTechnician() != null && rapportino.getTechnician().getId().equals(user.getId()))
                || (rapportino.getCreatedBy() != null && rapportino.getCreatedBy().getId().equals(user.getId()));
    }

    private void assertEditable(Rapportino rapportino) {
        if (rapportino.getStatus() != RapportinoStatus.DRAFT) {
            throw new InvalidWorkflowTransitionException("Rapportino " + rapportino.getNumber()
                    + " cannot be modified in status " + rapportino.getStatus());
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    // Snapshot ricalcolato dalle entita' scelte; dopo la firma non viene piu' chiamato (DRAFT-only)
    private void refreshSnapshot(Rapportino rapportino) {
        rapportino.setClientName(rapportino.getClient() != null ? rapportino.getClient().getName() : null);
        rapportino.setPlantLabel(rapportino.getPlant() != null ? rapportino.getPlant().getName() : null);
        rapportino.setClientReference(rapportino.getWorksiteReference() != null ? rapportino.getWorksiteReference().getName() : null);
        rapportino.setOrderNumber(rapportino.getWork().getOrderNumber());
    }

    private List<RapportinoChecklistAnswer> buildAnswers(List<ChecklistAnswerRequest> requests, String locale) {
        List<RapportinoChecklistAnswer> answers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ChecklistAnswerRequest r = requests.get(i);
            ChecklistTemplateItem item = null;
            String label;
            if (r.templateItemId() != null) {
                item = checklistTemplateItemRepository.findById(r.templateItemId()).orElse(null);
                if (item == null) {
                    errors.add("checklistAnswers[" + i + "]: unknown template item " + r.templateItemId());
                    continue;
                }
                label = item.labelFor(locale);
            } else {
                if (r.label() == null || r.label().isBlank()) {
                    errors.add("checklistAnswers[" + i + "]: label is required for a custom item");
                    continue;
                }
                label = r.label().trim();
            }
            answers.add(new RapportinoChecklistAnswer(item, label, r.checked(), r.note(), i));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return answers;
    }

    private RapportinoListItemResponse toListItem(Rapportino r) {
        return new RapportinoListItemResponse(
                r.getId(),
                r.getNumber(),
                r.getStatus(),
                r.getInterventionDate(),
                r.getWork().getId(),
                r.getWork().getName(),
                r.getOrderNumber(),
                r.getClientName(),
                r.getPlantLabel(),
                r.getTechnician() != null ? r.getTechnician().getId() : null,
                fullName(r.getTechnician()),
                r.getTotalHours(),
                r.getSignedAt(),
                r.getPdfAttachmentId() != null
        );
    }

    RapportinoDetailResponse toDetail(Rapportino r) {
        SignatureResponse signature = null;
        if (r.getSignedAt() != null) {
            String image = signatureRepository.findByRapportinoId(r.getId())
                    .map(RapportinoSignature::getImageData)
                    .orElse(null);
            signature = new SignatureResponse(image, r.getSignerName(), r.getSignedAt(), r.getPrivacyAcceptedAt(), r.getSignatureSource());
        }

        LocalDateTime requestExpiresAt = null;
        if (r.getStatus() == RapportinoStatus.AWAITING_SIGNATURE) {
            requestExpiresAt = signatureRequestRepository.findByRapportinoIdAndUsedAtIsNullAndRevokedAtIsNull(r.getId()).stream()
                    .map(RapportinoSignatureRequest::getExpiresAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }

        return new RapportinoDetailResponse(
                r.getId(),
                r.getNumber(),
                r.getStatus(),
                r.getInterventionDate(),
                r.getLocale(),
                r.getWork().getId(),
                r.getWork().getName(),
                r.getTechnician() != null ? r.getTechnician().getId() : null,
                fullName(r.getTechnician()),
                r.getClient() != null ? r.getClient().getId() : null,
                r.getPlant() != null ? r.getPlant().getId() : null,
                r.getWorksiteReference() != null ? r.getWorksiteReference().getId() : null,
                r.getClientName(),
                r.getClientReference(),
                r.getPlantLabel(),
                r.getOrderNumber(),
                r.isTypeMaintenance(),
                r.isTypeCallOut(),
                r.isTypeQuote(),
                r.isTypeWarranty(),
                r.getDescription(),
                r.getWorkHours(),
                r.getTravelHours(),
                r.getTotalHours(),
                r.getTravelKm(),
                r.isMeal(),
                r.isParking(),
                r.getWorkState(),
                r.getChecklistAnswers().stream()
                        .map(a -> new ChecklistAnswerResponse(a.getId(),
                                a.getTemplateItem() != null ? a.getTemplateItem().getId() : null,
                                a.getLabelSnapshot(), a.isChecked(), a.getNote(), a.getPosition()))
                        .toList(),
                r.getMaterials().stream()
                        .map(m -> new MaterialResponse(m.getId(), m.getDescription(), m.getQuantity(), m.getPosition()))
                        .toList(),
                signature,
                r.getPdfAttachmentId() != null,
                r.getPdfHash(),
                requestExpiresAt,
                r.getVoidedAt(),
                fullName(r.getVoidedBy()),
                r.getReplaces() != null ? r.getReplaces().getId() : null,
                r.getReplaces() != null ? r.getReplaces().getNumber() : null,
                r.getCreatedBy() != null ? r.getCreatedBy().getId() : null,
                fullName(r.getCreatedBy()),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    private static String fullName(User user) {
        return user != null ? user.getFirstName() + " " + user.getLastName() : null;
    }
}
