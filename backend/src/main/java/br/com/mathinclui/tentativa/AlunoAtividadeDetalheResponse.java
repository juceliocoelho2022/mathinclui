package br.com.mathinclui.tentativa;

import br.com.mathinclui.acessibilidade.ConfiguracaoAcessibilidadeResponse;

import java.util.List;

public record AlunoAtividadeDetalheResponse(
        Long atribuicaoId,
        Long atividadeId,
        String titulo,
        String descricao,
        short anoEscolar,
        String conteudo,
        String estado,
        Long tentativaId,
        ConfiguracaoAcessibilidadeResponse acessibilidade,
        List<AlunoQuestaoResponse> questoes
) {}
