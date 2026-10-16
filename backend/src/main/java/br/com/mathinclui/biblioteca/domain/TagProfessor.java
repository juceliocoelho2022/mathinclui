package br.com.mathinclui.biblioteca.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tags_professor")
public class TagProfessor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "professor_id", nullable = false) private Long professorId;
    @Column(nullable = false, length = 80) private String nome;
    @Column(name = "nome_normalizado", nullable = false, length = 80) private String nomeNormalizado;

    protected TagProfessor() {}
    public TagProfessor(Long professorId, String nome, String nomeNormalizado) {
        this.professorId = professorId; this.nome = nome; this.nomeNormalizado = nomeNormalizado;
    }
    public void renomear(String nome, String nomeNormalizado){this.nome=nome; this.nomeNormalizado=nomeNormalizado;}
    public Long getId(){return id;} public Long getProfessorId(){return professorId;} public String getNome(){return nome;}
    public String getNomeNormalizado(){return nomeNormalizado;}
}
