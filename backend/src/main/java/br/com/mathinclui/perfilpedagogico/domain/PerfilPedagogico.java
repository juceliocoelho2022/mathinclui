package br.com.mathinclui.perfilpedagogico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfis_pedagogicos")
public class PerfilPedagogico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false, unique = true)
    private Long alunoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tamanho_fonte", nullable = false, length = 20)
    private TamanhoFonte tamanhoFonte = TamanhoFonte.PADRAO;

    @Column(name = "leitura_voz", nullable = false)
    private boolean leituraVoz;

    @Column(name = "apoio_visual", nullable = false)
    private boolean apoioVisual;

    @Column(name = "dicas_passo_a_passo", nullable = false)
    private boolean dicasPassoAPasso;

    @Column(name = "repeticao_guiada", nullable = false)
    private boolean repeticaoGuiada;

    @Column(name = "questoes_por_tela", nullable = false)
    private short questoesPorTela = 1;

    @Column(name = "tempo_ampliado", nullable = false)
    private boolean tempoAmpliado;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_apoio", nullable = false, length = 20)
    private NivelApoio nivelApoio = NivelApoio.PADRAO;

    protected PerfilPedagogico() {
    }

    public PerfilPedagogico(Long alunoId) {
        this.alunoId = alunoId;
    }

    public void atualizar(TamanhoFonte tamanhoFonte, boolean leituraVoz, boolean apoioVisual,
                          boolean dicasPassoAPasso, boolean repeticaoGuiada, short questoesPorTela,
                          boolean tempoAmpliado, NivelApoio nivelApoio) {
        this.tamanhoFonte = tamanhoFonte;
        this.leituraVoz = leituraVoz;
        this.apoioVisual = apoioVisual;
        this.dicasPassoAPasso = dicasPassoAPasso;
        this.repeticaoGuiada = repeticaoGuiada;
        this.questoesPorTela = questoesPorTela;
        this.tempoAmpliado = tempoAmpliado;
        this.nivelApoio = nivelApoio;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public TamanhoFonte getTamanhoFonte() { return tamanhoFonte; }
    public boolean isLeituraVoz() { return leituraVoz; }
    public boolean isApoioVisual() { return apoioVisual; }
    public boolean isDicasPassoAPasso() { return dicasPassoAPasso; }
    public boolean isRepeticaoGuiada() { return repeticaoGuiada; }
    public short getQuestoesPorTela() { return questoesPorTela; }
    public boolean isTempoAmpliado() { return tempoAmpliado; }
    public NivelApoio getNivelApoio() { return nivelApoio; }
}
