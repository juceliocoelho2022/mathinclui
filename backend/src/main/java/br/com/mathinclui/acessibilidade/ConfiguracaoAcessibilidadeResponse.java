package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.ConfiguracaoAcessibilidadeEfetiva;
import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;

public record ConfiguracaoAcessibilidadeResponse(
        TamanhoFonte tamanhoFonte,
        boolean leituraVoz,
        boolean apoioVisual,
        boolean dicasPassoAPasso,
        boolean repeticaoGuiada,
        short questoesPorTela,
        boolean tempoAmpliado,
        NivelApoio nivelApoio
) {
    public static ConfiguracaoAcessibilidadeResponse from(ConfiguracaoAcessibilidadeEfetiva configuracao) {
        return new ConfiguracaoAcessibilidadeResponse(
                configuracao.tamanhoFonte(), configuracao.leituraVoz(), configuracao.apoioVisual(),
                configuracao.dicasPassoAPasso(), configuracao.repeticaoGuiada(), configuracao.questoesPorTela(),
                configuracao.tempoAmpliado(), configuracao.nivelApoio());
    }
}
