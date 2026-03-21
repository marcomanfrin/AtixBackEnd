package marcomanfrin.atixbackend.DTO.dashboard;

import marcomanfrin.atixbackend.enums.WorkStatus;

public record WorkStatusCountDTO(
        WorkStatus status,
        int count
) {}
