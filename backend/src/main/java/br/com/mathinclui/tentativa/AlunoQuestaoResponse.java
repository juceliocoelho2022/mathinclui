package br.com.mathinclui.tentativa;

import br.com.mathinclui.questao.domain.TipoQuestao;

import java.util.List;

public record AlunoQuestaoResponse(
        Long id,
        String enunciado,
        TipoQuestao tipo,
        String imagemUrl,
        int ordem,
        int tentativasUsadas,
        int limiteTentativas,
        int dicasLiberadas,
        String dicaAtual,
        boolean encerrada,
        List<AlunoAlternativaResponse> alternativas
) {}
