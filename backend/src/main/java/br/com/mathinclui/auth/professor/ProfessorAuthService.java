package br.com.mathinclui.auth.professor;

import br.com.mathinclui.auth.api.AuthProblemException;
import br.com.mathinclui.auth.api.AuthResponse;
import br.com.mathinclui.auth.security.AuthPrincipal;
import br.com.mathinclui.auth.security.SubjectType;
import br.com.mathinclui.auth.token.RefreshTokenService;
import br.com.mathinclui.professor.domain.Professor;
import br.com.mathinclui.professor.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

@Service
public class ProfessorAuthService {
    private final ProfessorRepository professorRepository; private final PasswordEncoder passwordEncoder; private final RefreshTokenService refreshTokenService; private final PasswordResetTokenRepository passwordResetTokenRepository; private final PasswordResetNotifier passwordResetNotifier; private final SecureResetTokenCodec resetTokenCodec; private final Clock clock; private final Duration resetTtl;
    public ProfessorAuthService(ProfessorRepository professorRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, PasswordResetTokenRepository passwordResetTokenRepository, PasswordResetNotifier passwordResetNotifier, Clock clock, @Value("${app.security.password-reset.ttl-seconds:1800}") long resetTtlSeconds) {
        this.professorRepository=professorRepository; this.passwordEncoder=passwordEncoder; this.refreshTokenService=refreshTokenService; this.passwordResetTokenRepository=passwordResetTokenRepository; this.passwordResetNotifier=passwordResetNotifier; this.resetTokenCodec=new SecureResetTokenCodec(); this.clock=clock; this.resetTtl=Duration.ofSeconds(resetTtlSeconds);
    }
    @Transactional public AuthResponse register(RegisterProfessorRequest request) {
        var email=normalizeEmail(request.email()); if (professorRepository.existsByEmailIgnoreCase(email)) throw new AuthProblemException(HttpStatus.CONFLICT,"Já existe um professor cadastrado com este e-mail.");
        final Professor professor; try { professor=professorRepository.saveAndFlush(new Professor(request.nome().trim(),email,passwordEncoder.encode(request.password()))); } catch (DataIntegrityViolationException exception) { throw new AuthProblemException(HttpStatus.CONFLICT,"Já existe um professor cadastrado com este e-mail."); }
        return refreshTokenService.issuePair(principal(professor));
    }
    @Transactional public AuthResponse login(ProfessorLoginRequest request) {
        var professor=professorRepository.findByEmailIgnoreCase(normalizeEmail(request.email())).filter(Professor::isAtivo).orElseThrow(AuthProblemException::invalidCredentials); if (!passwordEncoder.matches(request.password(),professor.getSenhaHash())) throw AuthProblemException.invalidCredentials(); return refreshTokenService.issuePair(principal(professor));
    }
    @Transactional public void requestPasswordReset(String rawEmail) {
        var email=normalizeEmail(rawEmail); professorRepository.findByEmailIgnoreCase(email).filter(Professor::isAtivo).ifPresent(professor->{ var rawToken=resetTokenCodec.generate(); var now=clock.instant(); passwordResetTokenRepository.save(new PasswordResetToken(professor.getId(),resetTokenCodec.hash(rawToken),now.plus(resetTtl),now)); passwordResetNotifier.notifyReset(email,rawToken); });
    }
    @Transactional public void resetPassword(ResetPasswordRequest request) {
        var now=clock.instant(); var token=passwordResetTokenRepository.findByTokenHash(resetTokenCodec.hash(request.token())).filter(candidate->candidate.canBeUsedAt(now)).orElseThrow(()->new AuthProblemException(HttpStatus.BAD_REQUEST,"Token de recuperação inválido ou expirado.")); var professor=professorRepository.findById(token.getProfessorId()).filter(Professor::isAtivo).orElseThrow(()->new AuthProblemException(HttpStatus.BAD_REQUEST,"Token de recuperação inválido ou expirado.")); professor.alterarSenhaHash(passwordEncoder.encode(request.newPassword())); token.consume(now); refreshTokenService.revokeAll(SubjectType.PROFESSOR,professor.getId());
    }
    private AuthPrincipal principal(Professor professor){ return new AuthPrincipal(professor.getId(),professor.getNome(),professor.getEmail(),SubjectType.PROFESSOR); }
    private String normalizeEmail(String email){ return email.trim().toLowerCase(Locale.ROOT); }
}
