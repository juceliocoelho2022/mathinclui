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
    public static AdaptacaoResponse from(AdaptacaoAtribuicao a) {
        return new AdaptacaoResponse(a.getAtribuicaoId(), null, a.getTamanhoFonteMinima(), a.getLeituraVozMinima(),
                a.getApoioVisualMinimo(), a.getDicasPassoAPasso(), a.getRepeticaoGuiada(), a.getQuestoesPorTela(),
                a.getTempoAmpliado(), a.getNivelApoio());
    }

    public static AdaptacaoResponse from(AdaptacaoAlunoAtribuicao a) {
        return new AdaptacaoResponse(a.getAtribuicaoId(), a.getAlunoId(), a.getTamanhoFonteMinima(), a.getLeituraVozMinima(),
                a.getApoioVisualMinimo(), a.getDicasPassoAPasso(), a.getRepeticaoGuiada(), a.getQuestoesPorTela(),
                a.getTempoAmpliado(), a.getNivelApoio());
    }

    public static AdaptacaoResponse vazia(Long atribuicaoId, Long alunoId) {
        return new AdaptacaoResponse(atribuicaoId, alunoId, null, null, null, null, null, null, null, null);
    }
}
