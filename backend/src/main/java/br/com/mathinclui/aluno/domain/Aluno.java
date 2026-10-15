package br.com.mathinclui.aluno.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "alunos")
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "turma_id", nullable = false)
    private Long turmaId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private Instant criadoEm;

    protected Aluno() {
    }

    public Aluno(Long turmaId, String nome, String username, String pinHash) {
        this.turmaId = turmaId;
        this.nome = nome.trim();
        this.username = username.trim().toLowerCase();
        this.pinHash = pinHash;
        this.ativo = true;
    }

    public void atualizar(String nome, String username) {
        this.nome = nome.trim();
        this.username = username.trim().toLowerCase();
    }

    public void alterarPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public Long getId() { return id; }
    public Long getTurmaId() { return turmaId; }
    public String getNome() { return nome; }
    public String getUsername() { return username; }
    public String getPinHash() { return pinHash; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
}
