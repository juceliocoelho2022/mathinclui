package br.com.mathinclui.turma.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "turmas")
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "ano_escolar", nullable = false)
    private short anoEscolar;

    @Column(nullable = false, length = 32)
    private String codigo;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private Instant criadoEm;

    protected Turma() {
    }

    public Turma(Long professorId, String nome, short anoEscolar, String codigo) {
        this.professorId = professorId;
        this.nome = nome.trim();
        this.anoEscolar = anoEscolar;
        this.codigo = codigo.trim();
        this.ativa = true;
    }

    public void atualizar(String nome, short anoEscolar, String codigo) {
        this.nome = nome.trim();
        this.anoEscolar = anoEscolar;
        this.codigo = codigo.trim();
    }

    public void desativar() {
        this.ativa = false;
    }

    public Long getId() { return id; }
    public Long getProfessorId() { return professorId; }
    public String getNome() { return nome; }
    public short getAnoEscolar() { return anoEscolar; }
    public String getCodigo() { return codigo; }
    public boolean isAtiva() { return ativa; }
    public Instant getCriadoEm() { return criadoEm; }
}
