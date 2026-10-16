package br.com.mathinclui.atribuicao;

import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.domain.StatusAtribuicao;

import java.time.Instant;

public record AtribuicaoResponse(
        Long id, Long atividadeId, Long professorId, Long turmaId, Long alunoId,
        Instant dataInicio, Instant dataLimite, StatusAtribuicao status, Instant criadoEm
) {
    public static AtribuicaoResponse from(Atribuicao atribuicao) {
        return new AtribuicaoResponse(atribuicao.getId(), atribuicao.getAtividadeId(), atribuicao.getProfessorId(),
                atribuicao.getTurmaId(), atribuicao.getAlunoId(), atribuicao.getDataInicio(), atribuicao.getDataLimite(),
                atribuicao.getStatus(), atribuicao.getCriadoEm());
    }
}
