package marcomanfrin.atixbackend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class SentryTestController {
    @GetMapping("/error")
    public void testError() {
        throw new RuntimeException("Test Sentry integration");
    }
}