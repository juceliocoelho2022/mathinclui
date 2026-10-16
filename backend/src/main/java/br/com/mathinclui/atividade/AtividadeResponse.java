package br.com.mathinclui.atividade;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;

import java.time.Instant;

public record AtividadeResponse(
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
        Instant criadoEm
) {
    public static AtividadeResponse from(Atividade atividade) {
        return new AtividadeResponse(atividade.getId(), atividade.getProfessorId(), atividade.getTitulo(),
                atividade.getDescricao(), atividade.getAnoEscolar(), atividade.getConteudo(),
                atividade.getDificuldade(), atividade.getTipoOrigem(), atividade.isPublicada(),
                atividade.getLimiteTentativas(), atividade.getCriadoEm());
    }
}
