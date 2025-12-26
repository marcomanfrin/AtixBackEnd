package marcomanfrin.softwareops.DTO.works;

import marcomanfrin.softwareops.enums.WorkStatus;

public record UpdateWorkRequest(WorkStatus status) {
}
