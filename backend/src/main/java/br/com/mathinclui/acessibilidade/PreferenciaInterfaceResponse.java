package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.PreferenciaInterfaceAluno;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;

public record PreferenciaInterfaceResponse(TamanhoFonte tamanhoFonte, boolean leituraVoz, boolean apoioVisual) {
    public static PreferenciaInterfaceResponse from(PreferenciaInterfaceAluno p) {
        return new PreferenciaInterfaceResponse(p.getTamanhoFonte(), p.isLeituraVoz(), p.isApoioVisual());
    }

    public static PreferenciaInterfaceResponse padrao() {
        return new PreferenciaInterfaceResponse(TamanhoFonte.PADRAO, false, false);
    }
}
