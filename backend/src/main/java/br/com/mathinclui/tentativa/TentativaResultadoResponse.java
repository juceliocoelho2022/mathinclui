package br.com.mathinclui.tentativa;

import br.com.mathinclui.tentativa.domain.StatusTentativa;
import br.com.mathinclui.tentativa.domain.Tentativa;

import java.math.BigDecimal;
import java.time.Instant;

public record TentativaResultadoResponse(
        Long id, StatusTentativa status, int quantidadeAcertos, int quantidadeErros,
        BigDecimal percentual, int tempoTotalSegundos, Instant finalizadaEm
) {
    public static TentativaResultadoResponse from(Tentativa tentativa) {
        return new TentativaResultadoResponse(tentativa.getId(), tentativa.getStatus(), tentativa.getQuantidadeAcertos(),
                tentativa.getQuantidadeErros(), tentativa.getPercentual(), tentativa.getTempoTotalSegundos(),
                tentativa.getFinalizadaEm());
    }
}
