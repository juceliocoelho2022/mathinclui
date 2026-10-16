package br.com.mathinclui.acessibilidade.domain;

import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;

public record ConfiguracaoAcessibilidadeEfetiva(
        TamanhoFonte tamanhoFonte,
        boolean leituraVoz,
        boolean apoioVisual,
        boolean dicasPassoAPasso,
        boolean repeticaoGuiada,
        short questoesPorTela,
        boolean tempoAmpliado,
        NivelApoio nivelApoio
) {}
