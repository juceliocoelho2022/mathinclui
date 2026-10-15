package br.com.mathinclui.auth.aluno;

import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.auth.api.AuthProblemException;
import br.com.mathinclui.auth.api.AuthResponse;
import br.com.mathinclui.auth.security.AuthPrincipal;
import br.com.mathinclui.auth.security.SubjectType;
import br.com.mathinclui.auth.token.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AlunoAuthService {
    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    public AlunoAuthService(AlunoRepository alunoRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) { this.alunoRepository = alunoRepository; this.passwordEncoder = passwordEncoder; this.refreshTokenService = refreshTokenService; }
    @Transactional public AuthResponse login(AlunoLoginRequest request) {
        var username = request.username().trim().toLowerCase(Locale.ROOT);
        var aluno = alunoRepository.findByUsernameIgnoreCase(username).filter(Aluno::isAtivo).orElseThrow(AuthProblemException::invalidCredentials);
        if (!passwordEncoder.matches(request.pin(), aluno.getPinHash())) throw AuthProblemException.invalidCredentials();
        var principal = new AuthPrincipal(aluno.getId(), aluno.getNome(), aluno.getUsername(), SubjectType.ALUNO);
        return refreshTokenService.issuePair(principal);
    }
}
