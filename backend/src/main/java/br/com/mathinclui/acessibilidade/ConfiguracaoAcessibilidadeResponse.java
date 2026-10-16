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
    public static ConfiguracaoAcessibilidadeResponse from(ConfiguracaoAcessibilidadeEfetiva c) {
        return new ConfiguracaoAcessibilidadeResponse(c.tamanhoFonte(), c.leituraVoz(), c.apoioVisual(),
                c.dicasPassoAPasso(), c.repeticaoGuiada(), c.questoesPorTela(), c.tempoAmpliado(), c.nivelApoio());
    }
}
