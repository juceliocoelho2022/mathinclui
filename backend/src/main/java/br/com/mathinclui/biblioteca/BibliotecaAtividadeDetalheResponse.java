package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import br.com.mathinclui.questao.QuestaoProfessorResponse;

import java.util.List;

public record BibliotecaAtividadeDetalheResponse(
        Long id, Long professorId, String titulo, String descricao, short anoEscolar, String conteudo,
        DificuldadeAtividade dificuldade, TipoOrigemAtividade tipoOrigem, boolean publicada, int limiteTentativas,
        List<HabilidadeBnccResponse> habilidades, List<TagResponse> tags, List<QuestaoProfessorResponse> questoes
) {
    public static BibliotecaAtividadeDetalheResponse from(Atividade a, List<HabilidadeBnccResponse> habilidades,
                                                           List<TagResponse> tags, List<QuestaoProfessorResponse> questoes) {
        return new BibliotecaAtividadeDetalheResponse(a.getId(), a.getProfessorId(), a.getTitulo(), a.getDescricao(),
                a.getAnoEscolar(), a.getConteudo(), a.getDificuldade(), a.getTipoOrigem(), a.isPublicada(),
                a.getLimiteTentativas(), List.copyOf(habilidades), List.copyOf(tags), List.copyOf(questoes));
    }
}
