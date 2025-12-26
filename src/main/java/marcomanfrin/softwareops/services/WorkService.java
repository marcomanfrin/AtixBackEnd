package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.clients.ClientResponse;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;
import marcomanfrin.softwareops.DTO.tickets.TicketResponse;
import marcomanfrin.softwareops.DTO.users.UserSummaryDTO;
import marcomanfrin.softwareops.DTO.works.WorkDetailResponse;
import marcomanfrin.softwareops.DTO.works.WorkRequest;
import marcomanfrin.softwareops.DTO.works.WorkSummaryResponse;
import marcomanfrin.softwareops.DTO.worksiteReferences.WorksiteReferenceResponse;
import marcomanfrin.softwareops.ServiceInterfaces.IWorkService;
import marcomanfrin.softwareops.entities.*;
import marcomanfrin.softwareops.entities.users.SellerUser;
import marcomanfrin.softwareops.entities.users.User;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.exceptions.ValidationException;
import marcomanfrin.softwareops.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public WorkService(WorkRepository workRepository,
                      UserRepository userRepository,
                      ClientRepository clientRepository,
                      PlantRepository plantRepository,
                      TicketRepository ticketRepository,
                      WorksiteReferenceRepository worksiteReferenceRepository,
                      WorkAssignmentRepository workAssignmentRepository) {
        this.workRepository = workRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.plantRepository = plantRepository;
        this.ticketRepository = ticketRepository;
        this.worksiteReferenceRepository = worksiteReferenceRepository;
        this.workAssignmentRepository = workAssignmentRepository;
    }

    @Override
    @Transactional
    public WorkDetailResponse createWork(WorkRequest request) {
        Work work = new Work();
        work.setName(request.name());
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
    public WorkDetailResponse getWorkById(UUID id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));
        return toWorkDetailResponse(work);
    }

    @Override
    @Transactional
    public WorkDetailResponse updateWork(UUID id, WorkRequest request) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + id));

        // PATCH logic: update only non-null fields
        if (request.name() != null) {
            work.setName(request.name());
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

        // Check if already assigned
        if (workAssignmentRepository.existsByWorkAndUser(work, technician)) {
            throw new IllegalArgumentException("Technician already assigned to this work");
        }

        WorkAssignment assignment = new WorkAssignment(work, technician);
        workAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void addWorksiteReference(UUID workId, UUID worksiteReferenceId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));

        WorksiteReference worksiteReference = worksiteReferenceRepository.findById(worksiteReferenceId)
                .orElseThrow(() -> new NotFoundException("Worksite reference not found with id: " + worksiteReferenceId));

        // Check if already added
        if (work.getWorksiteReferences().contains(worksiteReference)) {
            throw new IllegalArgumentException("Worksite reference already added to this work");
        }

        work.getWorksiteReferences().add(worksiteReference);
        workRepository.save(work);
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
                work.getProgrammingProgression()
        );
    }

    private WorkDetailResponse toWorkDetailResponse(Work work) {
        return new WorkDetailResponse(
                work.getId(),
                work.getName(),
                work.getBidNumber(),
                work.getSeller() != null ? toUserSummaryDTO(work.getSeller()) : null,
                work.getOrderNumber(),
                work.getOrderDate(),
                work.getElectricalSchemaProgression(),
                work.getProgrammingProgression(),
                work.getExpectedStartDate(),
                work.isCompleted(),
                work.getCompletedAt(),
                work.isInvoiced(),
                work.getInvoicedAt(),
                work.getPlant() != null ? toPlantResponse(work.getPlant()) : null,
                toClientResponse(work.getAtixClient()),
                work.getFinalClient() != null ? toClientResponse(work.getFinalClient()) : null,
                work.getWorksiteReferences().stream()
                        .map(this::toWorksiteReferenceResponse)
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

    private TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSenderEmail(),
                ticket.getOrderNumber() != null ? ticket.getOrderNumber().getId() : null,
                ticket.getName(),
                ticket.getDescription(),
                ticket.getStatus()
        );
    }
}
