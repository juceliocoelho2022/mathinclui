package br.com.mathinclui.biblioteca;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ClassificacaoAtividadeRequest(@NotNull List<Long> habilidadeIds, @NotNull List<Long> tagIds) {}
