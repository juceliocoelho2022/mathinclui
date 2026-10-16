package br.com.mathinclui.questao;

import br.com.mathinclui.questao.domain.TipoQuestao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuestaoRequest(
        @NotBlank String enunciado,
        @NotNull TipoQuestao tipo,
        String respostaCorreta,
        String explicacao,
        String dica1,
        String dica2,
        String dica3,
        String imagemUrl,
        @Min(1) int ordem,
        @Min(1) Integer limiteTentativas,
        @Valid List<AlternativaRequest> alternativas
) {}
