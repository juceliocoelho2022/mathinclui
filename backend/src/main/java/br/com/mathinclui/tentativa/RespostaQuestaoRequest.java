package br.com.mathinclui.tentativa;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RespostaQuestaoRequest(@NotBlank String resposta, @Min(0) int tempoSegundos) {}
