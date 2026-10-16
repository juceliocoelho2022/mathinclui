package br.com.mathinclui.acessibilidade.domain;

import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "preferencias_interface_aluno")
public class PreferenciaInterfaceAluno {
    @Id
    @Column(name = "aluno_id")
    private Long alunoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tamanho_fonte", nullable = false, length = 20)
    private TamanhoFonte tamanhoFonte = TamanhoFonte.PADRAO;

    @Column(name = "leitura_voz", nullable = false)
    private boolean leituraVoz;

    @Column(name = "apoio_visual", nullable = false)
    private boolean apoioVisual;

    @Column(name = "atualizado_em", nullable = false, insertable = false, updatable = false)
    private Instant atualizadoEm;

    protected PreferenciaInterfaceAluno() {}

    public PreferenciaInterfaceAluno(Long alunoId) {
        if (alunoId == null) throw new IllegalArgumentException("Aluno é obrigatório.");
        this.alunoId = alunoId;
    }

    public void atualizar(TamanhoFonte tamanhoFonte, boolean leituraVoz, boolean apoioVisual) {
        if (tamanhoFonte == null) throw new IllegalArgumentException("Tamanho de fonte é obrigatório.");
        this.tamanhoFonte = tamanhoFonte;
        this.leituraVoz = leituraVoz;
        this.apoioVisual = apoioVisual;
    }

    public Long getAlunoId() { return alunoId; }
    public TamanhoFonte getTamanhoFonte() { return tamanhoFonte; }
    public boolean isLeituraVoz() { return leituraVoz; }
    public boolean isApoioVisual() { return apoioVisual; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
