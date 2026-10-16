package br.com.mathinclui.resposta.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "respostas")
public class Resposta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tentativa_id", nullable = false)
    private Long tentativaId;

    @Column(name = "questao_id", nullable = false)
    private Long questaoId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resposta;

    @Column(nullable = false)
    private boolean correta;

    @Column(name = "quantidade_dicas", nullable = false)
    private short quantidadeDicas;

    @Column(name = "tempo_segundos", nullable = false)
    private int tempoSegundos;

    @Column(name = "respondida_em", nullable = false)
    private Instant respondidaEm;

    protected Resposta() {}

    public Resposta(Long tentativaId, Long questaoId, String resposta, boolean correta,
                    short quantidadeDicas, int tempoSegundos, Instant respondidaEm) {
        this.tentativaId = tentativaId;
        this.questaoId = questaoId;
        atualizar(resposta, correta, quantidadeDicas, tempoSegundos, respondidaEm);
    }

    public void atualizar(String resposta, boolean correta, short quantidadeDicas,
                          int tempoAcumuladoSegundos, Instant respondidaEm) {
        if (resposta == null || resposta.isBlank()) throw new IllegalArgumentException("Resposta é obrigatória.");
        if (quantidadeDicas < 0 || quantidadeDicas > 3 || tempoAcumuladoSegundos < 0 || respondidaEm == null) {
            throw new IllegalArgumentException("Estado de resposta inválido.");
        }
        this.resposta = resposta;
        this.correta = correta;
        this.quantidadeDicas = quantidadeDicas;
        this.tempoSegundos = tempoAcumuladoSegundos;
        this.respondidaEm = respondidaEm;
    }

    public Long getId() { return id; }
    public Long getTentativaId() { return tentativaId; }
    public Long getQuestaoId() { return questaoId; }
    public String getResposta() { return resposta; }
    public boolean isCorreta() { return correta; }
    public short getQuantidadeDicas() { return quantidadeDicas; }
    public int getTempoSegundos() { return tempoSegundos; }
    public Instant getRespondidaEm() { return respondidaEm; }
}
