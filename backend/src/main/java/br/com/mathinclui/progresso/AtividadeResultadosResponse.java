package br.com.mathinclui.progresso;

import java.math.BigDecimal;
import java.util.List;

public record AtividadeResultadosResponse(
        Long atribuicaoId,
        Long atividadeId,
        String titulo,
        int totalAlunos,
        int participantes,
        int concluidos,
        BigDecimal participacaoPercentual,
        BigDecimal mediaPercentual,
        List<AlunoResultadoResponse> alunos,
        List<QuestaoResultadoResponse> questoes
) {}
