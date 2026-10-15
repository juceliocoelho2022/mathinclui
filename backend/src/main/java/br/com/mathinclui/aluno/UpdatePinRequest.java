package br.com.mathinclui.aluno;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePinRequest(
        @NotBlank @Pattern(regexp = "\\d{4,6}") String pin
) {
}
