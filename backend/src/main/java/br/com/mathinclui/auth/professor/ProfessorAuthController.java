package br.com.mathinclui.auth.professor;

import br.com.mathinclui.auth.api.AuthProblemException;
import br.com.mathinclui.auth.api.AuthResponse;
import br.com.mathinclui.auth.security.LoginAttemptTracker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/professor")
public class ProfessorAuthController {
    private final ProfessorAuthService service;
    private final LoginAttemptTracker loginAttemptTracker;
    public ProfessorAuthController(ProfessorAuthService service, LoginAttemptTracker loginAttemptTracker) { this.service = service; this.loginAttemptTracker = loginAttemptTracker; }
    @PostMapping("/register") ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterProfessorRequest request) { return ResponseEntity.created(URI.create("/api/v1/auth/me")).body(service.register(request)); }
    @PostMapping("/login") AuthResponse login(@Valid @RequestBody ProfessorLoginRequest request, HttpServletRequest httpRequest) {
        var key = "professor:" + httpRequest.getRemoteAddr() + ":" + request.email().trim().toLowerCase(Locale.ROOT);
        rejectIfBlocked(key);
        try { var response = service.login(request); loginAttemptTracker.recordSuccess(key); return response; }
        catch (AuthProblemException exception) { loginAttemptTracker.recordFailure(key); throw exception; }
    }
    @PostMapping("/forgot-password") ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) { service.requestPasswordReset(request.email()); return ResponseEntity.accepted().body(Map.of("message", "Se a conta existir, as instruções de recuperação serão enviadas.")); }
    @PostMapping("/reset-password") ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) { service.resetPassword(request); return ResponseEntity.noContent().build(); }
    private void rejectIfBlocked(String key) { if (loginAttemptTracker.isBlocked(key)) throw new AuthProblemException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas de login. Aguarde antes de tentar novamente."); }
}
