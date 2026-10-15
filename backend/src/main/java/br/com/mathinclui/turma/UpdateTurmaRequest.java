package br.com.mathinclui.turma;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTurmaRequest(
        @NotBlank @Size(max = 100) String nome,
        @Min(6) @Max(8) short anoEscolar,
        @NotBlank @Size(max = 32) @Pattern(regexp = "[A-Za-z0-9._-]{2,32}") String codigo
) {
}
