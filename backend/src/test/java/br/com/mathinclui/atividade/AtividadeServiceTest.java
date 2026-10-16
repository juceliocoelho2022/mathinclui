package br.com.mathinclui.atividade;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.questao.domain.Alternativa;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeServiceTest {
    @Mock AtividadeRepository atividadeRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock AlternativaRepository alternativaRepository;
    private AtividadeService service;

    @BeforeEach
    void setUp() {
        service = new AtividadeService(atividadeRepository, questaoRepository, alternativaRepository);
    }

    @Test
    void professor_nao_pode_editar_atividade_de_outro_professor() {
        when(atividadeRepository.findByIdAndProfessorId(90L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(2L, 90L,
                new UpdateAtividadeRequest("X", null, (short) 7, "NUMEROS", DificuldadeAtividade.BASICA, 3)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void publicar_falha_sem_questoes() {
        var atividade = atividade(1L, 10L);
        when(atividadeRepository.findByIdAndProfessorId(10L, 1L)).thenReturn(Optional.of(atividade));
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.publicar(1L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("questão");
    }

    @Test
    void publicar_multipla_escolha_exige_duas_alternativas_e_uma_correta() {
        var atividade = atividade(1L, 10L);
        var questao = new Questao(10L, "Escolha", TipoQuestao.MULTIPLA_ESCOLHA, null,
                "Explicação", null, null, null, null, 1, null);
        setId(questao, 20L);
        when(atividadeRepository.findByIdAndProfessorId(10L, 1L)).thenReturn(Optional.of(atividade));
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(10L)).thenReturn(List.of(questao));
        when(alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(20L))
                .thenReturn(List.of(new Alternativa(20L, "A", false, 1)));

        assertThatThrownBy(() -> service.publicar(1L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("alternativas");
    }

    @Test
    void atividade_com_historico_rejeita_exclusao() {
        var atividade = atividade(1L, 10L);
        when(atividadeRepository.findByIdAndProfessorId(10L, 1L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.possuiHistorico(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("histórico");
    }

    private Atividade atividade(Long professorId, Long id) {
        var atividade = new Atividade(professorId, "Atividade", null, (short) 7,
                "NUMEROS", DificuldadeAtividade.BASICA, 3);
        setId(atividade, id);
        return atividade;
    }

    private void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
