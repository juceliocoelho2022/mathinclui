package br.com.mathinclui.questao;

import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;

import java.util.List;

public record QuestaoProfessorResponse(
        Long id,
        Long atividadeId,
        String enunciado,
        TipoQuestao tipo,
        String respostaCorreta,
        String explicacao,
        String dica1,
        String dica2,
        String dica3,
        String imagemUrl,
        int ordem,
        Integer limiteTentativas,
        List<AlternativaResponse> alternativas
) {
    public static QuestaoProfessorResponse from(Questao questao, List<AlternativaResponse> alternativas) {
        return new QuestaoProfessorResponse(questao.getId(), questao.getAtividadeId(), questao.getEnunciado(),
                questao.getTipo(), questao.getRespostaCorreta(), questao.getExplicacao(), questao.getDica1(),
                questao.getDica2(), questao.getDica3(), questao.getImagemUrl(), questao.getOrdem(),
                questao.getLimiteTentativas(), alternativas);
    }
}
