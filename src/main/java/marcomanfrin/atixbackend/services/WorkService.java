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
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.exceptions.ValidationException;
import marcomanfrin.atixbackend.repositories.*;
import marcomanfrin.atixbackend.specifications.WorkSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public WorkService(WorkRepository workRepository,
                      UserRepository userRepository,
                      ClientRepository clientRepository,
                      PlantRepository plantRepository,
                      TicketRepository ticketRepository,
                      WorksiteReferenceRepository worksiteReferenceRepository,
                      WorkAssignmentRepository workAssignmentRepository,
                      WorksiteReferenceAssignmentRepository worksiteReferenceAssignmentRepository) {
        this.workRepository = workRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.plantRepository = plantRepository;
        this.ticketRepository = ticketRepository;
        this.worksiteReferenceRepository = worksiteReferenceRepository;
        this.workAssignmentRepository = workAssignmentRepository;
        this.worksiteReferenceAssignmentRepository = worksiteReferenceAssignmentRepository;
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
        work.setExpectedOfficeHours(request.expectedOfficeHours() != null ? request.expectedOfficeHours() : 0);
        work.setExpectedPlantHours(request.expectedPlantHours() != null ? request.expectedPlantHours() : 0);

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

        // Set Atix client (required)
        Client atixClient = clientRepository.findById(request.atixClientId())
                .orElseThrow(() -> new NotFoundException("Atix client not found"));
        work.setAtixClient(atixClient);

        // Set final client (optional)
        if (request.finalClientId() != null) {
            Client finalClient = clientRepository.findById(request.finalClientId())
                    .orElseThrow(() -> new NotFoundException("Final client not found"));
            work.setFinalClient(finalClient);
        }

        // Set ticket (optional)
        if (request.ticketId() != null) {
            Ticket ticket = ticketRepository.findById(request.ticketId())
                    .orElseThrow(() -> new NotFoundException("Ticket not found"));
            work.setTicket(ticket);
        }

        Work savedWork = workRepository.save(work);
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
            Boolean completed,
            Boolean invoiced,
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            LocalDate expectedStartDateFrom,
            LocalDate expectedStartDateTo,
            String name,
            String bidNumber,
            String orderNumber,
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
        spec = spec.and(WorkSpecification.isCompleted(completed));
        spec = spec.and(WorkSpecification.isInvoiced(invoiced));
        spec = spec.and(WorkSpecification.hasOrderDateAfter(orderDateFrom));
        spec = spec.and(WorkSpecification.hasOrderDateBefore(orderDateTo));
        spec = spec.and(WorkSpecification.hasExpectedStartDateAfter(expectedStartDateFrom));
        spec = spec.and(WorkSpecification.hasExpectedStartDateBefore(expectedStartDateTo));
        spec = spec.and(WorkSpecification.hasNameContaining(name));
        spec = spec.and(WorkSpecification.hasBidNumber(bidNumber));
        spec = spec.and(WorkSpecification.hasOrderNumber(orderNumber));

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

        // Update ticket if provided
        if (request.ticketId() != null) {
            Ticket ticket = ticketRepository.findById(request.ticketId())
                    .orElseThrow(() -> new NotFoundException("Ticket not found"));
            work.setTicket(ticket);
        }

        Work updatedWork = workRepository.save(work);
        return toWorkDetailResponse(updatedWork);
    }

    @Override
    @Transactional
    public void closeWork(UUID id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));
        work.setCompleted(true);
        work.setCompletedAt(LocalDateTime.now());
        workRepository.save(work);
    }

    @Override
    @Transactional
    public void invoiceWork(UUID id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));
        work.setInvoiced(true);
        work.setInvoicedAt(LocalDateTime.now());
        workRepository.save(work);
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
    }

    @Override
    @Transactional
    public void addWorksiteReference(UUID workId, UUID worksiteReferenceId, WorksiteReferenceRole role) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));

        WorksiteReference worksiteReference = worksiteReferenceRepository.findById(worksiteReferenceId)
                .orElseThrow(() -> new NotFoundException("Worksite reference not found with id: " + worksiteReferenceId));

        // Check if already assigned
        if (worksiteReferenceAssignmentRepository.existsByWorkAndWorksiteReference(work, worksiteReference)) {
            throw new IllegalArgumentException("Worksite reference already assigned to this work");
        }

        // Create assignment with role
        WorksiteReferenceAssignment assignment = new WorksiteReferenceAssignment(work, worksiteReference, role);
        worksiteReferenceAssignmentRepository.save(assignment);
    }

    private WorkSummaryResponse toWorkSummaryResponse(Work work) {
        return new WorkSummaryResponse(
                work.getId(),
                work.getName(),
                work.getBidNumber(),
                work.getOrderNumber(),
                work.getOrderDate(),
                work.isCompleted(),
                work.isInvoiced(),
                work.getElectricalSchemaProgression(),
                work.getProgrammingProgression(),
                work.getNasSubDirectory(),
                work.getPlant() != null ? work.getPlant().getNasDirectory() : null,
                work.getExpectedStartDate(),
                work.getPlant() != null ? toPlantResponse(work.getPlant()) : null,
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
                work.isCompleted(),
                work.getCompletedAt(),
                work.getCreatedAt(),
                work.isInvoiced(),
                work.getInvoicedAt(),
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
                plant.getPswStation()
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
                worksiteReference.getName()
        );
    }

    private WorksiteReferenceAssignmentResponse toWorksiteReferenceAssignmentResponse(WorksiteReferenceAssignment assignment) {
        return new WorksiteReferenceAssignmentResponse(
                assignment.getId(),
                assignment.getWorksiteReference().getId(),
                assignment.getWorksiteReference().getName(),
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
