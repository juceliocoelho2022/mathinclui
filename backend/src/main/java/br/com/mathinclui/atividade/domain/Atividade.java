package br.com.mathinclui.atividade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "atividades")
public class Atividade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professor_id")
    private Long professorId;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ano_escolar", nullable = false)
    private short anoEscolar;

    @Column(nullable = false, length = 60)
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DificuldadeAtividade dificuldade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origem", nullable = false, length = 20)
    private TipoOrigemAtividade tipoOrigem;

    @Column(nullable = false)
    private boolean publicada;

    @Column(name = "limite_tentativas", nullable = false)
    private int limiteTentativas = 3;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private Instant criadoEm;

    protected Atividade() {
    }

    public Atividade(Long professorId, String titulo, String descricao, short anoEscolar,
                     String conteudo, DificuldadeAtividade dificuldade, int limiteTentativas) {
        if (professorId == null) throw new IllegalArgumentException("Professor é obrigatório.");
        validar(titulo, anoEscolar, conteudo, dificuldade, limiteTentativas);
        this.professorId = professorId;
        this.tipoOrigem = TipoOrigemAtividade.PROFESSOR;
        this.publicada = false;
        aplicar(titulo, descricao, anoEscolar, conteudo, dificuldade, limiteTentativas);
    }

    public void atualizar(String titulo, String descricao, short anoEscolar, String conteudo,
                          DificuldadeAtividade dificuldade, int limiteTentativas) {
        validar(titulo, anoEscolar, conteudo, dificuldade, limiteTentativas);
        aplicar(titulo, descricao, anoEscolar, conteudo, dificuldade, limiteTentativas);
    }

    public void publicar() {
        this.publicada = true;
    }

    public boolean pertenceAoProfessor(Long professorId) {
        return tipoOrigem == TipoOrigemAtividade.PROFESSOR && this.professorId != null && this.professorId.equals(professorId);
    }

    public boolean isSistemaPublicada() {
        return tipoOrigem == TipoOrigemAtividade.SISTEMA && publicada;
    }

    private void aplicar(String titulo, String descricao, short anoEscolar, String conteudo,
                         DificuldadeAtividade dificuldade, int limiteTentativas) {
        this.titulo = titulo.trim();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.anoEscolar = anoEscolar;
        this.conteudo = conteudo.trim();
        this.dificuldade = dificuldade;
        this.limiteTentativas = limiteTentativas;
    }

    private void validar(String titulo, short anoEscolar, String conteudo,
                         DificuldadeAtividade dificuldade, int limiteTentativas) {
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("Título é obrigatório.");
        if (anoEscolar < 6 || anoEscolar > 8) throw new IllegalArgumentException("Ano escolar deve estar entre 6 e 8.");
        if (conteudo == null || conteudo.isBlank()) throw new IllegalArgumentException("Conteúdo é obrigatório.");
        if (dificuldade == null) throw new IllegalArgumentException("Dificuldade é obrigatória.");
        if (limiteTentativas < 1) throw new IllegalArgumentException("Limite de tentativas deve ser maior ou igual a 1.");
    }

    public Long getId() { return id; }
    public Long getProfessorId() { return professorId; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public short getAnoEscolar() { return anoEscolar; }
    public String getConteudo() { return conteudo; }
    public DificuldadeAtividade getDificuldade() { return dificuldade; }
    public TipoOrigemAtividade getTipoOrigem() { return tipoOrigem; }
    public boolean isPublicada() { return publicada; }
    public int getLimiteTentativas() { return limiteTentativas; }
    public Instant getCriadoEm() { return criadoEm; }
}
