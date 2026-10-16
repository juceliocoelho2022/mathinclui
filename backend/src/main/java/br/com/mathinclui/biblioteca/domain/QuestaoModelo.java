package br.com.mathinclui.biblioteca.domain;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.questao.domain.TipoQuestao;
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
@Table(name = "questoes_modelo")
public class QuestaoModelo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "professor_id") private Long professorId;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_origem", nullable = false, length = 20) private TipoOrigemBiblioteca tipoOrigem;
    @Column(nullable = false, columnDefinition = "TEXT") private String enunciado;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TipoQuestao tipo;
    @Column(name = "resposta_correta", columnDefinition = "TEXT") private String respostaCorreta;
    @Column(columnDefinition = "TEXT") private String explicacao;
    @Column(name="dica_1",columnDefinition="TEXT") private String dica1;
    @Column(name="dica_2",columnDefinition="TEXT") private String dica2;
    @Column(name="dica_3",columnDefinition="TEXT") private String dica3;
    @Column(name="imagem_url",columnDefinition="TEXT") private String imagemUrl;
    @Column(name="limite_tentativas") private Integer limiteTentativas;
    @Column(name="ano_escolar",nullable=false) private short anoEscolar;
    @Column(nullable=false,length=60) private String conteudo;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private DificuldadeAtividade dificuldade;
    @Column(name="origem_modelo_id") private Long origemModeloId;
    @Column(name="copiado_em") private Instant copiadoEm;
    @Column(name="criado_em",nullable=false,insertable=false,updatable=false) private Instant criadoEm;

    protected QuestaoModelo() {}
    public QuestaoModelo(Long professorId, TipoOrigemBiblioteca tipoOrigem, String enunciado, TipoQuestao tipo,
                         String respostaCorreta, String explicacao, String dica1, String dica2, String dica3,
                         String imagemUrl, Integer limiteTentativas, short anoEscolar, String conteudo,
                         DificuldadeAtividade dificuldade) {
        this.professorId=professorId; this.tipoOrigem=tipoOrigem; this.enunciado=enunciado; this.tipo=tipo;
        this.respostaCorreta=respostaCorreta; this.explicacao=explicacao; this.dica1=dica1; this.dica2=dica2;
        this.dica3=dica3; this.imagemUrl=imagemUrl; this.limiteTentativas=limiteTentativas; this.anoEscolar=anoEscolar;
        this.conteudo=conteudo; this.dificuldade=dificuldade;
    }
    public void marcarOrigem(Long origemModeloId, Instant copiadoEm){this.origemModeloId=origemModeloId; this.copiadoEm=copiadoEm;}
    public Long getId(){return id;} public Long getProfessorId(){return professorId;} public TipoOrigemBiblioteca getTipoOrigem(){return tipoOrigem;}
    public String getEnunciado(){return enunciado;} public TipoQuestao getTipo(){return tipo;} public String getRespostaCorreta(){return respostaCorreta;}
    public String getExplicacao(){return explicacao;} public String getDica1(){return dica1;} public String getDica2(){return dica2;}
    public String getDica3(){return dica3;} public String getImagemUrl(){return imagemUrl;} public Integer getLimiteTentativas(){return limiteTentativas;}
    public short getAnoEscolar(){return anoEscolar;} public String getConteudo(){return conteudo;} public DificuldadeAtividade getDificuldade(){return dificuldade;}
    public Long getOrigemModeloId(){return origemModeloId;} public Instant getCopiadoEm(){return copiadoEm;} public Instant getCriadoEm(){return criadoEm;}
}
