package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.PreferenciaInterfaceAluno;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;

public record PreferenciaInterfaceResponse(
        Long alunoId,
        TamanhoFonte tamanhoFonte,
        boolean leituraVoz,
        boolean apoioVisual
) {
    public static PreferenciaInterfaceResponse from(PreferenciaInterfaceAluno preferencia) {
        return new PreferenciaInterfaceResponse(preferencia.getAlunoId(), preferencia.getTamanhoFonte(),
                preferencia.isLeituraVoz(), preferencia.isApoioVisual());
    }

    public static PreferenciaInterfaceResponse padrao(Long alunoId) {
        return new PreferenciaInterfaceResponse(alunoId, TamanhoFonte.PADRAO, false, false);
    }
}
