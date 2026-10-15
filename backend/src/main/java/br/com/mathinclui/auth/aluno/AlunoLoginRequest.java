package br.com.mathinclui.auth.aluno;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlunoLoginRequest(@NotBlank @Size(max = 80) String username, @NotBlank @Pattern(regexp = "\\d{4,6}", message = "O PIN deve conter de 4 a 6 dígitos.") String pin) {}
