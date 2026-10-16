package br.com.mathinclui.tentativa;

import br.com.mathinclui.tentativa.domain.StatusTentativa;
import br.com.mathinclui.tentativa.domain.Tentativa;

import java.time.Instant;

public record TentativaResponse(Long id, Long atribuicaoId, StatusTentativa status, Instant iniciadaEm, Instant finalizadaEm) {
    public static TentativaResponse from(Tentativa tentativa) {
        return new TentativaResponse(tentativa.getId(), tentativa.getAtribuicaoId(), tentativa.getStatus(),
                tentativa.getIniciadaEm(), tentativa.getFinalizadaEm());
    }
}
