package marcomanfrin.atixbackend.DTO.dashboard;

import marcomanfrin.atixbackend.enums.TicketStatus;

public record TicketStatusCountDTO(TicketStatus status, int count) {
}
