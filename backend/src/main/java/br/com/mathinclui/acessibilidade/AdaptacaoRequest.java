package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AdaptacaoRequest(
        TamanhoFonte tamanhoFonteMinima,
        Boolean leituraVozMinima,
        Boolean apoioVisualMinimo,
        Boolean dicasPassoAPasso,
        Boolean repeticaoGuiada,
        @Min(1) @Max(10) Short questoesPorTela,
        Boolean tempoAmpliado,
        NivelApoio nivelApoio
) {
    public static AdaptacaoRequest herdarTudo() {
        return new AdaptacaoRequest(null, null, null, null, null, null, null, null);
    }
}
