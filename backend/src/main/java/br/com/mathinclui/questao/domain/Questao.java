package br.com.mathinclui.questao.domain;

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
@Table(name = "questoes")
public class Questao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atividade_id", nullable = false)
    private Long atividadeId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoQuestao tipo;

    @Column(name = "resposta_correta", columnDefinition = "TEXT")
    private String respostaCorreta;

    @Column(columnDefinition = "TEXT")
    private String explicacao;

    @Column(name = "dica_1", columnDefinition = "TEXT")
    private String dica1;

    @Column(name = "dica_2", columnDefinition = "TEXT")
    private String dica2;

    @Column(name = "dica_3", columnDefinition = "TEXT")
    private String dica3;

    @Column(name = "imagem_url", columnDefinition = "TEXT")
    private String imagemUrl;

    @Column(nullable = false)
    private int ordem;

    @Column(name = "limite_tentativas")
    private Integer limiteTentativas;

    @Column(name = "origem_questao_modelo_id")
    private Long origemQuestaoModeloId;

    @Column(name = "copiado_em")
    private Instant copiadoEm;

    protected Questao() {
    }

    public Questao(Long atividadeId, String enunciado, TipoQuestao tipo, String respostaCorreta,
                   String explicacao, String dica1, String dica2, String dica3, String imagemUrl,
                   int ordem, Integer limiteTentativas) {
        validar(atividadeId, enunciado, tipo, respostaCorreta, ordem, limiteTentativas);
        this.atividadeId = atividadeId;
        aplicar(enunciado, tipo, respostaCorreta, explicacao, dica1, dica2, dica3, imagemUrl, ordem, limiteTentativas);
    }

    public void atualizar(String enunciado, TipoQuestao tipo, String respostaCorreta, String explicacao,
                          String dica1, String dica2, String dica3, String imagemUrl, int ordem,
                          Integer limiteTentativas) {
        validar(this.atividadeId, enunciado, tipo, respostaCorreta, ordem, limiteTentativas);
        aplicar(enunciado, tipo, respostaCorreta, explicacao, dica1, dica2, dica3, imagemUrl, ordem, limiteTentativas);
    }

    public void marcarOrigemModelo(Long origemQuestaoModeloId, Instant copiadoEm) {
        this.origemQuestaoModeloId = origemQuestaoModeloId;
        this.copiadoEm = copiadoEm;
    }

    public int limiteEfetivo(int limiteAtividade) {
        if (limiteAtividade < 1) throw new IllegalArgumentException("Limite de tentativas da atividade deve ser maior ou igual a 1.");
        return limiteTentativas == null ? limiteAtividade : limiteTentativas;
    }

    private void aplicar(String enunciado, TipoQuestao tipo, String respostaCorreta, String explicacao,
                         String dica1, String dica2, String dica3, String imagemUrl, int ordem,
                         Integer limiteTentativas) {
        this.enunciado = enunciado.trim();
        this.tipo = tipo;
        this.respostaCorreta = normalizarOpcional(respostaCorreta);
        this.explicacao = normalizarOpcional(explicacao);
        this.dica1 = normalizarOpcional(dica1);
        this.dica2 = normalizarOpcional(dica2);
        this.dica3 = normalizarOpcional(dica3);
        this.imagemUrl = normalizarOpcional(imagemUrl);
        this.ordem = ordem;
        this.limiteTentativas = limiteTentativas;
    }

    private void validar(Long atividadeId, String enunciado, TipoQuestao tipo, String respostaCorreta,
                         int ordem, Integer limiteTentativas) {
        if (atividadeId == null) throw new IllegalArgumentException("Atividade é obrigatória.");
        if (enunciado == null || enunciado.isBlank()) throw new IllegalArgumentException("Enunciado é obrigatório.");
        if (tipo == null) throw new IllegalArgumentException("Tipo da questão é obrigatório.");
        if (tipo != TipoQuestao.MULTIPLA_ESCOLHA && (respostaCorreta == null || respostaCorreta.isBlank())) {
            throw new IllegalArgumentException("Resposta correta é obrigatória para questão numérica ou de texto curto.");
        }
        if (ordem < 1) throw new IllegalArgumentException("Ordem deve ser maior ou igual a 1.");
        if (limiteTentativas != null && limiteTentativas < 1) {
            throw new IllegalArgumentException("Limite de tentativas deve ser maior ou igual a 1.");
        }
    }

    private String normalizarOpcional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public Long getAtividadeId() { return atividadeId; }
    public String getEnunciado() { return enunciado; }
    public TipoQuestao getTipo() { return tipo; }
    public String getRespostaCorreta() { return respostaCorreta; }
    public String getExplicacao() { return explicacao; }
    public String getDica1() { return dica1; }
    public String getDica2() { return dica2; }
    public String getDica3() { return dica3; }
    public String getImagemUrl() { return imagemUrl; }
    public int getOrdem() { return ordem; }
    public Integer getLimiteTentativas() { return limiteTentativas; }
    public Long getOrigemQuestaoModeloId() { return origemQuestaoModeloId; }
    public Instant getCopiadoEm() { return copiadoEm; }
}
