package br.com.mathinclui.biblioteca;

import java.util.List;

public record ClassificacaoAtividadeResponse(List<HabilidadeBnccResponse> habilidades, List<TagResponse> tags) {}
