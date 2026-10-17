package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.HabilidadeBncc;

public record HabilidadeBnccResponse(
        Long id,
        String codigo,
        short anoEscolar,
        String unidadeTematica,
        String objetoConhecimento,
        String descricao
) {
    public static HabilidadeBnccResponse from(HabilidadeBncc habilidade) {
        return new HabilidadeBnccResponse(
                habilidade.getId(), habilidade.getCodigo(), habilidade.getAnoEscolar(),
                habilidade.getUnidadeTematica(), habilidade.getObjetoConhecimento(), habilidade.getDescricao());
    }
}
