package br.com.mathinclui.correcao;

import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;

@Service
public class CorrecaoService {
    private final AlternativaRepository alternativaRepository;

    public CorrecaoService(AlternativaRepository alternativaRepository) {
        this.alternativaRepository = alternativaRepository;
    }

    public CorrecaoResultado corrigir(Questao questao, String resposta) {
        if (questao == null || resposta == null || resposta.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resposta é obrigatória.");
        }
        return switch (questao.getTipo()) {
            case MULTIPLA_ESCOLHA -> corrigirMultiplaEscolha(questao, resposta);
            case NUMERICA -> corrigirNumerica(questao, resposta);
            case TEXTO_CURTO -> corrigirTexto(questao, resposta);
        };
    }

    private CorrecaoResultado corrigirMultiplaEscolha(Questao questao, String resposta) {
        final long alternativaId;
        try {
            alternativaId = Long.parseLong(resposta.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alternativa inválida.");
        }
        var alternativa = alternativaRepository.findByIdAndQuestaoId(alternativaId, questao.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Alternativa não pertence à questão."));
        return new CorrecaoResultado(alternativa.isCorreta(), Long.toString(alternativaId));
    }

    private CorrecaoResultado corrigirNumerica(Questao questao, String resposta) {
        var esperado = decimal(questao.getRespostaCorreta());
        var recebido = decimal(resposta);
        return new CorrecaoResultado(esperado.compareTo(recebido) == 0, recebido.toPlainString());
    }

    private BigDecimal decimal(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Questão sem resposta numérica configurada.");
        }
        var texto = valor.trim();
        if (texto.contains(".") && texto.contains(",")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato numérico ambíguo.");
        }
        texto = texto.replace(',', '.');
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resposta numérica inválida.");
        }
    }

    private CorrecaoResultado corrigirTexto(Questao questao, String resposta) {
        var esperado = normalizarTexto(questao.getRespostaCorreta());
        var recebido = normalizarTexto(resposta);
        return new CorrecaoResultado(esperado.equals(recebido), recebido);
    }

    private String normalizarTexto(String valor) {
        if (valor == null) return "";
        var nfc = Normalizer.normalize(valor, Normalizer.Form.NFC);
        return nfc.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
