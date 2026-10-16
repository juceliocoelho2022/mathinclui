package br.com.mathinclui.biblioteca.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alternativas_modelo")
public class AlternativaModelo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="questao_modelo_id",nullable=false) private Long questaoModeloId;
    @Column(nullable=false,columnDefinition="TEXT") private String texto;
    @Column(nullable=false) private boolean correta;
    @Column(nullable=false) private int ordem;
    protected AlternativaModelo() {}
    public AlternativaModelo(Long questaoModeloId,String texto,boolean correta,int ordem){
        this.questaoModeloId=questaoModeloId; this.texto=texto; this.correta=correta; this.ordem=ordem;
    }
    public Long getId(){return id;} public Long getQuestaoModeloId(){return questaoModeloId;} public String getTexto(){return texto;}
    public boolean isCorreta(){return correta;} public int getOrdem(){return ordem;}
}
