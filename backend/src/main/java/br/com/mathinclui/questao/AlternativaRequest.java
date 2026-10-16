package br.com.mathinclui.questao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AlternativaRequest(@NotBlank String texto, boolean correta, @Min(1) int ordem) {}
