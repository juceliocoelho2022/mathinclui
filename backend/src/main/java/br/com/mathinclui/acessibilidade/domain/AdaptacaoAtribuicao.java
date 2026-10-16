package br.com.mathinclui.acessibilidade.domain;

import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "adaptacoes_atribuicao")
public class AdaptacaoAtribuicao {
    @Id
    @Column(name = "atribuicao_id")
    private Long atribuicaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tamanho_fonte_minima", length = 20)
    private TamanhoFonte tamanhoFonteMinima;

    @Column(name = "leitura_voz_minima")
    private Boolean leituraVozMinima;

    @Column(name = "apoio_visual_minimo")
    private Boolean apoioVisualMinimo;

    @Column(name = "dicas_passo_a_passo")
    private Boolean dicasPassoAPasso;

    @Column(name = "repeticao_guiada")
    private Boolean repeticaoGuiada;

    @Column(name = "questoes_por_tela")
    private Short questoesPorTela;

    @Column(name = "tempo_ampliado")
    private Boolean tempoAmpliado;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_apoio", length = 20)
    private NivelApoio nivelApoio;

    @Column(name = "atualizado_em", nullable = false, insertable = false, updatable = false)
    private Instant atualizadoEm;

    protected AdaptacaoAtribuicao() {}

    public AdaptacaoAtribuicao(Long atribuicaoId) {
        if (atribuicaoId == null) throw new IllegalArgumentException("Atribuição é obrigatória.");
        this.atribuicaoId = atribuicaoId;
    }

    public void atualizar(TamanhoFonte tamanhoFonteMinima, Boolean leituraVozMinima, Boolean apoioVisualMinimo,
                          Boolean dicasPassoAPasso, Boolean repeticaoGuiada, Short questoesPorTela,
                          Boolean tempoAmpliado, NivelApoio nivelApoio) {
        if (questoesPorTela != null && (questoesPorTela < 1 || questoesPorTela > 10)) {
            throw new IllegalArgumentException("Questões por tela deve ficar entre 1 e 10.");
        }
        this.tamanhoFonteMinima = tamanhoFonteMinima;
        this.leituraVozMinima = leituraVozMinima;
        this.apoioVisualMinimo = apoioVisualMinimo;
        this.dicasPassoAPasso = dicasPassoAPasso;
        this.repeticaoGuiada = repeticaoGuiada;
        this.questoesPorTela = questoesPorTela;
        this.tempoAmpliado = tempoAmpliado;
        this.nivelApoio = nivelApoio;
    }

    public Long getAtribuicaoId() { return atribuicaoId; }
    public TamanhoFonte getTamanhoFonteMinima() { return tamanhoFonteMinima; }
    public Boolean getLeituraVozMinima() { return leituraVozMinima; }
    public Boolean getApoioVisualMinimo() { return apoioVisualMinimo; }
    public Boolean getDicasPassoAPasso() { return dicasPassoAPasso; }
    public Boolean getRepeticaoGuiada() { return repeticaoGuiada; }
    public Short getQuestoesPorTela() { return questoesPorTela; }
    public Boolean getTempoAmpliado() { return tempoAmpliado; }
    public NivelApoio getNivelApoio() { return nivelApoio; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
