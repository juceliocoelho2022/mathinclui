package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.biblioteca.domain.AlternativaModelo;
import br.com.mathinclui.biblioteca.domain.QuestaoModelo;
import br.com.mathinclui.biblioteca.domain.TipoOrigemBiblioteca;
import br.com.mathinclui.questao.domain.TipoQuestao;

import java.time.Instant;
import java.util.List;

public record QuestaoModeloResponse(
        Long id,
        Long professorId,
        TipoOrigemBiblioteca tipoOrigem,
        String enunciado,
        TipoQuestao tipo,
        String respostaCorreta,
        String explicacao,
        String dica1,
        String dica2,
        String dica3,
        String imagemUrl,
        Integer limiteTentativas,
        short anoEscolar,
        String conteudo,
        DificuldadeAtividade dificuldade,
        Long origemModeloId,
        Instant copiadoEm,
        List<AlternativaModeloResponse> alternativas
) {
    public static QuestaoModeloResponse from(QuestaoModelo modelo, List<AlternativaModelo> alternativas) {
        return new QuestaoModeloResponse(modelo.getId(), modelo.getProfessorId(), modelo.getTipoOrigem(),
                modelo.getEnunciado(), modelo.getTipo(), modelo.getRespostaCorreta(), modelo.getExplicacao(),
                modelo.getDica1(), modelo.getDica2(), modelo.getDica3(), modelo.getImagemUrl(), modelo.getLimiteTentativas(),
                modelo.getAnoEscolar(), modelo.getConteudo(), modelo.getDificuldade(), modelo.getOrigemModeloId(),
                modelo.getCopiadoEm(), alternativas.stream().map(AlternativaModeloResponse::from).toList());
    }

    public record AlternativaModeloResponse(Long id, String texto, boolean correta, int ordem) {
        public static AlternativaModeloResponse from(AlternativaModelo alternativa) {
            return new AlternativaModeloResponse(alternativa.getId(), alternativa.getTexto(), alternativa.isCorreta(), alternativa.getOrdem());
        }
    }
}
