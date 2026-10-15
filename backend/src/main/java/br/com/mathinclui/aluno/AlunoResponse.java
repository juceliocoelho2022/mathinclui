package br.com.mathinclui.aluno;

import br.com.mathinclui.aluno.domain.Aluno;

import java.time.Instant;

public record AlunoResponse(
        Long id,
        Long turmaId,
        String nome,
        String username,
        boolean ativo,
        Instant criadoEm
) {
    public static AlunoResponse from(Aluno aluno) {
        return new AlunoResponse(
                aluno.getId(), aluno.getTurmaId(), aluno.getNome(), aluno.getUsername(),
                aluno.isAtivo(), aluno.getCriadoEm()
        );
    }
}
