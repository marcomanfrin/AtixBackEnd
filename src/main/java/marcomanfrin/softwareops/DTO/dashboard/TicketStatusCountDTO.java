package marcomanfrin.softwareops.DTO.dashboard;

import marcomanfrin.softwareops.enums.TicketStatus;

public record TicketStatusCountDTO(TicketStatus status, int count) {
}
