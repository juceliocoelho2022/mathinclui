package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record BibliotecaAtividadeFiltro(
        TipoOrigemAtividade origem,
        Short ano,
        String unidadeTematica,
        String objetoConhecimento,
        String habilidade,
        String conteudo,
        DificuldadeAtividade dificuldade,
        String tag,
        String busca,
        int page,
        int size
) {
    public BibliotecaAtividadeFiltro {
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página deve ser maior ou igual a zero.");
        if (size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tamanho da página deve estar entre 1 e 100.");
        if (ano != null && (ano < 6 || ano > 8)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano deve estar entre 6 e 8.");
    }

    public static BibliotecaAtividadeFiltro padrao() {
        return new BibliotecaAtividadeFiltro(null, null, null, null, null, null, null, null, null, 0, 20);
    }
}
