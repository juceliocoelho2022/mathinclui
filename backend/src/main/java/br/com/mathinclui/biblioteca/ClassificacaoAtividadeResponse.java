package br.com.mathinclui.biblioteca;

import java.util.List;

public record ClassificacaoAtividadeResponse(List<Long> habilidadeIds, List<Long> tagIds) {
    public ClassificacaoAtividadeResponse {
        habilidadeIds = List.copyOf(habilidadeIds);
        tagIds = List.copyOf(tagIds);
    }
}
