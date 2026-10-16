package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SalvarQuestaoNoBancoRequest(
        @Min(6) @Max(8) short anoEscolar,
        @NotBlank String conteudo,
        @NotNull DificuldadeAtividade dificuldade,
        @NotNull List<Long> habilidadeIds,
        @NotNull List<Long> tagIds
) {}
