package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;

import java.util.List;

public record BibliotecaAtividadeResumoResponse(
        Long id, String titulo, short anoEscolar, String conteudo, DificuldadeAtividade dificuldade,
        TipoOrigemAtividade tipoOrigem, boolean publicada, long quantidadeQuestoes,
        List<HabilidadeBnccResponse> habilidades, List<TagResponse> tags
) {
    public static BibliotecaAtividadeResumoResponse from(Atividade a, long quantidadeQuestoes,
                                                          List<HabilidadeBnccResponse> habilidades,
                                                          List<TagResponse> tags) {
        return new BibliotecaAtividadeResumoResponse(a.getId(), a.getTitulo(), a.getAnoEscolar(), a.getConteudo(),
                a.getDificuldade(), a.getTipoOrigem(), a.isPublicada(), quantidadeQuestoes,
                List.copyOf(habilidades), List.copyOf(tags));
    }
}
