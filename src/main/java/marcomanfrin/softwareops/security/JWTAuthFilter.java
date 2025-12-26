package marcomanfrin.softwareops.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.exceptions.UnauthorizedException;
import marcomanfrin.softwareops.services.UserService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
	private final JWTTools jwtTools;
	private final UserService userService;

    public JWTAuthFilter(JWTTools jwtTools, UserService userService) {
        this.jwtTools = jwtTools;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            // Se manca o è malformato -> 401 (MA SOLO se non sei su una rotta pubblica)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token mancante o non valido");
                return;
            }

            String token = authHeader.substring(7);

            jwtTools.verifyToken(token);

            UUID userId = jwtTools.getIDFromToken(token);

            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new UnauthorizedException("Utente non trovato"));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (UnauthorizedException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Problemi col token!");
        }
    }

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return new AntPathMatcher().match("/auth/**", request.getServletPath());
	}
}
