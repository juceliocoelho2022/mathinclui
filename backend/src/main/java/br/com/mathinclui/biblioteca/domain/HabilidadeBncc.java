package br.com.mathinclui.biblioteca.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "habilidades_bncc")
public class HabilidadeBncc {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 32) private String codigo;
    @Column(name = "ano_escolar", nullable = false) private short anoEscolar;
    @Column(name = "unidade_tematica", nullable = false, length = 120) private String unidadeTematica;
    @Column(name = "objeto_conhecimento", nullable = false, columnDefinition = "TEXT") private String objetoConhecimento;
    @Column(nullable = false, columnDefinition = "TEXT") private String descricao;

    protected HabilidadeBncc() {}

    public HabilidadeBncc(String codigo, short anoEscolar, String unidadeTematica, String objetoConhecimento, String descricao) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("Código BNCC é obrigatório.");
        if (anoEscolar < 6 || anoEscolar > 8) throw new IllegalArgumentException("Ano escolar deve estar entre 6 e 8.");
        if (unidadeTematica == null || unidadeTematica.isBlank()) throw new IllegalArgumentException("Unidade temática é obrigatória.");
        if (objetoConhecimento == null || objetoConhecimento.isBlank()) throw new IllegalArgumentException("Objeto de conhecimento é obrigatório.");
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória.");
        this.codigo = codigo.trim();
        this.anoEscolar = anoEscolar;
        this.unidadeTematica = unidadeTematica.trim();
        this.objetoConhecimento = objetoConhecimento.trim();
        this.descricao = descricao.trim();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public short getAnoEscolar() { return anoEscolar; }
    public String getUnidadeTematica() { return unidadeTematica; }
    public String getObjetoConhecimento() { return objetoConhecimento; }
    public String getDescricao() { return descricao; }
}
