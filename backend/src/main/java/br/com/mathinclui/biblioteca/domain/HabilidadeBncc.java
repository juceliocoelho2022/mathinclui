package br.com.mathinclui.biblioteca.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "habilidades_bncc")
public class HabilidadeBncc {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 24) private String codigo;
    @Column(name = "ano_escolar", nullable = false) private short anoEscolar;
    @Column(name = "unidade_tematica", nullable = false, length = 120) private String unidadeTematica;
    @Column(name = "objeto_conhecimento", nullable = false, length = 240) private String objetoConhecimento;
    @Column(nullable = false, columnDefinition = "TEXT") private String descricao;

    protected HabilidadeBncc() {}
    public HabilidadeBncc(String codigo, short anoEscolar, String unidadeTematica, String objetoConhecimento, String descricao) {
        this.codigo = codigo; this.anoEscolar = anoEscolar; this.unidadeTematica = unidadeTematica;
        this.objetoConhecimento = objetoConhecimento; this.descricao = descricao;
    }
    public Long getId(){return id;} public String getCodigo(){return codigo;} public short getAnoEscolar(){return anoEscolar;}
    public String getUnidadeTematica(){return unidadeTematica;} public String getObjetoConhecimento(){return objetoConhecimento;}
    public String getDescricao(){return descricao;}
}
