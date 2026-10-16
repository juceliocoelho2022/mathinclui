package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAlunoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.AdaptacaoAtribuicao;
import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;

public record AdaptacaoResponse(
        Long atribuicaoId,
        Long alunoId,
        TamanhoFonte tamanhoFonteMinima,
        Boolean leituraVozMinima,
        Boolean apoioVisualMinimo,
        Boolean dicasPassoAPasso,
        Boolean repeticaoGuiada,
        Short questoesPorTela,
        Boolean tempoAmpliado,
        NivelApoio nivelApoio
) {
    public static AdaptacaoResponse from(AdaptacaoAtribuicao adaptacao) {
        return new AdaptacaoResponse(adaptacao.getAtribuicaoId(), null, adaptacao.getTamanhoFonteMinima(),
                adaptacao.getLeituraVozMinima(), adaptacao.getApoioVisualMinimo(), adaptacao.getDicasPassoAPasso(),
                adaptacao.getRepeticaoGuiada(), adaptacao.getQuestoesPorTela(), adaptacao.getTempoAmpliado(),
                adaptacao.getNivelApoio());
    }

    public static AdaptacaoResponse from(AdaptacaoAlunoAtribuicao adaptacao) {
        return new AdaptacaoResponse(adaptacao.getAtribuicaoId(), adaptacao.getAlunoId(),
                adaptacao.getTamanhoFonteMinima(), adaptacao.getLeituraVozMinima(), adaptacao.getApoioVisualMinimo(),
                adaptacao.getDicasPassoAPasso(), adaptacao.getRepeticaoGuiada(), adaptacao.getQuestoesPorTela(),
                adaptacao.getTempoAmpliado(), adaptacao.getNivelApoio());
    }

    public static AdaptacaoResponse herdada(Long atribuicaoId, Long alunoId) {
        return new AdaptacaoResponse(atribuicaoId, alunoId, null, null, null, null, null, null, null, null);
    }
}
