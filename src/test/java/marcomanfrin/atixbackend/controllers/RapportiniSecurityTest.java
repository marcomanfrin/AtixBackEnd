package marcomanfrin.atixbackend.controllers;

import marcomanfrin.atixbackend.DTO.rapportini.PublicRapportinoPreviewResponse;
import marcomanfrin.atixbackend.exceptions.InvalidSigningTokenException;
import marcomanfrin.atixbackend.security.JWTAuthFilter;
import marcomanfrin.atixbackend.security.JWTTools;
import marcomanfrin.atixbackend.security.PublicRateLimiter;
import marcomanfrin.atixbackend.security.SecurityConfig;
import marcomanfrin.atixbackend.services.RapportinoService;
import marcomanfrin.atixbackend.services.RapportinoSignatureService;
import marcomanfrin.atixbackend.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Superficie HTTP: solo /public/** e' anonimo, tutto il resto dei rapportini richiede il JWT
@WebMvcTest(controllers = {RapportiniController.class, PublicSignatureController.class})
@Import({SecurityConfig.class, JWTAuthFilter.class, PublicRateLimiter.class})
@TestPropertySource(properties = {
        "cors.allowed-origins=http://localhost:8888",
        "public.rate-limit.max-requests=5",
        "public.rate-limit.window-seconds=600"
})
class RapportiniSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired PublicRateLimiter rateLimiter;

    @MockitoBean RapportinoService rapportinoService;
    @MockitoBean RapportinoSignatureService signatureService;
    @MockitoBean JWTTools jwtTools;
    @MockitoBean UserService userService;

    // In Tomcat il servletPath e' il path della richiesta (JWTAuthFilter.shouldNotFilter lo usa); MockMvc lo lascia vuoto
    private static org.springframework.test.web.servlet.request.RequestPostProcessor from(String remoteAddr) {
        return r -> {
            r.setServletPath(r.getRequestURI());
            r.setRemoteAddr(remoteAddr);
            return r;
        };
    }

    private static final String SIGN_BODY = "{\"signerName\":\"Giulia\",\"signatureImage\":\"data:image/png;base64,xx\",\"privacyAccepted\":true}";

    @Test
    void publicPreviewNeedsNoCredentials() throws Exception {
        when(signatureService.preview("good")).thenReturn(new PublicRapportinoPreviewResponse(
                "RFL-2026-0001", LocalDate.now(), "it", "Cliente", "Impianto", "desc",
                List.of(), List.of(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, LocalDateTime.now().plusHours(1)));

        mvc.perform(get("/public/rapportini/sign/good").with(from("198.51.100.1")))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.number").value("RFL-2026-0001"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void publicSignNeedsNoCredentialsAndPassesClientIpAndUserAgent() throws Exception {
        mvc.perform(post("/public/rapportini/sign/good").contentType(MediaType.APPLICATION_JSON).content(SIGN_BODY)
                        .header("User-Agent", "UA-test")
                        .with(from("198.51.100.2")))
                .andExpect(status().isOk());
        verify(signatureService).sign(eq("good"), any(), eq("198.51.100.2"), eq("UA-test"));
    }

    @Test
    void forwardedIpIsTrustedOnlyFromTheProxy() throws Exception {
        // dall'esterno: X-Real-IP ignorato
        mvc.perform(post("/public/rapportini/sign/a").contentType(MediaType.APPLICATION_JSON).content(SIGN_BODY)
                .header("X-Real-IP", "1.2.3.4").with(from("198.51.100.3")));
        verify(signatureService).sign(eq("a"), any(), eq("198.51.100.3"), any());

        // da nginx (rete privata): X-Real-IP usato
        mvc.perform(post("/public/rapportini/sign/b").contentType(MediaType.APPLICATION_JSON).content(SIGN_BODY)
                .header("X-Real-IP", "1.2.3.4").with(from("172.18.0.5")));
        verify(signatureService).sign(eq("b"), any(), eq("1.2.3.4"), any());
    }

    @Test
    void invalidTokensProduceIdenticalResponses() throws Exception {
        when(signatureService.preview(anyString())).thenThrow(new InvalidSigningTokenException());

        MvcResult first = mvc.perform(get("/public/rapportini/sign/unknown").with(from("198.51.100.4")))
                .andExpect(status().isNotFound()).andReturn();
        MvcResult second = mvc.perform(get("/public/rapportini/sign/expired").with(from("198.51.100.4")))
                .andExpect(status().isNotFound()).andReturn();

        assertEquals(first.getResponse().getContentAsString(), second.getResponse().getContentAsString());
        assertFalse(first.getResponse().getContentAsString().contains("timestamp"));
    }

    @Test
    void publicEndpointsAreRateLimitedPerIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(get("/public/rapportini/sign/t").with(from("198.51.100.9")))
                    .andExpect(status().is(not429()));
        }
        mvc.perform(get("/public/rapportini/sign/t").with(from("198.51.100.9")))
                .andExpect(status().isTooManyRequests());
        // un altro IP non e' toccato
        mvc.perform(get("/public/rapportini/sign/t").with(from("198.51.100.10")))
                .andExpect(status().is(not429()));
    }

    private static org.hamcrest.Matcher<Integer> not429() {
        return org.hamcrest.Matchers.not(429);
    }

    @Test
    void everyAuthenticatedRapportinoEndpointRejectsAnonymousCalls() throws Exception {
        String id = UUID.randomUUID().toString();
        var calls = List.of(
                get("/rapportini"),
                get("/rapportini/checklist-template"),
                get("/rapportini/" + id),
                get("/rapportini/" + id + "/pdf"),
                post("/rapportini").contentType(MediaType.APPLICATION_JSON).content("{}"),
                patch("/rapportini/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"),
                delete("/rapportini/" + id),
                post("/rapportini/" + id + "/sign").contentType(MediaType.APPLICATION_JSON).content(SIGN_BODY),
                post("/rapportini/" + id + "/signature-request"),
                post("/rapportini/" + id + "/signature-request/revoke"),
                post("/rapportini/" + id + "/void")
        );
        for (var call : calls) {
            mvc.perform(call.with(from("198.51.100.20"))).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(rapportinoService);
    }

    @Test
    void publicMatcherDoesNotOpenOtherPaths() throws Exception {
        for (String path : List.of("/works", "/attachments/WORK/" + UUID.randomUUID(), "/users", "/public-anything", "/rapportini/public")) {
            mvc.perform(get(path).with(from("198.51.100.21"))).andExpect(status().isUnauthorized());
        }
    }
}
