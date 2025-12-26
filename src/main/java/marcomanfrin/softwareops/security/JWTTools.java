package marcomanfrin.softwareops.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import marcomanfrin.softwareops.entities.User;

import java.util.Date;
import java.util.UUID;

@Component
public class JWTTools {

	private final String secret;
	public JWTTools(@Value("${jwt.secret}") String secret) {
		this.secret = secret;
	}

	public String createToken(User user) {
		return Jwts.builder()
				.issuedAt(new Date(System.currentTimeMillis())) // IAT (Issued At)
				.expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7 * 4)) // Expiration Date (4 weeks)
				.subject(String.valueOf(user.getId()))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes()))
				.compact();
	}

	public void verifyToken(String accessToken) {
		Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes())).build().parse(accessToken);
	}

	public UUID getIDFromToken(String accessToken) {
		return UUID.fromString(Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes())).build()
				.parseSignedClaims(accessToken)
				.getPayload()
				.getSubject());
	}
}
