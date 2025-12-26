package marcomanfrin.softwareops.DTO.tasks;

import marcomanfrin.softwareops.enums.TaskStatus;

public record UpdateTaskStatusRequest(TaskStatus status) {
}
