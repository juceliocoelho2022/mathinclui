package br.com.mathinclui.biblioteca;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportarQuestoesRequest(@NotEmpty List<Long> questoesModeloIds) {}
