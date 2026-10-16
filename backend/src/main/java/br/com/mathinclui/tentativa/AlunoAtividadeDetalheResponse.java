package br.com.mathinclui.tentativa;

import br.com.mathinclui.perfilpedagogico.PerfilPedagogicoResponse;

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
        PerfilPedagogicoResponse perfilPedagogico,
        List<AlunoQuestaoResponse> questoes
) {}
