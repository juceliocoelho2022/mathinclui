package br.com.mathinclui.perfilpedagogico;

import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PerfilPedagogicoRequest(
        @NotNull TamanhoFonte tamanhoFonte,
        boolean leituraVoz,
        boolean apoioVisual,
        boolean dicasPassoAPasso,
        boolean repeticaoGuiada,
        @Min(1) @Max(10) short questoesPorTela,
        boolean tempoAmpliado,
        @NotNull NivelApoio nivelApoio
) {
}
