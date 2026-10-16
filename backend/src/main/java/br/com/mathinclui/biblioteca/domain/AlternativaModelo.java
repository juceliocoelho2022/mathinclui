package br.com.mathinclui.biblioteca.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "alternativas_modelo")
public class AlternativaModelo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "questao_modelo_id", nullable = false) private Long questaoModeloId;
    @Column(nullable = false, columnDefinition = "TEXT") private String texto;
    @Column(nullable = false) private boolean correta;
    @Column(nullable = false) private int ordem;

    protected AlternativaModelo() {}

    public AlternativaModelo(Long questaoModeloId, String texto, boolean correta, int ordem) {
        if (questaoModeloId == null) throw new IllegalArgumentException("Questão modelo é obrigatória.");
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Texto é obrigatório.");
        if (ordem < 1) throw new IllegalArgumentException("Ordem deve ser positiva.");
        this.questaoModeloId = questaoModeloId;
        this.texto = texto.trim();
        this.correta = correta;
        this.ordem = ordem;
    }

    public Long getId() { return id; }
    public Long getQuestaoModeloId() { return questaoModeloId; }
    public String getTexto() { return texto; }
    public boolean isCorreta() { return correta; }
    public int getOrdem() { return ordem; }
}
