package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
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
        List<AlternativaModeloResponse> alternativas,
        List<Long> habilidadeIds,
        List<Long> tagIds
) {
    public static QuestaoModeloResponse from(QuestaoModelo m, List<AlternativaModeloResponse> alternativas,
                                              List<Long> habilidadeIds, List<Long> tagIds) {
        return new QuestaoModeloResponse(m.getId(), m.getProfessorId(), m.getTipoOrigem(), m.getEnunciado(),
                m.getTipo(), m.getRespostaCorreta(), m.getExplicacao(), m.getDica1(), m.getDica2(), m.getDica3(),
                m.getImagemUrl(), m.getLimiteTentativas(), m.getAnoEscolar(), m.getConteudo(), m.getDificuldade(),
                m.getOrigemModeloId(), m.getCopiadoEm(), List.copyOf(alternativas), List.copyOf(habilidadeIds),
                List.copyOf(tagIds));
    }
}
