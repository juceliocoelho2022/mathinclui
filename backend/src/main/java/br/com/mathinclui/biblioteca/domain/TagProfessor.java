package br.com.mathinclui.biblioteca.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tags_professor")
public class TagProfessor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "professor_id", nullable = false) private Long professorId;
    @Column(nullable = false, length = 80) private String nome;
    @Column(name = "nome_normalizado", nullable = false, length = 80) private String nomeNormalizado;

    protected TagProfessor() {}

    public TagProfessor(Long professorId, String nome, String nomeNormalizado) {
        if (professorId == null) throw new IllegalArgumentException("Professor é obrigatório.");
        if (nome == null || nome.isBlank() || nomeNormalizado == null || nomeNormalizado.isBlank())
            throw new IllegalArgumentException("Nome da tag é obrigatório.");
        this.professorId = professorId;
        this.nome = nome.trim();
        this.nomeNormalizado = nomeNormalizado;
    }

    public Long getId() { return id; }
    public Long getProfessorId() { return professorId; }
    public String getNome() { return nome; }
    public String getNomeNormalizado() { return nomeNormalizado; }
}
