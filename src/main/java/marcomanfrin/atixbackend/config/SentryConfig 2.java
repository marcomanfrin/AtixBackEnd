package marcomanfrin.atixbackend.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;

@Configuration
public class SentryConfig {

    @Value("${sentry.dsn}")
    private String dsn;

    @Value("${sentry.environment:development}")
    private String environment;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @Value("${sentry.traces-sample-rate:1.0}")
    private Double tracesSampleRate;

    @PostConstruct
    public void init() {
        // Manual Sentry initialization to avoid Spring Boot autoconfiguration issues
        SentryOptions options = new SentryOptions();
        options.setDsn(dsn);
        options.setEnvironment(environment);
        options.setTracesSampleRate(tracesSampleRate);
        options.setEnableTracing(true);
        options.setSendDefaultPii(false);
        options.setAttachStacktrace(true);
        options.setAttachThreads(true);

        // Add before send callback
        options.setBeforeSend((event, hint) -> {
            event.setServerName(applicationName);
            event.setTag("server_port", serverPort);
            event.setTag("application", applicationName);
            event.setTag("environment", environment);
            return event;
        });

        Sentry.init(options);
    }
}
