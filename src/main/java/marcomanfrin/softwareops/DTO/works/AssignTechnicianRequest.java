package marcomanfrin.softwareops.DTO.works;

import marcomanfrin.softwareops.enums.AssignmentRole;
import java.util.UUID;

public record AssignTechnicianRequest (UUID technicianId, AssignmentRole role){}
