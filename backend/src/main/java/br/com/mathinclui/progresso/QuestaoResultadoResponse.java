package br.com.mathinclui.progresso;

public record QuestaoResultadoResponse(
        Long questaoId,
        String enunciado,
        int acertos,
        int errosFinais,
        long totalSubmissoes
) {}
