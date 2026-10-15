package br.com.mathinclui.aluno.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    private boolean ativo;
    protected Aluno() {}
    public Long getId() { return id; }
    public Long getTurmaId() { return turmaId; }
    public String getNome() { return nome; }
    public String getUsername() { return username; }
    public String getPinHash() { return pinHash; }
    public boolean isAtivo() { return ativo; }
}
