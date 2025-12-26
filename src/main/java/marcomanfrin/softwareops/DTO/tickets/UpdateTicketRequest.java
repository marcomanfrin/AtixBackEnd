package marcomanfrin.softwareops.DTO.tickets;

import marcomanfrin.softwareops.enums.TicketStatus;

public record UpdateTicketRequest(String name, String description, TicketStatus status) {
}
