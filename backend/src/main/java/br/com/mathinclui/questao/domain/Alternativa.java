package br.com.mathinclui.questao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alternativas")
public class Alternativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "questao_id", nullable = false)
    private Long questaoId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false)
    private boolean correta;

    @Column(nullable = false)
    private int ordem;

    protected Alternativa() {
    }

    public Alternativa(Long questaoId, String texto, boolean correta, int ordem) {
        if (questaoId == null) throw new IllegalArgumentException("Questão é obrigatória.");
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Texto da alternativa é obrigatório.");
        if (ordem < 1) throw new IllegalArgumentException("Ordem deve ser maior ou igual a 1.");
        this.questaoId = questaoId;
        this.texto = texto.trim();
        this.correta = correta;
        this.ordem = ordem;
    }

    public Long getId() { return id; }
    public Long getQuestaoId() { return questaoId; }
    public String getTexto() { return texto; }
    public boolean isCorreta() { return correta; }
    public int getOrdem() { return ordem; }
}
