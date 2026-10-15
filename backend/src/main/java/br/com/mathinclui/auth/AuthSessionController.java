package br.com.mathinclui.auth;

import br.com.mathinclui.auth.api.AuthResponse;
import br.com.mathinclui.auth.api.SessionUser;
import br.com.mathinclui.auth.token.RefreshRequest;
import br.com.mathinclui.auth.token.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthSessionController {
    private final RefreshTokenService refreshTokenService;
    public AuthSessionController(RefreshTokenService refreshTokenService) { this.refreshTokenService = refreshTokenService; }
    @PostMapping("/refresh") AuthResponse refresh(@Valid @RequestBody RefreshRequest request) { return refreshTokenService.refresh(request.refreshToken()); }
    @PostMapping("/logout") ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) { refreshTokenService.logout(request.refreshToken()); return ResponseEntity.noContent().build(); }
    @GetMapping("/me") SessionUser me(JwtAuthenticationToken authentication) {
        var jwt = authentication.getToken();
        var subject = jwt.getSubject();
        var id = Long.parseLong(subject.substring(subject.indexOf(':') + 1));
        return new SessionUser(id, jwt.getClaimAsString("name"), jwt.getClaimAsString("login"), jwt.getClaimAsString("subject_type"), jwt.getClaimAsString("role"));
    }
}
