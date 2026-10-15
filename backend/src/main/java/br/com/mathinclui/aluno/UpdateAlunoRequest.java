package br.com.mathinclui.aluno;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAlunoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(min = 3, max = 80) @Pattern(regexp = "[A-Za-z0-9._-]+") String username
) {
}
