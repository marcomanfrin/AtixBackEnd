package marcomanfrin.atixbackend.services;

import jakarta.persistence.EntityManager;
import marcomanfrin.atixbackend.DTO.rapportini.*;
import marcomanfrin.atixbackend.entities.ChecklistTemplateItem;
import marcomanfrin.atixbackend.entities.Rapportino;
import marcomanfrin.atixbackend.entities.Work;
import marcomanfrin.atixbackend.entities.users.AdministrativeUser;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.exceptions.ForbiddenException;
import marcomanfrin.atixbackend.exceptions.InvalidWorkflowTransitionException;
import marcomanfrin.atixbackend.exceptions.ValidationException;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportEntryUpdateRequest;
import marcomanfrin.atixbackend.entities.WorkReport;
import marcomanfrin.atixbackend.entities.WorkReportEntry;
import marcomanfrin.atixbackend.repositories.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RapportinoService.class, RapportinoNumberGenerator.class, RapportinoStateMachine.class,
        RapportinoPdfService.class, WorkReportService.class, RapportinoSignatureService.class,
        RapportinoServiceIntegrationTest.StorageConfig.class})
class RapportinoServiceIntegrationTest {

    @TestConfiguration
    static class StorageConfig {
        @Bean
        InMemoryAttachmentService attachmentService(AttachmentRepository attachments, AttachmentLinkRepository links) {
            return new InMemoryAttachmentService(attachments, links);
        }
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", RapportinoTestSupport.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", RapportinoTestSupport.POSTGRES::getUsername);
        registry.add("spring.datasource.password", RapportinoTestSupport.POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @Autowired RapportinoService service;
    @Autowired RapportinoNumberGenerator numberGenerator;
    @Autowired RapportinoRepository rapportinoRepository;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired InMemoryAttachmentService storage;
    @Autowired WorkReportService workReportService;
    @Autowired WorkReportRepository workReportRepository;
    @Autowired WorkReportEntryRepository workReportEntryRepository;
    @Autowired RapportinoSignatureService signatureService;
    @Autowired RapportinoSignatureRequestRepository signatureRequestRepository;

    TechnicianUser mario;
    TechnicianUser luigi;
    AdministrativeUser admin;
    Work work;

    @BeforeEach
    void setUp() {
        // I test con propagation NOT_SUPPORTED non hanno transazione: le fixture servono solo agli altri
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        mario = RapportinoTestSupport.technician(em, "Mario");
        luigi = RapportinoTestSupport.technician(em, "Luigi");
        admin = RapportinoTestSupport.admin(em, "Anna");
        work = RapportinoTestSupport.work(em);
        em.persist(new ChecklistTemplateItem("T_" + UUID.randomUUID(), "Verifica impianto", "Plant check", 1));
        em.flush();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RapportinoDetailResponse createAs(TechnicianUser user) {
        RapportinoTestSupport.loginAs(user);
        return service.create(new RapportinoCreateRequest(work.getId(), null, null, null, null));
    }

    // ---------- create ----------

    @Test
    void createIsDraftNumberedAndSnapshotsWorkData() {
        RapportinoDetailResponse r = createAs(mario);

        assertEquals(RapportinoStatus.DRAFT, r.status());
        assertTrue(r.number().matches("RFL-\\d{4}-\\d{4,}"), r.number());
        assertEquals(work.getId(), r.workId());
        assertEquals(mario.getId(), r.technicianId());
        assertEquals("Provincia di Bolzano", r.clientName());   // cliente finale preferito all'Atix
        assertEquals("Palazzo di Giustizia", r.plantLabel());
        assertEquals("Mario Rossi", r.clientReference());
        assertEquals(work.getOrderNumber(), r.orderNumber());
        assertFalse(r.checklistAnswers().isEmpty(), "checklist prefilled from the active template");
    }

    @Test
    void secondRapportinoOnSameWorkGetsDistinctNumber() {
        RapportinoDetailResponse first = createAs(mario);
        RapportinoDetailResponse second = createAs(mario);
        assertNotEquals(first.number(), second.number());
    }

    @Test
    void createWithUnknownWorkFails() {
        RapportinoTestSupport.loginAs(mario);
        assertThrows(RuntimeException.class,
                () -> service.create(new RapportinoCreateRequest(UUID.randomUUID(), null, null, null, null)));
    }

    // ---------- update / DRAFT-only ----------

    @Test
    void updateReplacesMaterialsAndChecklistInOrder() {
        RapportinoDetailResponse r = createAs(mario);

        RapportinoDetailResponse updated = service.update(r.id(), updateWith(
                List.of(new ChecklistAnswerRequest(null, "Voce libera", true, "ok")),
                List.of(new MaterialRequest("Cavo", new BigDecimal("2")), new MaterialRequest("Relè", BigDecimal.ONE))));

        assertEquals(List.of("Cavo", "Relè"), updated.materials().stream().map(MaterialResponse::description).toList());
        assertEquals(1, updated.checklistAnswers().size());
        assertEquals("Voce libera", updated.checklistAnswers().get(0).label());

        RapportinoDetailResponse removed = service.update(r.id(), updateWith(null,
                List.of(new MaterialRequest("Relè", BigDecimal.ONE))));
        assertEquals(List.of("Relè"), removed.materials().stream().map(MaterialResponse::description).toList());
    }

    @Test
    void customChecklistItemWithoutLabelIsRejected() {
        RapportinoDetailResponse r = createAs(mario);
        assertThrows(ValidationException.class, () -> service.update(r.id(),
                updateWith(List.of(new ChecklistAnswerRequest(null, " ", true, null)), null)));
    }

    @Test
    void updateIsRejectedUnlessDraft() {
        RapportinoDetailResponse r = createAs(mario);
        // AWAITING_SIGNATURE e' coperto da signatureRequestStoresOnlyTheHashAndFreezesContent (serve una richiesta vera)
        for (RapportinoStatus status : List.of(RapportinoStatus.SIGNED, RapportinoStatus.VOID)) {
            forceStatus(r.id(), status);
            assertThrows(InvalidWorkflowTransitionException.class,
                    () -> service.update(r.id(), updateWith(null, null)), "update in " + status);
        }
    }

    @Test
    void deleteOnlyInDraft() {
        RapportinoDetailResponse signed = createAs(mario);
        forceStatus(signed.id(), RapportinoStatus.SIGNED);
        assertThrows(InvalidWorkflowTransitionException.class, () -> service.delete(signed.id()));

        RapportinoDetailResponse draft = createAs(mario);
        service.update(draft.id(), updateWith(null, List.of(new MaterialRequest("Cavo", BigDecimal.ONE))));
        service.delete(draft.id());
        em.flush();
        assertFalse(rapportinoRepository.existsById(draft.id()));
    }

    // ---------- role scoping ----------

    @Test
    void technicianSeesOnlyOwnRapportini() {
        RapportinoDetailResponse marios = createAs(mario);
        RapportinoDetailResponse luigis = createAs(luigi);

        RapportinoTestSupport.loginAs(mario);
        Page<RapportinoListItemResponse> page = service.list(work.getId(), null, null, null, null, PageRequest.of(0, 50));
        Set<UUID> ids = new HashSet<>(page.map(RapportinoListItemResponse::id).getContent());
        assertTrue(ids.contains(marios.id()));
        assertFalse(ids.contains(luigis.id()));

        assertThrows(ForbiddenException.class, () -> service.getById(luigis.id()));
        assertThrows(ForbiddenException.class, () -> service.update(luigis.id(), updateWith(null, null)));
        assertThrows(ForbiddenException.class, () -> service.delete(luigis.id()));
    }

    @Test
    void creatorKeepsVisibilityWhenTechnicianIsSomeoneElse() {
        RapportinoTestSupport.loginAs(mario);
        RapportinoDetailResponse r = service.create(new RapportinoCreateRequest(work.getId(), null, luigi.getId(), null, null));
        assertEquals(luigi.getId(), r.technicianId());
        assertDoesNotThrow(() -> service.getById(r.id()));
    }

    @Test
    void administrativeUserSeesEverything() {
        RapportinoDetailResponse marios = createAs(mario);
        RapportinoDetailResponse luigis = createAs(luigi);

        RapportinoTestSupport.loginAs(admin);
        Set<UUID> ids = new HashSet<>(service.list(work.getId(), null, null, null, null, PageRequest.of(0, 50))
                .map(RapportinoListItemResponse::id).getContent());
        assertTrue(ids.containsAll(Set.of(marios.id(), luigis.id())));
        assertDoesNotThrow(() -> service.getById(luigis.id()));
    }

    @Test
    void listFiltersByStatus() {
        RapportinoDetailResponse draft = createAs(mario);
        RapportinoDetailResponse signed = createAs(mario);
        forceStatus(signed.id(), RapportinoStatus.SIGNED);

        Set<UUID> ids = new HashSet<>(service.list(work.getId(), null, RapportinoStatus.SIGNED, null, null, PageRequest.of(0, 50))
                .map(RapportinoListItemResponse::id).getContent());
        assertEquals(Set.of(signed.id()), ids);
        assertFalse(ids.contains(draft.id()));
    }

    // ---------- concurrent numbering ----------

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentNumberingNeverCollides() throws Exception {
        int threads = 12;
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return tx.execute(status -> numberGenerator.nextNumber());
            }));
        }
        start.countDown();

        List<String> numbers = new ArrayList<>();
        for (Future<String> f : futures) {
            numbers.add(f.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertEquals(threads, new HashSet<>(numbers).size(), "duplicate numbers: " + numbers);
        List<Integer> seq = numbers.stream().map(n -> Integer.parseInt(n.substring(n.lastIndexOf('-') + 1))).sorted().toList();
        for (int i = 1; i < seq.size(); i++) {
            assertEquals(seq.get(i - 1) + 1, seq.get(i), "sequence has gaps: " + seq);
        }
    }

    // ---------- signature, hours projection, void ----------

    private RapportinoSignRequest validSignature() throws Exception {
        return new RapportinoSignRequest("Giulia Bianchi", RapportinoPdfServiceTest.signaturePng(), true);
    }

    private BigDecimal workTotalHours() {
        return workReportRepository.findByWork(work).map(WorkReport::getTotalHours).orElse(BigDecimal.ZERO);
    }

    private RapportinoDetailResponse draftWithHours(String work, String travel) {
        RapportinoDetailResponse r = createAs(mario);
        return service.update(r.id(), new RapportinoUpdateRequest(null, null, null, null, null,
                null, null, null, null, "Intervento", new BigDecimal(work), new BigDecimal(travel), BigDecimal.TEN,
                null, null, null, null, null, null));
    }

    @Test
    void signingProjectsWorkPlusTravelHoursAndArchivesPdf() throws Exception {
        BigDecimal before = workTotalHours();
        RapportinoDetailResponse r = draftWithHours("4", "2");

        RapportinoDetailResponse signed = service.signOnSite(r.id(), validSignature());
        em.flush();

        assertEquals(RapportinoStatus.SIGNED, signed.status());
        assertEquals("Giulia Bianchi", signed.signature().signerName());
        assertEquals(marcomanfrin.atixbackend.enums.SignatureSource.LOCAL, signed.signature().source());
        assertNotNull(signed.signature().imageData());
        assertTrue(signed.pdfAvailable());
        assertEquals(64, signed.pdfHash().length());
        assertEquals(0, before.add(new BigDecimal("6")).compareTo(workTotalHours()));

        WorkReportEntry entry = workReportEntryRepository.findByRapportinoId(r.id()).orElseThrow();
        assertEquals(0, new BigDecimal("6").compareTo(entry.getHours()));
        assertEquals(mario.getId(), entry.getCreatedBy().getId());

        byte[] pdf = service.getDocument(r.id()).content();
        assertEquals(signed.pdfHash(), RapportinoPdfService.sha256(pdf));
    }

    @Test
    void firstSignatureCreatesTheWorkReportWhenMissing() throws Exception {
        assertTrue(workReportRepository.findByWork(work).isEmpty());
        RapportinoDetailResponse r = draftWithHours("3", "0");
        service.signOnSite(r.id(), validSignature());
        em.flush();
        assertEquals(0, new BigDecimal("3").compareTo(workTotalHours()));
    }

    @Test
    void signatureValidation() throws Exception {
        RapportinoDetailResponse r = createAs(mario);
        String png = RapportinoPdfServiceTest.signaturePng();
        assertThrows(ValidationException.class, () -> service.signOnSite(r.id(), new RapportinoSignRequest("Giulia", png, false)));
        assertThrows(ValidationException.class, () -> service.signOnSite(r.id(), new RapportinoSignRequest("Giulia", "data:image/png;base64,aGVsbG8=", true)));
        assertThrows(ValidationException.class, () -> service.signOnSite(r.id(), new RapportinoSignRequest("Giulia", "not-a-data-url", true)));
        assertThrows(ValidationException.class, () -> service.signOnSite(r.id(), new RapportinoSignRequest(" ", png, true)));
        assertEquals(RapportinoStatus.DRAFT, service.getById(r.id()).status());
    }

    @Test
    void secondSignatureIsRejectedAndProjectionStaysSingle() throws Exception {
        RapportinoDetailResponse r = draftWithHours("4", "2");
        service.signOnSite(r.id(), validSignature());
        em.flush();
        BigDecimal afterFirst = workTotalHours();

        assertThrows(InvalidWorkflowTransitionException.class, () -> service.signOnSite(r.id(), validSignature()));
        Rapportino entity = rapportinoRepository.findById(r.id()).orElseThrow();
        assertThrows(InvalidWorkflowTransitionException.class,
                () -> service.applySignature(entity, validSignature(), marcomanfrin.atixbackend.enums.SignatureSource.LOCAL, null, null));

        em.flush();
        assertEquals(1, workReportEntryRepository.findAll().stream()
                .filter(e -> e.getRapportino() != null && e.getRapportino().getId().equals(r.id())).count());
        assertEquals(0, afterFirst.compareTo(workTotalHours()));
    }

    @Test
    void voidRemovesProjectedHoursAndKeepsDocument() throws Exception {
        RapportinoDetailResponse r = draftWithHours("4", "2");
        service.signOnSite(r.id(), validSignature());
        em.flush();
        BigDecimal signedTotal = workTotalHours();

        RapportinoTestSupport.loginAs(admin);
        RapportinoDetailResponse voided = service.voidRapportino(r.id());
        em.flush();

        assertEquals(RapportinoStatus.VOID, voided.status());
        assertNotNull(voided.voidedAt());
        assertEquals("Anna Test", voided.voidedByName());
        assertEquals(0, signedTotal.subtract(new BigDecimal("6")).compareTo(workTotalHours()));
        assertNotNull(service.getDocument(r.id()).content());

        // riemissione collegata al rapportino annullato
        RapportinoTestSupport.loginAs(mario);
        RapportinoDetailResponse reissued = service.create(new RapportinoCreateRequest(work.getId(), null, null, r.id(), null));
        assertEquals(r.number(), reissued.replacesNumber());
    }

    @Test
    void reissueRequiresAVoidRapportino() {
        RapportinoDetailResponse draft = createAs(mario);
        assertThrows(InvalidWorkflowTransitionException.class,
                () -> service.create(new RapportinoCreateRequest(work.getId(), null, null, draft.id(), null)));
    }

    @Test
    void generatedEntriesAreProtectedManualOnesAreNot() throws Exception {
        RapportinoDetailResponse r = draftWithHours("4", "2");
        service.signOnSite(r.id(), validSignature());
        em.flush();
        WorkReportEntry generated = workReportEntryRepository.findByRapportinoId(r.id()).orElseThrow();

        assertThrows(InvalidWorkflowTransitionException.class, () -> workReportService.updateWorkReportEntry(
                generated.getId(), new WorkReportEntryUpdateRequest(null, "x", BigDecimal.ONE, null)));
        assertThrows(InvalidWorkflowTransitionException.class, () -> workReportService.deleteWorkReportEntry(generated.getId()));

        WorkReport report = workReportRepository.findByWork(work).orElseThrow();
        WorkReportEntry manual = new WorkReportEntry(report, "Manuale", BigDecimal.ONE, java.time.LocalDate.now(), mario);
        report.addEntry(manual);
        workReportEntryRepository.save(manual);
        em.flush();
        assertEquals(0, new BigDecimal("3").compareTo(workReportService.updateWorkReportEntry(
                manual.getId(), new WorkReportEntryUpdateRequest(null, null, new BigDecimal("3"), null)).hours()));
    }

    @Test
    void documentIsNotAvailableBeforeSignature() {
        RapportinoDetailResponse r = createAs(mario);
        assertThrows(marcomanfrin.atixbackend.exceptions.NotFoundException.class, () -> service.getDocument(r.id()));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void storageFailureRollsBackTheWholeSignature() throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        UUID[] ids = tx.execute(status -> {
            TechnicianUser tech = RapportinoTestSupport.technician(em, "Rollback");
            Work w = RapportinoTestSupport.work(em);
            em.flush();
            RapportinoTestSupport.loginAs(tech);
            UUID id = service.create(new RapportinoCreateRequest(w.getId(), null, null, null, null)).id();
            service.update(id, new RapportinoUpdateRequest(null, null, null, null, null, null, null, null, null,
                    "x", new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, null, null, null, null, null, null));
            return new UUID[]{id, w.getId()};
        });

        storage.failNextStore = true;
        RapportinoSignRequest sign = validSignature();
        assertThrows(RuntimeException.class, () -> service.signOnSite(ids[0], sign));

        tx.execute(status -> {
            Rapportino r = rapportinoRepository.findById(ids[0]).orElseThrow();
            assertEquals(RapportinoStatus.DRAFT, r.getStatus());
            assertNull(r.getSignedAt());
            assertNull(r.getPdfAttachmentId());
            assertTrue(workReportEntryRepository.findByRapportinoId(ids[0]).isEmpty());
            return null;
        });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void uploadedObjectIsRemovedWhenTheSignatureTransactionRollsBack() throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        UUID id = tx.execute(status -> {
            TechnicianUser tech = RapportinoTestSupport.technician(em, "Cleanup");
            Work w = RapportinoTestSupport.work(em);
            em.flush();
            RapportinoTestSupport.loginAs(tech);
            return service.create(new RapportinoCreateRequest(w.getId(), null, null, null, null)).id();
        });

        RapportinoSignRequest sign = validSignature();
        int before = storage.removed.size();
        assertThrows(IllegalStateException.class, () -> tx.execute(status -> {
            service.signOnSite(id, sign);
            throw new IllegalStateException("fail after upload");
        }));
        assertEquals(before + 1, storage.removed.size(), "object uploaded during the rolled-back signature is removed");
    }

    // ---------- remote signature ----------

    @Test
    void signatureRequestStoresOnlyTheHashAndFreezesContent() {
        RapportinoDetailResponse r = createAs(mario);
        SignatureRequestCreateResponse created = signatureService.createRequest(r.id(), 60);
        em.flush();

        assertEquals(43, created.token().length(), "256-bit token, base64url without padding");
        var stored = signatureRequestRepository.findByRapportinoId(r.id()).get(0);
        assertNotEquals(created.token(), stored.getTokenHash());
        assertEquals(RapportinoSignatureService.hash(created.token()), stored.getTokenHash());
        Long plaintextHits = (Long) em.createNativeQuery(
                "select count(*) from rapportino_signature_requests where token_hash = :t").setParameter("t", created.token()).getSingleResult();
        assertEquals(0L, plaintextHits);

        assertEquals(RapportinoStatus.AWAITING_SIGNATURE, service.getById(r.id()).status());
        assertThrows(InvalidWorkflowTransitionException.class, () -> service.update(r.id(), updateWith(null, null)));
        assertThrows(InvalidWorkflowTransitionException.class, () -> service.signOnSite(r.id(), validSignature()));
        assertThrows(InvalidWorkflowTransitionException.class, () -> signatureService.createRequest(r.id(), 60));
    }

    @Test
    void expiryIsClampedToTheServerMaximum() {
        assertEquals(RapportinoSignatureService.MAX_EXPIRY_MINUTES, RapportinoSignatureService.clampExpiry(100_000));
        assertEquals(RapportinoSignatureService.DEFAULT_EXPIRY_MINUTES, RapportinoSignatureService.clampExpiry(null));
        assertEquals(15, RapportinoSignatureService.clampExpiry(15));

        RapportinoDetailResponse r = createAs(mario);
        SignatureRequestCreateResponse created = signatureService.createRequest(r.id(), 100_000);
        assertFalse(created.expiresAt().isAfter(java.time.LocalDateTime.now().plusMinutes(RapportinoSignatureService.MAX_EXPIRY_MINUTES + 1)));
    }

    @Test
    void remoteSignatureSignsWithSourceRemoteAndIsSingleUse() throws Exception {
        RapportinoDetailResponse r = draftWithHours("2", "1");
        String token = signatureService.createRequest(r.id(), 60).token();

        PublicRapportinoPreviewResponse preview = signatureService.preview(token);
        assertEquals(r.number(), preview.number());
        assertEquals(0, new BigDecimal("3").compareTo(preview.totalHours()));

        SecurityContextHolder.clearContext(); // il cliente non e' autenticato
        signatureService.sign(token, validSignature(), "203.0.113.7", "Mozilla/5.0 Test");
        em.flush();

        RapportinoTestSupport.loginAs(mario);
        RapportinoDetailResponse signed = service.getById(r.id());
        assertEquals(RapportinoStatus.SIGNED, signed.status());
        assertEquals(marcomanfrin.atixbackend.enums.SignatureSource.REMOTE, signed.signature().source());
        assertTrue(signed.pdfAvailable());
        var sig = em.createQuery("select s from RapportinoSignature s where s.rapportino.id = :id", marcomanfrin.atixbackend.entities.RapportinoSignature.class)
                .setParameter("id", r.id()).getSingleResult();
        assertEquals("203.0.113.7", sig.getSignerIp());
        assertEquals("Mozilla/5.0 Test", sig.getSignerUserAgent());
        assertNotNull(signatureRequestRepository.findByRapportinoId(r.id()).get(0).getUsedAt());
        assertTrue(workReportEntryRepository.findByRapportinoId(r.id()).isPresent());

        assertThrows(marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException.class, () -> signatureService.preview(token));
        assertThrows(marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException.class,
                () -> signatureService.sign(token, validSignature(), "1.1.1.1", "x"));
    }

    @Test
    void missingConsentDoesNotConsumeTheToken() throws Exception {
        RapportinoDetailResponse r = createAs(mario);
        String token = signatureService.createRequest(r.id(), 60).token();
        String png = RapportinoPdfServiceTest.signaturePng();

        assertThrows(ValidationException.class, () -> signatureService.sign(token, new RapportinoSignRequest("Giulia", png, false), "1.1.1.1", "x"));
        assertNull(signatureRequestRepository.findByRapportinoId(r.id()).get(0).getUsedAt());
        assertEquals(RapportinoStatus.AWAITING_SIGNATURE, rapportinoRepository.findById(r.id()).orElseThrow().getStatus());
        assertDoesNotThrow(() -> signatureService.preview(token));
    }

    @Test
    void revokeReturnsToDraftAndKillsTheToken() {
        RapportinoDetailResponse r = createAs(mario);
        String token = signatureService.createRequest(r.id(), 60).token();

        signatureService.revoke(r.id());
        em.flush();

        assertEquals(RapportinoStatus.DRAFT, service.getById(r.id()).status());
        assertThrows(marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException.class, () -> signatureService.preview(token));
        assertDoesNotThrow(() -> service.update(r.id(), updateWith(null, null)));
    }

    @Test
    void expiredRequestReturnsRapportinoToDraft() {
        RapportinoDetailResponse r = createAs(mario);
        String token = signatureService.createRequest(r.id(), 60).token();
        em.flush();
        em.createNativeQuery("update rapportino_signature_requests set expires_at = now() - interval '1 minute' where rapportino_id = :id")
                .setParameter("id", r.id()).executeUpdate();
        em.clear();

        assertThrows(marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException.class, () -> signatureService.preview(token));
        assertEquals(RapportinoStatus.DRAFT, service.getById(r.id()).status());
    }

    @Test
    void unknownExpiredUsedAndRevokedTokensFailIdentically() throws Exception {
        java.util.List<Class<?>> failures = new java.util.ArrayList<>();
        java.util.function.Consumer<String> probe = token -> {
            try {
                signatureService.preview(token);
                failures.add(null);
            } catch (RuntimeException e) {
                failures.add(e.getClass());
                assertEquals(marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException.MESSAGE, e.getMessage());
            }
        };

        probe.accept("unknown-token");

        RapportinoDetailResponse revoked = createAs(mario);
        String revokedToken = signatureService.createRequest(revoked.id(), 60).token();
        signatureService.revoke(revoked.id());
        probe.accept(revokedToken);

        RapportinoDetailResponse used = createAs(mario);
        String usedToken = signatureService.createRequest(used.id(), 60).token();
        signatureService.sign(usedToken, validSignature(), "1.1.1.1", "x");
        probe.accept(usedToken);

        RapportinoDetailResponse expired = createAs(mario);
        String expiredToken = signatureService.createRequest(expired.id(), 60).token();
        em.flush();
        em.createNativeQuery("update rapportino_signature_requests set expires_at = now() - interval '1 minute' where rapportino_id = :id")
                .setParameter("id", expired.id()).executeUpdate();
        em.clear();
        probe.accept(expiredToken);

        assertEquals(4, failures.size());
        assertTrue(failures.stream().allMatch(c -> c == marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException.class), failures.toString());
    }

    // ---------- helpers ----------

    private void forceStatus(UUID id, RapportinoStatus status) {
        Rapportino r = rapportinoRepository.findById(id).orElseThrow();
        r.setStatus(status);
        em.flush();
    }

    private static RapportinoUpdateRequest updateWith(List<ChecklistAnswerRequest> answers, List<MaterialRequest> materials) {
        return new RapportinoUpdateRequest(null, null, null, null, null,
                null, null, null, null, "Intervento", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN,
                null, null, null, null, answers, materials);
    }
}
