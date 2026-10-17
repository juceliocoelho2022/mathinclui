package br.com.mathinclui.biblioteca;

import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;

public record QuestaoImportadaResponse(Long id, String enunciado, TipoQuestao tipo, int ordem, Long origemQuestaoModeloId) {
    public static QuestaoImportadaResponse from(Questao questao) {
        return new QuestaoImportadaResponse(questao.getId(), questao.getEnunciado(), questao.getTipo(),
                questao.getOrdem(), questao.getOrigemQuestaoModeloId());
    }
}
