package br.com.mathinclui.correcao;

import br.com.mathinclui.questao.domain.Alternativa;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrecaoServiceTest {
    @Mock AlternativaRepository alternativaRepository;
    private CorrecaoService service;

    @BeforeEach
    void setUp() { service = new CorrecaoService(alternativaRepository); }

    @Test
    void deve_corrigir_multipla_escolha_por_alternativa_da_propria_questao() {
        var questao = questao(TipoQuestao.MULTIPLA_ESCOLHA, null, 10L);
        var alternativa = new Alternativa(10L, "50%", true, 1);
        when(alternativaRepository.findByIdAndQuestaoId(20L, 10L)).thenReturn(Optional.of(alternativa));
        assertThat(service.corrigir(questao, "20").correta()).isTrue();
    }

    @Test
    void deve_rejeitar_alternativa_que_nao_pertence_a_questao() {
        var questao = questao(TipoQuestao.MULTIPLA_ESCOLHA, null, 10L);
        when(alternativaRepository.findByIdAndQuestaoId(99L, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.corrigir(questao, "99"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deve_tratar_representacoes_numericas_equivalentes() {
        var questao = questao(TipoQuestao.NUMERICA, "2.00", 10L);
        assertThat(service.corrigir(questao, "2").correta()).isTrue();
        assertThat(service.corrigir(questao, "2.0").correta()).isTrue();
        assertThat(service.corrigir(questao, "2,00").correta()).isTrue();
    }

    @Test
    void deve_rejeitar_numero_com_separadores_ambiguos() {
        var questao = questao(TipoQuestao.NUMERICA, "1234.5", 10L);
        assertThatThrownBy(() -> service.corrigir(questao, "1,234.5"))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.corrigir(questao, "1.234,5"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void texto_curto_ignora_caixa_e_espacos_mas_preserva_acentos() {
        var questao = questao(TipoQuestao.TEXTO_CURTO, "fração própria", 10L);
        assertThat(service.corrigir(questao, "  FRAÇÃO   PRÓPRIA ").correta()).isTrue();
        assertThat(service.corrigir(questao, "fracao propria").correta()).isFalse();
    }

    private Questao questao(TipoQuestao tipo, String correta, Long id) {
        var q = new Questao(1L, "Questão", tipo, correta, null, null, null, null, null, 1, null);
        try {
            Field f = Questao.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(q, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        return q;
    }
}
