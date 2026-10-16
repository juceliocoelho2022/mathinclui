package br.com.mathinclui.questao.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestaoTest {

    @Test
    void questao_deve_herdar_limite_da_atividade_quando_override_nulo() {
        var questao = novaQuestao(null);
        assertThat(questao.limiteEfetivo(3)).isEqualTo(3);
    }

    @Test
    void questao_deve_usar_override_quando_presente() {
        var questao = novaQuestao(5);
        assertThat(questao.limiteEfetivo(3)).isEqualTo(5);
    }

    @Test
    void limites_menores_que_um_devem_ser_rejeitados() {
        assertThatThrownBy(() -> novaQuestao(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tentativas");
        assertThatThrownBy(() -> novaQuestao(null).limiteEfetivo(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tentativas");
    }

    private Questao novaQuestao(Integer limite) {
        return new Questao(2L, "Quanto é 10% de 50?", TipoQuestao.NUMERICA, "5",
                "10% de 50 é 5", "Divida por 10", null, null, null, 1, limite);
    }
}
