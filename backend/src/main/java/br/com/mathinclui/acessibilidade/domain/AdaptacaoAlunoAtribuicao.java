package br.com.mathinclui.acessibilidade.domain;

import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import jakarta.persistence.*;

@Entity
@Table(name = "adaptacoes_aluno_atribuicao")
public class AdaptacaoAlunoAtribuicao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "atribuicao_id", nullable = false) private Long atribuicaoId;
    @Column(name = "aluno_id", nullable = false) private Long alunoId;
    @Enumerated(EnumType.STRING) @Column(name = "tamanho_fonte_minima", length = 20) private TamanhoFonte tamanhoFonteMinima;
    @Column(name = "leitura_voz_minima") private Boolean leituraVozMinima;
    @Column(name = "apoio_visual_minimo") private Boolean apoioVisualMinimo;
    @Column(name = "dicas_passo_a_passo") private Boolean dicasPassoAPasso;
    @Column(name = "repeticao_guiada") private Boolean repeticaoGuiada;
    @Column(name = "questoes_por_tela") private Short questoesPorTela;
    @Column(name = "tempo_ampliado") private Boolean tempoAmpliado;
    @Enumerated(EnumType.STRING) @Column(name = "nivel_apoio", length = 20) private NivelApoio nivelApoio;

    protected AdaptacaoAlunoAtribuicao() {}

    public AdaptacaoAlunoAtribuicao(Long atribuicaoId, Long alunoId, TamanhoFonte tamanhoFonteMinima,
                                    Boolean leituraVozMinima, Boolean apoioVisualMinimo, Boolean dicasPassoAPasso,
                                    Boolean repeticaoGuiada, Short questoesPorTela, Boolean tempoAmpliado,
                                    NivelApoio nivelApoio) {
        if (atribuicaoId == null || alunoId == null) throw new IllegalArgumentException("Atribuição e aluno são obrigatórios.");
        this.atribuicaoId = atribuicaoId;
        this.alunoId = alunoId;
        atualizar(tamanhoFonteMinima, leituraVozMinima, apoioVisualMinimo, dicasPassoAPasso,
                repeticaoGuiada, questoesPorTela, tempoAmpliado, nivelApoio);
    }

    public void atualizar(TamanhoFonte tamanhoFonteMinima, Boolean leituraVozMinima, Boolean apoioVisualMinimo,
                          Boolean dicasPassoAPasso, Boolean repeticaoGuiada, Short questoesPorTela,
                          Boolean tempoAmpliado, NivelApoio nivelApoio) {
        if (questoesPorTela != null && (questoesPorTela < 1 || questoesPorTela > 10))
            throw new IllegalArgumentException("Questões por tela deve estar entre 1 e 10.");
        this.tamanhoFonteMinima = tamanhoFonteMinima;
        this.leituraVozMinima = leituraVozMinima;
        this.apoioVisualMinimo = apoioVisualMinimo;
        this.dicasPassoAPasso = dicasPassoAPasso;
        this.repeticaoGuiada = repeticaoGuiada;
        this.questoesPorTela = questoesPorTela;
        this.tempoAmpliado = tempoAmpliado;
        this.nivelApoio = nivelApoio;
    }

    public Long getId() { return id; }
    public Long getAtribuicaoId() { return atribuicaoId; }
    public Long getAlunoId() { return alunoId; }
    public TamanhoFonte getTamanhoFonteMinima() { return tamanhoFonteMinima; }
    public Boolean getLeituraVozMinima() { return leituraVozMinima; }
    public Boolean getApoioVisualMinimo() { return apoioVisualMinimo; }
    public Boolean getDicasPassoAPasso() { return dicasPassoAPasso; }
    public Boolean getRepeticaoGuiada() { return repeticaoGuiada; }
    public Short getQuestoesPorTela() { return questoesPorTela; }
    public Boolean getTempoAmpliado() { return tempoAmpliado; }
    public NivelApoio getNivelApoio() { return nivelApoio; }
}
