package br.com.mathinclui.auth.professor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "professor_id", nullable = false) private Long professorId;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "consumed_at") private Instant consumedAt;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    protected PasswordResetToken() {}
    public PasswordResetToken(Long professorId, String tokenHash, Instant expiresAt, Instant criadoEm) { this.professorId = professorId; this.tokenHash = tokenHash; this.expiresAt = expiresAt; this.criadoEm = criadoEm; }
    public Long getProfessorId() { return professorId; }
    public boolean canBeUsedAt(Instant instant) { return consumedAt == null && instant.isBefore(expiresAt); }
    public void consume(Instant instant) { consumedAt = instant; }
}
