package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.HabilidadeBncc;

public record HabilidadeBnccResponse(Long id, String codigo, short anoEscolar, String unidadeTematica,
                                     String objetoConhecimento, String descricao) {
    public static HabilidadeBnccResponse from(HabilidadeBncc h) {
        return new HabilidadeBnccResponse(h.getId(), h.getCodigo(), h.getAnoEscolar(), h.getUnidadeTematica(),
                h.getObjetoConhecimento(), h.getDescricao());
    }
}
