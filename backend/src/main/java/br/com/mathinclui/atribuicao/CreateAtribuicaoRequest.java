package br.com.mathinclui.atribuicao;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateAtribuicaoRequest(
        @NotNull Long atividadeId,
        Long turmaId,
        Long alunoId,
        Instant dataInicio,
        Instant dataLimite
) {}
