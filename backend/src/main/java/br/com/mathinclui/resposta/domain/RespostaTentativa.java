package br.com.mathinclui.resposta.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "resposta_tentativas")
public class RespostaTentativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tentativa_id", nullable = false)
    private Long tentativaId;

    @Column(name = "questao_id", nullable = false)
    private Long questaoId;

    @Column(name = "numero_tentativa", nullable = false)
    private int numeroTentativa;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resposta;

    @Column(nullable = false)
    private boolean correta;

    @Column(name = "dica_liberada", nullable = false)
    private short dicaLiberada;

    @Column(name = "tempo_segundos", nullable = false)
    private int tempoSegundos;

    @Column(name = "respondida_em", nullable = false)
    private Instant respondidaEm;

    protected RespostaTentativa() {}

    public RespostaTentativa(Long tentativaId, Long questaoId, int numeroTentativa, String resposta,
                             boolean correta, short dicaLiberada, int tempoSegundos, Instant respondidaEm) {
        if (tentativaId == null || questaoId == null || numeroTentativa < 1 || resposta == null || resposta.isBlank()
                || dicaLiberada < 0 || dicaLiberada > 3 || tempoSegundos < 0 || respondidaEm == null) {
            throw new IllegalArgumentException("Histórico de resposta inválido.");
        }
        this.tentativaId = tentativaId;
        this.questaoId = questaoId;
        this.numeroTentativa = numeroTentativa;
        this.resposta = resposta;
        this.correta = correta;
        this.dicaLiberada = dicaLiberada;
        this.tempoSegundos = tempoSegundos;
        this.respondidaEm = respondidaEm;
    }

    public Long getId() { return id; }
    public Long getTentativaId() { return tentativaId; }
    public Long getQuestaoId() { return questaoId; }
    public int getNumeroTentativa() { return numeroTentativa; }
    public String getResposta() { return resposta; }
    public boolean isCorreta() { return correta; }
    public short getDicaLiberada() { return dicaLiberada; }
    public int getTempoSegundos() { return tempoSegundos; }
    public Instant getRespondidaEm() { return respondidaEm; }
}
