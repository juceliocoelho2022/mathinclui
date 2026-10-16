package br.com.mathinclui.tentativa;

public record RespostaQuestaoResponse(
        boolean correta,
        boolean encerrada,
        int tentativasUsadas,
        int limiteTentativas,
        int dicasLiberadas,
        String dica,
        String explicacao,
        String respostaCorreta,
        boolean atividadeFinalizada
) {}
