package br.com.mathinclui.biblioteca;

import java.util.List;

public record ClassificacaoAtividadeRequest(List<Long> habilidadeIds, List<Long> tagIds) {
    public ClassificacaoAtividadeRequest {
        habilidadeIds = habilidadeIds == null ? List.of() : List.copyOf(habilidadeIds);
        tagIds = tagIds == null ? List.of() : List.copyOf(tagIds);
    }
}
