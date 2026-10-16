package br.com.mathinclui.atribuicao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "atribuicoes")
public class Atribuicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atividade_id", nullable = false)
    private Long atividadeId;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "turma_id")
    private Long turmaId;

    @Column(name = "aluno_id")
    private Long alunoId;

    @Column(name = "data_inicio", nullable = false)
    private Instant dataInicio;

    @Column(name = "data_limite")
    private Instant dataLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAtribuicao status = StatusAtribuicao.ATIVA;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private Instant criadoEm;

    protected Atribuicao() {}

    public Atribuicao(Long atividadeId, Long professorId, Long turmaId, Long alunoId,
                      Instant dataInicio, Instant dataLimite) {
        if (atividadeId == null || professorId == null) throw new IllegalArgumentException("Atividade e professor são obrigatórios.");
        if ((turmaId == null) == (alunoId == null)) throw new IllegalArgumentException("Informe exatamente uma turma ou um aluno.");
        if (dataInicio == null) throw new IllegalArgumentException("Data de início é obrigatória.");
        if (dataLimite != null && dataLimite.isBefore(dataInicio)) throw new IllegalArgumentException("Data limite não pode ser anterior ao início.");
        this.atividadeId = atividadeId;
        this.professorId = professorId;
        this.turmaId = turmaId;
        this.alunoId = alunoId;
        this.dataInicio = dataInicio;
        this.dataLimite = dataLimite;
        this.status = StatusAtribuicao.ATIVA;
    }

    public boolean destinadaAo(Long alunoId, Long turmaId) {
        return (this.alunoId != null && this.alunoId.equals(alunoId)) ||
                (this.turmaId != null && this.turmaId.equals(turmaId));
    }

    public boolean disponivelEm(Instant instante) {
        return status == StatusAtribuicao.ATIVA && !dataInicio.isAfter(instante) &&
                (dataLimite == null || !dataLimite.isBefore(instante));
    }

    public Long getId() { return id; }
    public Long getAtividadeId() { return atividadeId; }
    public Long getProfessorId() { return professorId; }
    public Long getTurmaId() { return turmaId; }
    public Long getAlunoId() { return alunoId; }
    public Instant getDataInicio() { return dataInicio; }
    public Instant getDataLimite() { return dataLimite; }
    public StatusAtribuicao getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
}
