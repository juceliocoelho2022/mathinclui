package br.com.mathinclui.tentativa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tentativas")
public class Tentativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atribuicao_id", nullable = false)
    private Long atribuicaoId;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "iniciada_em", nullable = false)
    private Instant iniciadaEm;

    @Column(name = "finalizada_em")
    private Instant finalizadaEm;

    @Column(name = "quantidade_acertos", nullable = false)
    private int quantidadeAcertos;

    @Column(name = "quantidade_erros", nullable = false)
    private int quantidadeErros;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentual;

    @Column(name = "tempo_total_segundos", nullable = false)
    private int tempoTotalSegundos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTentativa status = StatusTentativa.EM_ANDAMENTO;

    protected Tentativa() {}

    public Tentativa(Long atribuicaoId, Long alunoId, Instant iniciadaEm) {
        if (atribuicaoId == null || alunoId == null || iniciadaEm == null) {
            throw new IllegalArgumentException("Atribuição, aluno e início são obrigatórios.");
        }
        this.atribuicaoId = atribuicaoId;
        this.alunoId = alunoId;
        this.iniciadaEm = iniciadaEm;
        this.status = StatusTentativa.EM_ANDAMENTO;
    }

    public void finalizar(int acertos, int erros, BigDecimal percentual, int tempoTotalSegundos, Instant finalizadaEm) {
        if (status == StatusTentativa.FINALIZADA) return;
        if (acertos < 0 || erros < 0 || tempoTotalSegundos < 0 || percentual == null || finalizadaEm == null) {
            throw new IllegalArgumentException("Resultado final inválido.");
        }
        this.quantidadeAcertos = acertos;
        this.quantidadeErros = erros;
        this.percentual = percentual;
        this.tempoTotalSegundos = tempoTotalSegundos;
        this.finalizadaEm = finalizadaEm;
        this.status = StatusTentativa.FINALIZADA;
    }

    public Long getId() { return id; }
    public Long getAtribuicaoId() { return atribuicaoId; }
    public Long getAlunoId() { return alunoId; }
    public Instant getIniciadaEm() { return iniciadaEm; }
    public Instant getFinalizadaEm() { return finalizadaEm; }
    public int getQuantidadeAcertos() { return quantidadeAcertos; }
    public int getQuantidadeErros() { return quantidadeErros; }
    public BigDecimal getPercentual() { return percentual; }
    public int getTempoTotalSegundos() { return tempoTotalSegundos; }
    public StatusTentativa getStatus() { return status; }
    public boolean isFinalizada() { return status == StatusTentativa.FINALIZADA; }
}
