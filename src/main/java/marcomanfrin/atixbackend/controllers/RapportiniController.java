package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.rapportini.*;
import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.services.RapportinoService;
import marcomanfrin.atixbackend.services.RapportinoSignatureService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rapportini")
public class RapportiniController {
    private final RapportinoService rapportinoService;
    private final RapportinoSignatureService signatureService;

    public RapportiniController(RapportinoService rapportinoService, RapportinoSignatureService signatureService) {
        this.rapportinoService = rapportinoService;
        this.signatureService = signatureService;
    }

    @PostMapping
    public ResponseEntity<RapportinoDetailResponse> create(@Valid @RequestBody RapportinoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rapportinoService.create(request));
    }

    // Visibilita' filtrata nel service in base a ruolo/tipo utente
    @GetMapping
    public ResponseEntity<Page<RapportinoListItemResponse>> list(
            @RequestParam(required = false) UUID workId,
            @RequestParam(required = false) UUID technicianId,
            @RequestParam(required = false) RapportinoStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Pageable pageable) {
        return ResponseEntity.ok(rapportinoService.list(workId, technicianId, status, dateFrom, dateTo, pageable));
    }

    @GetMapping("/checklist-template")
    public ResponseEntity<List<ChecklistTemplateItemResponse>> getChecklistTemplate() {
        return ResponseEntity.ok(rapportinoService.getChecklistTemplate());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RapportinoDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(rapportinoService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RapportinoDetailResponse> update(@PathVariable UUID id,
                                                           @Valid @RequestBody RapportinoUpdateRequest request) {
        return ResponseEntity.ok(rapportinoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        rapportinoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<RapportinoDetailResponse> sign(@PathVariable UUID id,
                                                         @Valid @RequestBody RapportinoSignRequest request) {
        return ResponseEntity.ok(rapportinoService.signOnSite(id, request));
    }

    // Il PDF passa sempre dal backend (stessa visibilita' del dettaglio): nessun URL di storage esposto
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable UUID id) {
        RapportinoService.PdfDocument document = rapportinoService.getDocument(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(document.filename()).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(document.content());
    }

    // Firma remota: il token in chiaro e' nella risposta solo qui; il frontend costruisce /sign/{token}
    @PostMapping("/{id}/signature-request")
    public ResponseEntity<SignatureRequestCreateResponse> createSignatureRequest(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) SignatureRequestCreateRequest request) {
        Integer minutes = request != null ? request.expiresInMinutes() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(signatureService.createRequest(id, minutes));
    }

    @PostMapping("/{id}/signature-request/revoke")
    public ResponseEntity<RapportinoDetailResponse> revokeSignatureRequest(@PathVariable UUID id) {
        signatureService.revoke(id);
        return ResponseEntity.ok(rapportinoService.getById(id));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or @securityService.isAdministrative(authentication)")
    public ResponseEntity<RapportinoDetailResponse> voidRapportino(@PathVariable UUID id) {
        return ResponseEntity.ok(rapportinoService.voidRapportino(id));
    }
}
