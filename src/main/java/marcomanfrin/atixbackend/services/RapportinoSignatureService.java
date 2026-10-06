package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.rapportini.PublicRapportinoPreviewResponse;
import marcomanfrin.atixbackend.DTO.rapportini.RapportinoSignRequest;
import marcomanfrin.atixbackend.DTO.rapportini.SignatureRequestCreateResponse;
import marcomanfrin.atixbackend.entities.Rapportino;
import marcomanfrin.atixbackend.entities.RapportinoSignatureRequest;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.enums.SignatureSource;
import marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException;
import marcomanfrin.atixbackend.exceptions.InvalidWorkflowTransitionException;
import marcomanfrin.atixbackend.repositories.RapportinoRepository;
import marcomanfrin.atixbackend.repositories.RapportinoSignatureRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

// Firma remota: token casuale a 256 bit consegnato una sola volta, a DB solo lo SHA-256.
// Scadenza, uso singolo e revoca sono verificati lato server a ogni chiamata.
@Service
public class RapportinoSignatureService {

    static final int DEFAULT_EXPIRY_MINUTES = 60;
    static final int MAX_EXPIRY_MINUTES = 24 * 60;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RapportinoService rapportinoService;
    private final RapportinoRepository rapportinoRepository;
    private final RapportinoSignatureRequestRepository requestRepository;
    private final RapportinoStateMachine stateMachine;

    public RapportinoSignatureService(RapportinoService rapportinoService,
                                      RapportinoRepository rapportinoRepository,
                                      RapportinoSignatureRequestRepository requestRepository,
                                      RapportinoStateMachine stateMachine) {
        this.rapportinoService = rapportinoService;
        this.rapportinoRepository = rapportinoRepository;
        this.requestRepository = requestRepository;
        this.stateMachine = stateMachine;
    }

    // ---------- Authenticated (tecnico) ----------

    @Transactional
    public SignatureRequestCreateResponse createRequest(UUID rapportinoId, Integer expiresInMinutes) {
        rapportinoService.releaseExpiredSignatureRequests();
        User currentUser = rapportinoService.currentUser();
        Rapportino rapportino = rapportinoService.getVisible(rapportinoId, currentUser);
        if (rapportino.getStatus() != RapportinoStatus.DRAFT) {
            throw new InvalidWorkflowTransitionException("A signature can be requested only for a DRAFT rapportino (current status: "
                    + rapportino.getStatus() + ")");
        }
        stateMachine.validateTransition(rapportino.getStatus(), RapportinoStatus.AWAITING_SIGNATURE);

        int minutes = clampExpiry(expiresInMinutes);
        String token = newToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutes);
        requestRepository.save(new RapportinoSignatureRequest(rapportino, hash(token), expiresAt, currentUser));

        rapportino.setStatus(RapportinoStatus.AWAITING_SIGNATURE);
        rapportinoRepository.save(rapportino);

        return new SignatureRequestCreateResponse(token, expiresAt);
    }

    @Transactional
    public void revoke(UUID rapportinoId) {
        Rapportino rapportino = rapportinoService.getVisible(rapportinoId, rapportinoService.currentUser());
        if (rapportino.getStatus() != RapportinoStatus.AWAITING_SIGNATURE) {
            throw new InvalidWorkflowTransitionException("Rapportino " + rapportino.getNumber()
                    + " has no outstanding signature request");
        }
        LocalDateTime now = LocalDateTime.now();
        for (RapportinoSignatureRequest request : requestRepository.findByRapportinoIdAndUsedAtIsNullAndRevokedAtIsNull(rapportinoId)) {
            request.setRevokedAt(now);
            requestRepository.save(request);
        }
        stateMachine.validateTransition(rapportino.getStatus(), RapportinoStatus.DRAFT);
        rapportino.setStatus(RapportinoStatus.DRAFT);
        rapportinoRepository.save(rapportino);
    }

    // ---------- Public (cliente, senza autenticazione) ----------

    @Transactional(readOnly = true)
    public PublicRapportinoPreviewResponse preview(String token) {
        RapportinoSignatureRequest request = requestRepository.findByTokenHash(hash(token))
                .orElseThrow(InvalidSigningTokenException::new);
        assertUsable(request);
        return toPreview(request.getRapportino(), request.getExpiresAt());
    }

    @Transactional
    public void sign(String token, RapportinoSignRequest signRequest, String signerIp, String signerUserAgent) {
        RapportinoSignatureRequest request = requestRepository.findByTokenHashForUpdate(hash(token))
                .orElseThrow(InvalidSigningTokenException::new);
        assertUsable(request);

        // Validazione prima di consumare il token: un errore del cliente non brucia il link
        RapportinoService.validateSignRequest(signRequest);

        String ip = truncate(signerIp, 64);
        String userAgent = truncate(signerUserAgent, 512);
        request.setUsedAt(LocalDateTime.now());
        request.setSignerIp(ip);
        request.setSignerUserAgent(userAgent);
        requestRepository.save(request);

        rapportinoService.applySignature(request.getRapportino(), signRequest, SignatureSource.REMOTE, ip, userAgent);
    }

    // ---------- Internals ----------

    private static void assertUsable(RapportinoSignatureRequest request) {
        if (!request.isUsable(LocalDateTime.now())
                || request.getRapportino().getStatus() != RapportinoStatus.AWAITING_SIGNATURE) {
            throw new InvalidSigningTokenException();
        }
    }

    static int clampExpiry(Integer requestedMinutes) {
        if (requestedMinutes == null || requestedMinutes < 1) {
            return DEFAULT_EXPIRY_MINUTES;
        }
        return Math.min(requestedMinutes, MAX_EXPIRY_MINUTES);
    }

    static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    (token == null ? "" : token).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    // Solo cio' che serve al cliente per firmare: niente id, niente contatti del tecnico, niente altre commesse
    private static PublicRapportinoPreviewResponse toPreview(Rapportino r, LocalDateTime expiresAt) {
        return new PublicRapportinoPreviewResponse(
                r.getNumber(),
                r.getInterventionDate(),
                r.getLocale(),
                r.getClientName(),
                r.getPlantLabel(),
                r.getDescription(),
                r.getChecklistAnswers().stream()
                        .map(a -> new PublicRapportinoPreviewResponse.Item(a.getLabelSnapshot(), a.isChecked(), a.getNote()))
                        .toList(),
                r.getMaterials().stream()
                        .map(m -> new PublicRapportinoPreviewResponse.Material(m.getDescription(), m.getQuantity()))
                        .toList(),
                r.getWorkHours(),
                r.getTravelHours(),
                r.getTotalHours(),
                expiresAt
        );
    }
}
