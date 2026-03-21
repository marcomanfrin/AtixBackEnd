package marcomanfrin.atixbackend.controllers;

import marcomanfrin.atixbackend.DTO.accessLog.AccessLogResponseDTO;
import marcomanfrin.atixbackend.services.AccessLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/access-logs")
public class AccessLogController {

    private final AccessLogService accessLogService;

    public AccessLogController(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<AccessLogResponseDTO>> getAccessLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 50, sort = "timestamp") Pageable pageable
    ) {
        Page<AccessLogResponseDTO> logs = accessLogService.getAccessLogs(userId, email, success, from, to, pageable);
        return ResponseEntity.ok(logs);
    }
}
