package marcomanfrin.softwareops.DTO.dashboard;

import marcomanfrin.softwareops.enums.WorkStatus;

public record WorkStatusCountDTO(WorkStatus status, int count) {
}
