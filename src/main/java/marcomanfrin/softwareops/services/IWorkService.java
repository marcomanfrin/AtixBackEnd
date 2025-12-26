package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.works.WorkResponseDTO;
import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.entities.WorkFromPlant;
import marcomanfrin.softwareops.entities.WorkFromTicket;
import marcomanfrin.softwareops.enums.WorkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IWorkService {
    WorkResponseDTO createWorkFromPlant(UUID plantId);
    Work createWorkFromTicket(UUID ticketId);

    Optional<Work> getWorkById(UUID id);
    List<Work> getAllWorks();
    Page<Work> getWorks(WorkStatus status, UUID technicianId, Pageable pageable);
    void deleteWork(UUID id);

    Work assignTechnician(UUID workId, UUID technicianId);
    Work updateWorkStatus(UUID workId, WorkStatus status);

    Work completeWork(UUID workId);

    List<Work> getWorksByTechnician(UUID technicianId);
    List<Work> getWorksByStatus(WorkStatus status);
    List<WorkFromPlant> getWorksByPlantId(UUID plantId);
    List<WorkFromTicket> getWorksByTicketId(UUID ticketId);

    Work getWorkByIdOrThrow(UUID id);
}
