package br.com.mathinclui.atividade;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAtividadeRequest(
        @NotBlank @Size(max = 160) String titulo,
        String descricao,
        @Min(6) @Max(8) short anoEscolar,
        @NotBlank @Size(max = 60) String conteudo,
        @NotNull DificuldadeAtividade dificuldade,
        @Min(1) Integer limiteTentativas
) {
    public CreateAtividadeRequest {
        if (limiteTentativas == null) limiteTentativas = 3;
    }
}
