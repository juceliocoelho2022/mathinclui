package br.com.mathinclui.atividade.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtividadeTest {

    @Test
    void atividade_deve_iniciar_com_limite_tres_quando_informado_tres() {
        var atividade = new Atividade(10L, "Porcentagem", "Prática", (short) 7,
                "PORCENTAGEM", DificuldadeAtividade.BASICA, 3);

        assertThat(atividade.getLimiteTentativas()).isEqualTo(3);
        assertThat(atividade.getTipoOrigem()).isEqualTo(TipoOrigemAtividade.PROFESSOR);
        assertThat(atividade.isPublicada()).isFalse();
    }

    @Test
    void limites_menores_que_um_devem_ser_rejeitados() {
        assertThatThrownBy(() -> new Atividade(10L, "Título", null, (short) 6,
                "NUMEROS", DificuldadeAtividade.BASICA, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tentativas");
    }
}
