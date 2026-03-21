package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.clients.ClientResponse;
import marcomanfrin.atixbackend.DTO.plants.PlantResponse;
import marcomanfrin.atixbackend.DTO.tickets.TicketResponse;
import marcomanfrin.atixbackend.DTO.users.UserSummaryDTO;
import marcomanfrin.atixbackend.DTO.works.WorkAssignmentResponse;
import marcomanfrin.atixbackend.DTO.works.WorkDetailResponse;
import marcomanfrin.atixbackend.DTO.works.WorkRequest;
import marcomanfrin.atixbackend.DTO.works.WorkSummaryResponse;
import marcomanfrin.atixbackend.DTO.works.WorkUpdateRequest;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceAssignmentResponse;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceResponse;
import marcomanfrin.atixbackend.ServiceInterfaces.IWorkService;
import marcomanfrin.atixbackend.entities.*;
import marcomanfrin.atixbackend.entities.users.SellerUser;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.TicketStatus;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.WorkStatus;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.repositories.*;
import marcomanfrin.atixbackend.specifications.WorkSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkService implements IWorkService {
    private final WorkRepository workRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PlantRepository plantRepository;
    private final TicketRepository ticketRepository;
    private final WorksiteReferenceRepository worksiteReferenceRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final WorksiteReferenceAssignmentRepository worksiteReferenceAssignmentRepository;
    private final WorkStateMachine stateMachine;

    public WorkService(WorkRepository workRepository,
                      UserRepository userRepository,
                      ClientRepository clientRepository,
                      PlantRepository plantRepository,
                      TicketRepository ticketRepository,
                      WorksiteReferenceRepository worksiteReferenceRepository,
                      WorkAssignmentRepository workAssignmentRepository,
                      WorksiteReferenceAssignmentRepository worksiteReferenceAssignmentRepository,
                      WorkStateMachine stateMachine) {
        this.workRepository = workRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.plantRepository = plantRepository;
        this.ticketRepository = ticketRepository;
        this.worksiteReferenceRepository = worksiteReferenceRepository;
        this.workAssignmentRepository = workAssignmentRepository;
        this.worksiteReferenceAssignmentRepository = worksiteReferenceAssignmentRepository;
        this.stateMachine = stateMachine;
    }

    @Override
    @Transactional
    public WorkDetailResponse createWork(WorkRequest request) {
        Work work = new Work();
        work.setName(request.name());
        work.setDescription(request.description());
        work.setBidNumber(request.bidNumber());
        work.setOrderNumber(request.orderNumber());
        work.setOrderDate(request.orderDate());
        work.setElectricalSchemaProgression(request.electricalSchemaProgression() != null ? request.electricalSchemaProgression() : 0);
        work.setProgrammingProgression(request.programmingProgression() != null ? request.programmingProgression() : 0);
        work.setExpectedStartDate(request.expectedStartDate());
        work.setNasSubDirectory(request.nasSubDirectory());
        work.setExpectedOfficeHours(request.expectedOfficeHours() != null ? request.expectedOfficeHours() : 0.0);
        work.setExpectedPlantHours(request.expectedPlantHours() != null ? request.expectedPlantHours() : 0.0);
        work.setStatus(WorkStatus.SCHEDULED);
        work.setStatusChangedAt(LocalDateTime.now());

        // Set seller
        if (request.sellerId() != null) {
            User seller = userRepository.findById(request.sellerId())
                    .orElseThrow(() -> new NotFoundException("Seller not found"));
            if (!(seller instanceof SellerUser)) {
                throw new IllegalArgumentException("User is not a seller");
            }
            work.setSeller((SellerUser) seller);
        }

        // Set plant
        if (request.plantId() != null) {
            Plant plant = plantRepository.findById(request.plantId())
                    .orElseThrow(() -> new NotFoundException("Plant not found"));
            work.setPlant(plant);
        }

        // Set Atix client (optional)
        if (request.atixClientId() != null) {
            Client atixClient = clientRepository.findById(request.atixClientId())
                    .orElseThrow(() -> new NotFoundException("Atix client not found"));
            work.setAtixClient(atixClient);
        }

        // Set final client (optional)
        if (request.finalClientId() != null) {
            Client finalClient = clientRepository.findById(request.finalClientId())
                    .orElseThrow(() -> new NotFoundException("Final client not found"));
            work.setFinalClient(finalClient);
        }

        Work savedWork = workRepository.save(work);

        // Set ticket (optional) - must be done after saving work because Ticket is the owning side
        if (request.ticketId() != null) {
            Ticket ticket = ticketRepository.findById(request.ticketId())
                    .orElseThrow(() -> new NotFoundException("Ticket not found"));
            ticket.setOrderNumber(savedWork);
            ticketRepository.save(ticket);
            savedWork.setTicket(ticket);
        }
        return toWorkDetailResponse(savedWork);
    }

    @Override
    public Page<WorkSummaryResponse> getAllWorks(Pageable pageable) {
        return workRepository.findAll(pageable)
                .map(this::toWorkSummaryResponse);
    }

    @Override
    public Page<WorkSummaryResponse> getFilteredWorks(
            UUID clientId,
            UUID atixClientId,
            UUID finalClientId,
            UUID sellerId,
            UUID plantId,
            UUID ticketId,
            UUID technicianId,
            WorkStatus status,
            List<WorkStatus> statuses,
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            LocalDate expectedStartDateFrom,
            LocalDate expectedStartDateTo,
            String name,
            String bidNumber,
            String orderNumber,
            String search,
            Pageable pageable) {

        // Build specification by combining all filters
        Specification<Work> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Client filters - prioritize specific client filters over general clientId
        if (atixClientId != null) {
            spec = spec.and(WorkSpecification.hasAtixClient(atixClientId));
        }
        if (finalClientId != null) {
            spec = spec.and(WorkSpecification.hasFinalClient(finalClientId));
        }
        // Use clientId to search in both atixClient and finalClient if no specific filter is provided
        if (clientId != null && atixClientId == null && finalClientId == null) {
            spec = spec.and(WorkSpecification.hasAnyClient(clientId));
        }

        // Other filters
        spec = spec.and(WorkSpecification.hasSeller(sellerId));
        spec = spec.and(WorkSpecification.hasPlant(plantId));
        spec = spec.and(WorkSpecification.hasTicket(ticketId));
        spec = spec.and(WorkSpecification.hasAssignedTechnician(technicianId));
        spec = spec.and(WorkSpecification.hasStatus(status));
        spec = spec.and(WorkSpecification.hasStatusIn(statuses));
        spec = spec.and(WorkSpecification.hasOrderDateAfter(orderDateFrom));
        spec = spec.and(WorkSpecification.hasOrderDateBefore(orderDateTo));
        spec = spec.and(WorkSpecification.hasExpectedStartDateAfter(expectedStartDateFrom));
        spec = spec.and(WorkSpecification.hasExpectedStartDateBefore(expectedStartDateTo));
        spec = spec.and(WorkSpecification.hasNameContaining(name));
        spec = spec.and(WorkSpecification.hasBidNumber(bidNumber));
        spec = spec.and(WorkSpecification.hasOrderNumber(orderNumber));
        spec = spec.and(WorkSpecification.searchByKeyword(search));

        return workRepository.findAll(spec, pageable)
                .map(this::toWorkSummaryResponse);
    }

    @Override
    public WorkDetailResponse getWorkById(UUID id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));
        return toWorkDetailResponse(work);
    }

    @Override
    @Transactional
    public WorkDetailResponse updateWork(UUID id, WorkUpdateRequest request) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        // PATCH logic: update only non-null fields
        if (request.name() != null) {
            work.setName(request.name());
        }
        if (request.description() != null) {
            work.setDescription(request.description());
        }
        if (request.bidNumber() != null) {
            work.setBidNumber(request.bidNumber());
        }
        if (request.orderNumber() != null) {
            work.setOrderNumber(request.orderNumber());
        }
        if (request.orderDate() != null) {
            work.setOrderDate(request.orderDate());
        }
        if (request.electricalSchemaProgression() != null) {
            work.setElectricalSchemaProgression(request.electricalSchemaProgression());
        }
        if (request.programmingProgression() != null) {
            work.setProgrammingProgression(request.programmingProgression());
        }
        if (request.expectedStartDate() != null) {
            work.setExpectedStartDate(request.expectedStartDate());
        }
        if (request.nasSubDirectory() != null) {
            work.setNasSubDirectory(request.nasSubDirectory());
        }
        if (request.expectedOfficeHours() != null) {
            work.setExpectedOfficeHours(request.expectedOfficeHours());
        }
        if (request.expectedPlantHours() != null) {
            work.setExpectedPlantHours(request.expectedPlantHours());
        }

        // Update seller if provided
        if (request.sellerId() != null) {
            User seller = userRepository.findById(request.sellerId())
                    .orElseThrow(() -> new NotFoundException("Seller not found"));
            if (!(seller instanceof SellerUser)) {
                throw new IllegalArgumentException("User is not a seller");
            }
            work.setSeller((SellerUser) seller);
        }

        // Update plant if provided
        if (request.plantId() != null) {
            Plant plant = plantRepository.findById(request.plantId())
                    .orElseThrow(() -> new NotFoundException("Plant not found"));
            work.setPlant(plant);
        }

        // Update Atix client if provided
        if (request.atixClientId() != null) {
            Client atixClient = clientRepository.findById(request.atixClientId())
                    .orElseThrow(() -> new NotFoundException("Atix client not found"));
            work.setAtixClient(atixClient);
        }

        // Update final client if provided
        if (request.finalClientId() != null) {
            Client finalClient = clientRepository.findById(request.finalClientId())
                    .orElseThrow(() -> new NotFoundException("Final client not found"));
            work.setFinalClient(finalClient);
        }

        Work updatedWork = workRepository.save(work);

        // Update ticket if provided - must be done after saving work because Ticket is the owning side
        if (request.ticketId() != null) {
            // Remove old ticket association if exists
            if (work.getTicket() != null && !work.getTicket().getId().equals(request.ticketId())) {
                Ticket oldTicket = work.getTicket();
                oldTicket.setOrderNumber(null);
                ticketRepository.save(oldTicket);
            }

            Ticket ticket = ticketRepository.findById(request.ticketId())
                    .orElseThrow(() -> new NotFoundException("Ticket not found"));
            ticket.setOrderNumber(updatedWork);
            ticketRepository.save(ticket);
            updatedWork.setTicket(ticket);
        }

        return toWorkDetailResponse(updatedWork);
    }

    @Override
    @Transactional
    public void startWork(UUID id, UUID userId, UserRole userRole) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        stateMachine.validateTransition(work.getStatus(), WorkStatus.IN_PROGRESS, userRole);

        work.setStatus(WorkStatus.IN_PROGRESS);
        work.setStatusChangedAt(LocalDateTime.now());
        work.setStatusChangedBy(userId);
        workRepository.save(work);

        // Sync ticket: OPEN → IN_PROGRESS
        if (work.getTicket() != null) {
            syncTicketStatus(work.getTicket(), TicketStatus.IN_PROGRESS, userId);
        }
    }

    @Override
    @Transactional
    public void closeWork(UUID id, UUID userId, UserRole userRole) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        stateMachine.validateTransition(work.getStatus(), WorkStatus.CLOSED, userRole);

        work.setStatus(WorkStatus.CLOSED);
        work.setStatusChangedAt(LocalDateTime.now());
        work.setStatusChangedBy(userId);
        workRepository.save(work);

        // Sync ticket: → RESOLVED
        if (work.getTicket() != null) {
            syncTicketStatus(work.getTicket(), TicketStatus.RESOLVED, userId);
        }
    }

    @Override
    @Transactional
    public void invoiceWork(UUID id, UUID userId, UserRole userRole) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        stateMachine.validateTransition(work.getStatus(), WorkStatus.INVOICED, userRole);

        work.setStatus(WorkStatus.INVOICED);
        work.setStatusChangedAt(LocalDateTime.now());
        work.setStatusChangedBy(userId);
        workRepository.save(work);

        // Sync ticket: → CLOSED
        if (work.getTicket() != null) {
            syncTicketStatus(work.getTicket(), TicketStatus.CLOSED, userId);
        }
    }

    @Override
    @Transactional
    public void reopenWork(UUID id, UUID userId, UserRole userRole) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        stateMachine.validateTransition(work.getStatus(), WorkStatus.IN_PROGRESS, userRole);

        work.setStatus(WorkStatus.IN_PROGRESS);
        work.setStatusChangedAt(LocalDateTime.now());
        work.setStatusChangedBy(userId);
        workRepository.save(work);

        // Sync ticket: → IN_PROGRESS
        if (work.getTicket() != null) {
            syncTicketStatus(work.getTicket(), TicketStatus.IN_PROGRESS, userId);
        }
    }

    @Override
    @Transactional
    public void forceStatusChange(UUID id, WorkStatus newStatus, UUID userId, UserRole userRole) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        stateMachine.validateTransition(work.getStatus(), newStatus, userRole);

        work.setStatus(newStatus);
        work.setStatusChangedAt(LocalDateTime.now());
        work.setStatusChangedBy(userId);
        workRepository.save(work);

        // Sync ticket based on new status
        if (work.getTicket() != null) {
            TicketStatus ticketStatus = mapWorkStatusToTicketStatus(newStatus);
            syncTicketStatus(work.getTicket(), ticketStatus, userId);
        }
    }

    private void syncTicketStatus(Ticket ticket, TicketStatus newStatus, UUID changedBy) {
        ticket.setStatus(newStatus);
        ticket.setStatusChangedAt(LocalDateTime.now());
        ticket.setStatusChangedBy(changedBy);
        ticketRepository.save(ticket);
    }

    private TicketStatus mapWorkStatusToTicketStatus(WorkStatus workStatus) {
        return switch (workStatus) {
            case SCHEDULED -> TicketStatus.OPEN;
            case IN_PROGRESS -> TicketStatus.IN_PROGRESS;
            case CLOSED -> TicketStatus.RESOLVED;
            case INVOICED -> TicketStatus.CLOSED;
        };
    }

    @Override
    @Transactional
    public void assignTechnician(UUID workId, UUID technicianId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new NotFoundException("Technician not found with id: " + technicianId));
        if (!(technician instanceof TechnicianUser)) {
            throw new IllegalArgumentException("only TechnicianU users can be assigned to a work");
        }

        // Check if already assigned
        if (workAssignmentRepository.existsByWorkAndUser(work, technician)) {
            throw new IllegalArgumentException("Technician already assigned to this work");
        }

        WorkAssignment assignment = new WorkAssignment(work, technician);
        workAssignmentRepository.save(assignment);

        // Auto-transition to IN_PROGRESS when a technician is assigned to a SCHEDULED work
        if (work.isScheduled()) {
            work.setStatus(WorkStatus.IN_PROGRESS);
            work.setStatusChangedAt(LocalDateTime.now());
            work.setStatusChangedBy(technicianId);
            workRepository.save(work);

            if (work.getTicket() != null) {
                syncTicketStatus(work.getTicket(), TicketStatus.IN_PROGRESS, technicianId);
            }
        }
    }

    @Override
    @Transactional
    public void addWorksiteReference(UUID workId, UUID worksiteReferenceId, WorksiteReferenceRole role) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));

        WorksiteReference worksiteReference = worksiteReferenceRepository.findById(worksiteReferenceId)
                .orElseThrow(() -> new NotFoundException("Worksite reference not found with id: " + worksiteReferenceId));

        // Upsert: if already assigned, update the role; otherwise create new assignment
        var existing = worksiteReferenceAssignmentRepository.findByWorkAndWorksiteReference(work, worksiteReference);
        if (existing.isPresent()) {
            existing.get().setRole(role);
            worksiteReferenceAssignmentRepository.save(existing.get());
        } else {
            WorksiteReferenceAssignment assignment = new WorksiteReferenceAssignment(work, worksiteReference, role);
            worksiteReferenceAssignmentRepository.save(assignment);
        }
    }

    @Override
    @Transactional
    public void deleteWork(UUID id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        // Remove associations before deleting the work
        work.getAssignments().clear();
        work.getWorksiteReferenceAssignments().clear();

        if (work.getTicket() != null) {
            work.getTicket().setOrderNumber(null);
            ticketRepository.save(work.getTicket());
        }

        workRepository.delete(work);
    }

    @Override
    @Transactional
    public void removeWorksiteReference(UUID workId, UUID worksiteReferenceAssignmentId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));

        WorksiteReferenceAssignment assignment = worksiteReferenceAssignmentRepository.findById(worksiteReferenceAssignmentId)
                .orElseThrow(() -> new NotFoundException("Worksite reference assignment not found with id: " + worksiteReferenceAssignmentId));

        if (!assignment.getWork().equals(work)) {
            throw new IllegalArgumentException("Worksite reference assignment does not belong to the specified work");
        }

        work.getWorksiteReferenceAssignments().remove(assignment);
        worksiteReferenceAssignmentRepository.delete(assignment);
    }

    @Override
    @Transactional
    public void removeTechnician(UUID workId, UUID technicianId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new NotFoundException("Technician not found with id: ".concat(technicianId.toString())));

        WorkAssignment assignment = workAssignmentRepository.findByWorkAndUser(work, technician)
                .orElseThrow(() -> new NotFoundException("Technician not assigned to this work"));

        work.getAssignments().remove(assignment);
        workAssignmentRepository.delete(assignment);
    }

    // F3: include atixClient in summary so the work card shows it after the order number
    private WorkSummaryResponse toWorkSummaryResponse(Work work) {
        return new WorkSummaryResponse(
                work.getId(),
                work.getName(),
                work.getBidNumber(),
                work.getOrderNumber(),
                work.getOrderDate(),
                work.getStatus(),
                work.getElectricalSchemaProgression(),
                work.getProgrammingProgression(),
                work.getNasSubDirectory(),
                work.getPlant() != null ? work.getPlant().getNasDirectory() : null,
                work.getExpectedStartDate(),
                work.getPlant() != null ? toPlantResponse(work.getPlant()) : null,
                work.getAtixClient() != null ? toClientResponse(work.getAtixClient()) : null,
                work.getFinalClient() != null ? toClientResponse(work.getFinalClient()) : null,
                work.getAssignments().stream()
                        .map(this::toWorkAssignmentResponse)
                        .collect(Collectors.toList())
        );
    }

    private WorkDetailResponse toWorkDetailResponse(Work work) {
        return new WorkDetailResponse(
                work.getId(),
                work.getName(),
                work.getDescription(),
                work.getBidNumber(),
                work.getSeller() != null ? toUserSummaryDTO(work.getSeller()) : null,
                work.getOrderNumber(),
                work.getOrderDate(),
                work.getElectricalSchemaProgression(),
                work.getProgrammingProgression(),
                work.getExpectedStartDate(),
                work.getStatus(),
                work.getStatusChangedAt(),
                work.getCreatedAt(),
                work.getPlant() != null ? toPlantResponse(work.getPlant()) : null,
                work.getAtixClient() != null ? toClientResponse(work.getAtixClient()) : null,
                work.getFinalClient() != null ? toClientResponse(work.getFinalClient()) : null,
                work.getWorksiteReferenceAssignments().stream()
                        .map(this::toWorksiteReferenceAssignmentResponse)
                        .collect(Collectors.toList()),
                work.getAssignments().stream()
                        .map(this::toWorkAssignmentResponse)
                        .collect(Collectors.toList()),
                work.getNasSubDirectory(),
                work.getExpectedOfficeHours(),
                work.getExpectedPlantHours(),
                work.getTicket() != null ? toTicketResponse(work.getTicket()) : null
        );
    }

    private UserSummaryDTO toUserSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getUserType().name()
        );
    }

    private PlantResponse toPlantResponse(Plant plant) {
        return new PlantResponse(
                plant.getId(),
                plant.getName(),
                plant.getNotes(),
                plant.getNasDirectory(),
                plant.getPswPhrase(),
                plant.getPswPlatform(),
                plant.getPswStation(),
                plant.getCreatedAt()
        );
    }

    private ClientResponse toClientResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getType()
        );
    }

    private WorksiteReferenceResponse toWorksiteReferenceResponse(WorksiteReference worksiteReference) {
        return new WorksiteReferenceResponse(
                worksiteReference.getId(),
                worksiteReference.getName(),
                worksiteReference.getTelephone(),
                worksiteReference.getNotes()
        );
    }

    private WorksiteReferenceAssignmentResponse toWorksiteReferenceAssignmentResponse(WorksiteReferenceAssignment assignment) {
        return new WorksiteReferenceAssignmentResponse(
                assignment.getId(),
                assignment.getWorksiteReference().getId(),
                assignment.getWorksiteReference().getName(),
                assignment.getWorksiteReference().getTelephone(),
                assignment.getRole()
        );
    }

    private WorkAssignmentResponse toWorkAssignmentResponse(WorkAssignment assignment) {
        User technician = assignment.getUser();
        return new WorkAssignmentResponse(
                assignment.getId(),
                technician.getId(),
                technician.getFirstName(),
                technician.getLastName(),
                technician.getEmail(),
                assignment.getAssignedAt()
        );
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSenderEmail(),
                ticket.getOrderNumber() != null ? ticket.getOrderNumber().getId() : null,
                ticket.getName(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCreatedAt()
        );
    }
}
