package br.com.mathinclui.biblioteca;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagNomeRequest(
        @NotBlank @Size(max = 80) String nome
) {}
