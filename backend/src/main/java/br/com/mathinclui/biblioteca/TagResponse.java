package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.TagProfessor;

public record TagResponse(Long id, String nome, String nomeNormalizado) {
    public static TagResponse from(TagProfessor tag) {
        return new TagResponse(tag.getId(), tag.getNome(), tag.getNomeNormalizado());
    }
}
