package marcomanfrin.atixbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.rapportini.PublicRapportinoPreviewResponse;
import marcomanfrin.atixbackend.DTO.rapportini.RapportinoSignRequest;
import marcomanfrin.atixbackend.security.PublicRateLimiter;
import marcomanfrin.atixbackend.services.RapportinoSignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Endpoint anonimi per la firma remota (permessi da /public/** in SecurityConfig).
// L'unica credenziale e' il token nell'URL; nessun identificativo interno nelle risposte.
@RestController
@RequestMapping("/public/rapportini/sign")
public class PublicSignatureController {
    private final RapportinoSignatureService signatureService;
    private final PublicRateLimiter rateLimiter;

    public PublicSignatureController(RapportinoSignatureService signatureService, PublicRateLimiter rateLimiter) {
        this.signatureService = signatureService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/{token}")
    public ResponseEntity<PublicRapportinoPreviewResponse> preview(@PathVariable String token, HttpServletRequest http) {
        rateLimiter.check(PublicRateLimiter.clientIp(http));
        return ResponseEntity.ok(signatureService.preview(token));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> sign(@PathVariable String token,
                                                    @Valid @RequestBody RapportinoSignRequest request,
                                                    HttpServletRequest http) {
        String ip = PublicRateLimiter.clientIp(http);
        rateLimiter.check(ip);
        signatureService.sign(token, request, ip, http.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("status", "SIGNED"));
    }
}
