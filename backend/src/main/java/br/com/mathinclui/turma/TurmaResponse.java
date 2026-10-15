package br.com.mathinclui.turma;

import br.com.mathinclui.turma.domain.Turma;

import java.time.Instant;

public record TurmaResponse(
        Long id,
        String nome,
        short anoEscolar,
        String codigo,
        boolean ativa,
        Instant criadoEm
) {
    public static TurmaResponse from(Turma turma) {
        return new TurmaResponse(
                turma.getId(),
                turma.getNome(),
                turma.getAnoEscolar(),
                turma.getCodigo(),
                turma.isAtiva(),
                turma.getCriadoEm()
        );
    }
}
