package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.biblioteca.domain.TipoOrigemBiblioteca;
import br.com.mathinclui.questao.domain.TipoQuestao;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record BibliotecaQuestaoFiltro(
        TipoOrigemBiblioteca origem,
        Short ano,
        String conteudo,
        DificuldadeAtividade dificuldade,
        TipoQuestao tipo,
        String habilidade,
        String tag,
        String busca,
        int page,
        int size
) {
    public BibliotecaQuestaoFiltro {
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página deve ser maior ou igual a zero.");
        if (size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tamanho da página deve estar entre 1 e 100.");
        if (ano != null && (ano < 6 || ano > 8)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano deve estar entre 6 e 8.");
    }
}
