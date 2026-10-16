package br.com.mathinclui.atividade;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateAtividadeRequestTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void json_sem_limite_de_tentativas_deve_usar_padrao_tres() throws Exception {
        var request = objectMapper.readValue("""
                {
                  "titulo": "Frações",
                  "anoEscolar": 7,
                  "conteudo": "FRACOES",
                  "dificuldade": "BASICA"
                }
                """, CreateAtividadeRequest.class);

        assertThat(request.limiteTentativas()).isEqualTo(3);
    }
}
