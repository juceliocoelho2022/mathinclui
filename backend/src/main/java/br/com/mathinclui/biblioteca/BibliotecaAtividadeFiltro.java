package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;

public record BibliotecaAtividadeFiltro(
        TipoOrigemAtividade origem,
        Short ano,
        String unidadeTematica,
        String objetoConhecimento,
        String habilidade,
        String conteudo,
        DificuldadeAtividade dificuldade,
        Long tag,
        String busca,
        int page,
        int size
) {
    public BibliotecaAtividadeFiltro {
        if (page < 0) throw new IllegalArgumentException("Página deve ser maior ou igual a zero.");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Tamanho da página deve ficar entre 1 e 100.");
    }
}
