package br.com.mathinclui.progresso;

import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.atribuicao.AtribuicaoService;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import br.com.mathinclui.resposta.domain.Resposta;
import br.com.mathinclui.resposta.repository.RespostaRepository;
import br.com.mathinclui.resposta.repository.RespostaTentativaRepository;
import br.com.mathinclui.tentativa.domain.Tentativa;
import br.com.mathinclui.tentativa.repository.TentativaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressoServiceTest {
    @Mock AtribuicaoService atribuicaoService;
    @Mock AtividadeRepository atividadeRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock TentativaRepository tentativaRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock RespostaTentativaRepository historicoRepository;
    @Mock AlunoRepository alunoRepository;
    private ProgressoService service;

    @BeforeEach
    void setUp() {
        service = new ProgressoService(atribuicaoService, atividadeRepository, questaoRepository,
                tentativaRepository, respostaRepository, historicoRepository, alunoRepository);
    }

    @Test
    void deve_calcular_participacao_media_e_metricas_sem_ranking() {
        var now = Instant.parse("2026-10-16T12:00:00Z");
        var atribuicao = new Atribuicao(40L, 1L, 20L, null, now.minusSeconds(60), null);
        setId(atribuicao, 50L);
        var atividade = new Atividade(1L, "Frações", null, (short) 7, "FRACOES",
                DificuldadeAtividade.BASICA, 3);
        atividade.publicar();
        setId(atividade, 40L);
        var questao = new Questao(40L, "Metade de 10", TipoQuestao.NUMERICA, "5",
                "Metade de 10 é 5", null, null, null, null, 1, null);
        setId(questao, 60L);
        var aluno1 = new Aluno(20L, "Ana", "ana_resultado", "hash");
        var aluno2 = new Aluno(20L, "Beto", "beto_resultado", "hash");
        setId(aluno1, 30L);
        setId(aluno2, 31L);
        var tentativa = new Tentativa(50L, 30L, now.minusSeconds(30));
        setId(tentativa, 70L);
        tentativa.finalizar(1, 0, new BigDecimal("100.00"), 15, now);
        var resposta = new Resposta(70L, 60L, "5", true, (short) 1, 15, now);

        when(atribuicaoService.obterDoProfessor(1L, 50L)).thenReturn(atribuicao);
        when(atividadeRepository.findById(40L)).thenReturn(Optional.of(atividade));
        when(alunoRepository.findAllByTurmaIdAndAtivoTrueOrderByNomeAsc(20L)).thenReturn(List.of(aluno1, aluno2));
        when(tentativaRepository.findAllByAtribuicaoId(50L)).thenReturn(List.of(tentativa));
        when(alunoRepository.findById(30L)).thenReturn(Optional.of(aluno1));
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(40L)).thenReturn(List.of(questao));
        when(respostaRepository.findAllByTentativaId(70L)).thenReturn(List.of(resposta));
        when(historicoRepository.countByTentativaId(70L)).thenReturn(1L);

        var resultado = service.resultados(1L, 50L);

        assertThat(resultado.totalAlunos()).isEqualTo(2);
        assertThat(resultado.participantes()).isEqualTo(1);
        assertThat(resultado.concluidos()).isEqualTo(1);
        assertThat(resultado.participacaoPercentual()).isEqualByComparingTo("50.00");
        assertThat(resultado.mediaPercentual()).isEqualByComparingTo("100.00");
        assertThat(resultado.alunos()).singleElement().satisfies(a -> {
            assertThat(a.nome()).isEqualTo("Ana");
            assertThat(a.totalSubmissoes()).isEqualTo(1);
            assertThat(a.totalDicas()).isEqualTo(1);
        });
        assertThat(resultado.questoes()).singleElement().satisfies(q -> {
            assertThat(q.questaoId()).isEqualTo(60L);
            assertThat(q.acertos()).isEqualTo(1);
            assertThat(q.errosFinais()).isZero();
        });
        assertThat(resultado.toString()).doesNotContainIgnoringCase("ranking");
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
