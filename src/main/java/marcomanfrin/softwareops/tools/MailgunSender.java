package marcomanfrin.softwareops.tools;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import marcomanfrin.softwareops.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailgunSender {
    private final String domainName;
    private final String apiKey;
    private final String sender;

    public MailgunSender(@Value("${mailgun.domain}") String domainName,
                         @Value("${mailgun.apiKey}") String apiKey,
                         @Value("${mailgun.sender}") String sender) {
        this.domainName = domainName;
        this.apiKey = apiKey;
        this.sender = sender;
    }

    public void sendRegistrationEmail(User recipient) {
        HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + this.domainName + "/messages")
                .basicAuth("api", this.apiKey)
                .queryString("from", this.sender)
                .queryString("to", recipient.getEmail())
                .queryString("subject", "Benvenuto sulla piattaforma")
                .queryString("text", "Ciao, " + recipient.getFirstName() + ", la registrazione è avvenuta con successo!")
                .asJson();
    }


}