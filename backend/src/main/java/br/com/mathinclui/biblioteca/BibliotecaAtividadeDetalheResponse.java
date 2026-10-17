package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;

import java.time.Instant;

public record BibliotecaAtividadeDetalheResponse(
        Long id,
        Long professorId,
        String titulo,
        String descricao,
        short anoEscolar,
        String conteudo,
        DificuldadeAtividade dificuldade,
        TipoOrigemAtividade tipoOrigem,
        boolean publicada,
        int limiteTentativas,
        Long origemAtividadeId,
        Instant copiadoEm
) {
    public static BibliotecaAtividadeDetalheResponse from(Atividade atividade) {
        return new BibliotecaAtividadeDetalheResponse(
                atividade.getId(), atividade.getProfessorId(), atividade.getTitulo(), atividade.getDescricao(),
                atividade.getAnoEscolar(), atividade.getConteudo(), atividade.getDificuldade(), atividade.getTipoOrigem(),
                atividade.isPublicada(), atividade.getLimiteTentativas(), atividade.getOrigemAtividadeId(),
                atividade.getCopiadoEm());
    }
}
