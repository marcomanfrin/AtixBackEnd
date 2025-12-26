package marcomanfrin.softwareops.DTO.works;

import marcomanfrin.softwareops.entities.*;
import marcomanfrin.softwareops.enums.WorkStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkResponseDTO(
        UUID id,
        WorkStatus status,
        int progressPercent,

        UUID plantId,
        UUID ticketId,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static WorkResponseDTO fromEntity(Work work) {
        UUID plantId = null;
        UUID ticketId = null;

        if (work instanceof WorkFromPlant wfp && wfp.getPlant() != null) {
            plantId = wfp.getPlant().getId();
        }

        if (work instanceof WorkFromTicket wft && wft.getTicket() != null) {
            ticketId = wft.getTicket().getId();
        }

        return new WorkResponseDTO(
                work.getId(),
                work.getStatus(),
                work.getProgressPercent(),
                plantId,
                ticketId,
                work.getCreatedAt(),
                work.getUpdatedAt()
        );
    }
}