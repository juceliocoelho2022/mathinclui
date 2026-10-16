package br.com.mathinclui.progresso;

import br.com.mathinclui.tentativa.domain.StatusTentativa;

import java.math.BigDecimal;

public record AlunoResultadoResponse(
        Long alunoId,
        String nome,
        StatusTentativa status,
        int acertos,
        int erros,
        BigDecimal percentual,
        int tempoTotalSegundos,
        long totalSubmissoes,
        int totalDicas
) {}
