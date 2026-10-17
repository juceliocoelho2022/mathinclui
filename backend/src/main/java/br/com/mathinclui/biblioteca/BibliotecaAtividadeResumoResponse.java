package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;

public record BibliotecaAtividadeResumoResponse(
        Long id,
        Long professorId,
        String titulo,
        short anoEscolar,
        String conteudo,
        DificuldadeAtividade dificuldade,
        TipoOrigemAtividade tipoOrigem,
        boolean publicada,
        Long origemAtividadeId
) {
    public static BibliotecaAtividadeResumoResponse from(Atividade atividade) {
        return new BibliotecaAtividadeResumoResponse(
                atividade.getId(), atividade.getProfessorId(), atividade.getTitulo(), atividade.getAnoEscolar(),
                atividade.getConteudo(), atividade.getDificuldade(), atividade.getTipoOrigem(), atividade.isPublicada(),
                atividade.getOrigemAtividadeId());
    }
}
