package br.com.mathinclui.auth.aluno;

import br.com.mathinclui.auth.api.AuthProblemException;
import br.com.mathinclui.auth.api.AuthResponse;
import br.com.mathinclui.auth.security.LoginAttemptTracker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth/aluno")
public class AlunoAuthController {
    private final AlunoAuthService service;
    private final LoginAttemptTracker loginAttemptTracker;
    public AlunoAuthController(AlunoAuthService service, LoginAttemptTracker loginAttemptTracker) { this.service = service; this.loginAttemptTracker = loginAttemptTracker; }
    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody AlunoLoginRequest request, HttpServletRequest httpRequest) {
        var key = "aluno:" + httpRequest.getRemoteAddr() + ":" + request.username().trim().toLowerCase(Locale.ROOT);
        if (loginAttemptTracker.isBlocked(key)) throw new AuthProblemException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas de login. Aguarde antes de tentar novamente.");
        try { var response = service.login(request); loginAttemptTracker.recordSuccess(key); return response; }
        catch (AuthProblemException exception) { loginAttemptTracker.recordFailure(key); throw exception; }
    }
}
