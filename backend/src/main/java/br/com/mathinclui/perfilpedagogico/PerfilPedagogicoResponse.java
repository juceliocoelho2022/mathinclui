package br.com.mathinclui.perfilpedagogico;

import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.PerfilPedagogico;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;

public record PerfilPedagogicoResponse(
        Long id,
        Long alunoId,
        TamanhoFonte tamanhoFonte,
        boolean leituraVoz,
        boolean apoioVisual,
        boolean dicasPassoAPasso,
        boolean repeticaoGuiada,
        short questoesPorTela,
        boolean tempoAmpliado,
        NivelApoio nivelApoio
) {
    public static PerfilPedagogicoResponse from(PerfilPedagogico perfil) {
        return new PerfilPedagogicoResponse(
                perfil.getId(), perfil.getAlunoId(), perfil.getTamanhoFonte(), perfil.isLeituraVoz(),
                perfil.isApoioVisual(), perfil.isDicasPassoAPasso(), perfil.isRepeticaoGuiada(),
                perfil.getQuestoesPorTela(), perfil.isTempoAmpliado(), perfil.getNivelApoio()
        );
    }
}
