package br.com.mathinclui.tentativa;

import java.time.Instant;

public record AlunoAtividadeResumoResponse(
        Long atribuicaoId,
        Long atividadeId,
        String titulo,
        short anoEscolar,
        String conteudo,
        String estado,
        Instant dataInicio,
        Instant dataLimite,
        Long tentativaId
) {}
