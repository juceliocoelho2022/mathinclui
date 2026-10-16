package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import jakarta.validation.constraints.NotNull;

public record PreferenciaInterfaceRequest(
        @NotNull TamanhoFonte tamanhoFonte,
        boolean leituraVoz,
        boolean apoioVisual
) {}
