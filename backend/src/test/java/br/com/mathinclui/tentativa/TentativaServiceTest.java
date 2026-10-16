package br.com.mathinclui.tentativa;

import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.correcao.CorrecaoResultado;
import br.com.mathinclui.correcao.CorrecaoService;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import br.com.mathinclui.resposta.domain.Resposta;
import br.com.mathinclui.resposta.repository.RespostaRepository;
import br.com.mathinclui.resposta.repository.RespostaTentativaRepository;
import br.com.mathinclui.tentativa.domain.StatusTentativa;
import br.com.mathinclui.tentativa.domain.Tentativa;
import br.com.mathinclui.tentativa.repository.TentativaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TentativaServiceTest {
    @Mock AtribuicaoRepository atribuicaoRepository;
    @Mock AtividadeRepository atividadeRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock AlternativaRepository alternativaRepository;
    @Mock TentativaRepository tentativaRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock RespostaTentativaRepository historicoRepository;
    @Mock AlunoRepository alunoRepository;
    @Mock CorrecaoService correcaoService;

    private TentativaService service;
    private final Instant now = Instant.parse("2026-10-16T12:00:00Z");

    @BeforeEach
    void setUp() {
        service = new TentativaService(atribuicaoRepository, atividadeRepository, questaoRepository,
                alternativaRepository, tentativaRepository, respostaRepository, historicoRepository,
                alunoRepository, correcaoService, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void iniciar_deve_retomar_tentativa_existente_em_andamento() {
        var fixture = fixture(3, 1);
        var tentativa = new Tentativa(fixture.atribuicao.getId(), fixture.aluno.getId(), now.minusSeconds(60));
        setId(tentativa, 500L);
        prepararAcesso(fixture);
        when(tentativaRepository.findByAtribuicaoIdAndAlunoId(fixture.atribuicao.getId(), fixture.aluno.getId()))
                .thenReturn(Optional.of(tentativa));

        var response = service.iniciar(fixture.aluno.getId(), fixture.atribuicao.getId());

        assertThat(response.id()).isEqualTo(500L);
        assertThat(response.status()).isEqualTo(StatusTentativa.EM_ANDAMENTO);
    }

    @Test
    void erro_intermediario_deve_liberar_dica_sem_expor_gabarito() {
        var fixture = fixture(3, null);
        var tentativa = tentativa(fixture, 500L);
        prepararResposta(fixture, tentativa);
        when(correcaoService.corrigir(fixture.questao, "4")).thenReturn(new CorrecaoResultado(false, "4"));
        when(historicoRepository.countByTentativaIdAndQuestaoId(500L, fixture.questao.getId())).thenReturn(0L);
        when(respostaRepository.findByTentativaIdAndQuestaoId(500L, fixture.questao.getId())).thenReturn(Optional.empty());
        when(respostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(historicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.responder(fixture.aluno.getId(), 500L, fixture.questao.getId(),
                new RespostaQuestaoRequest("4", 12));

        assertThat(response.correta()).isFalse();
        assertThat(response.encerrada()).isFalse();
        assertThat(response.dica()).isEqualTo("Pense em metade de 10");
        assertThat(response.explicacao()).isNull();
        assertThat(response.respostaCorreta()).isNull();
        assertThat(tentativa.getQuantidadeErros()).isZero();
    }

    @Test
    void acerto_deve_encerrar_questao_revelar_explicacao_e_finalizar_atividade_de_uma_questao() {
        var fixture = fixture(3, 1);
        var tentativa = tentativa(fixture, 500L);
        prepararResposta(fixture, tentativa);
        when(correcaoService.corrigir(fixture.questao, "5")).thenReturn(new CorrecaoResultado(true, "5"));
        when(historicoRepository.countByTentativaIdAndQuestaoId(500L, fixture.questao.getId())).thenReturn(0L);
        when(respostaRepository.findByTentativaIdAndQuestaoId(500L, fixture.questao.getId())).thenReturn(Optional.empty());
        when(respostaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(historicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(fixture.atividade.getId())).thenReturn(List.of(fixture.questao));
        when(respostaRepository.findAllByTentativaId(500L)).thenAnswer(inv -> List.of(
                new Resposta(500L, fixture.questao.getId(), "5", true, (short) 0, 8, now)));

        var response = service.responder(fixture.aluno.getId(), 500L, fixture.questao.getId(),
                new RespostaQuestaoRequest("5", 8));

        assertThat(response.correta()).isTrue();
        assertThat(response.encerrada()).isTrue();
        assertThat(response.explicacao()).isEqualTo("Metade de 10 é 5");
        assertThat(response.respostaCorreta()).isEqualTo("5");
        assertThat(response.atividadeFinalizada()).isTrue();
        assertThat(tentativa.getStatus()).isEqualTo(StatusTentativa.FINALIZADA);
        assertThat(tentativa.getPercentual()).isEqualByComparingTo("100.00");
    }

    @Test
    void questao_ja_encerrada_deve_rejeitar_nova_submissao() {
        var fixture = fixture(3, 1);
        var tentativa = tentativa(fixture, 500L);
        prepararResposta(fixture, tentativa);
        when(respostaRepository.findByTentativaIdAndQuestaoId(500L, fixture.questao.getId()))
                .thenReturn(Optional.of(new Resposta(500L, fixture.questao.getId(), "5", true, (short) 0, 5, now)));

        assertThatThrownBy(() -> service.responder(fixture.aluno.getId(), 500L, fixture.questao.getId(),
                new RespostaQuestaoRequest("5", 2)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("encerrada");
    }

    private void prepararAcesso(Fixture f) {
        when(alunoRepository.findById(f.aluno.getId())).thenReturn(Optional.of(f.aluno));
        when(atribuicaoRepository.findForUpdate(f.atribuicao.getId())).thenReturn(Optional.of(f.atribuicao));
        when(atividadeRepository.findById(f.atividade.getId())).thenReturn(Optional.of(f.atividade));
    }

    private void prepararResposta(Fixture f, Tentativa tentativa) {
        when(tentativaRepository.findForUpdate(500L, f.aluno.getId())).thenReturn(Optional.of(tentativa));
        when(atribuicaoRepository.findById(f.atribuicao.getId())).thenReturn(Optional.of(f.atribuicao));
        when(atividadeRepository.findById(f.atividade.getId())).thenReturn(Optional.of(f.atividade));
        when(questaoRepository.findByIdAndAtividadeId(f.questao.getId(), f.atividade.getId())).thenReturn(Optional.of(f.questao));
    }

    private Tentativa tentativa(Fixture f, Long id) {
        var t = new Tentativa(f.atribuicao.getId(), f.aluno.getId(), now.minusSeconds(30));
        setId(t, id);
        return t;
    }

    private Fixture fixture(int limiteAtividade, Integer limiteQuestao) {
        var aluno = new Aluno(20L, "Ana", "ana_teste", "hash");
        setId(aluno, 30L);
        var atividade = new Atividade(1L, "Frações", null, (short) 7, "FRACOES",
                DificuldadeAtividade.BASICA, limiteAtividade);
        atividade.publicar();
        setId(atividade, 40L);
        var atribuicao = new Atribuicao(40L, 1L, null, 30L, now.minusSeconds(120), now.plusSeconds(3600));
        setId(atribuicao, 50L);
        var questao = new Questao(40L, "Qual é a metade de 10?", TipoQuestao.NUMERICA, "5",
                "Metade de 10 é 5", "Pense em metade de 10", "Divida por 2", null, null, 1, limiteQuestao);
        setId(questao, 60L);
        return new Fixture(aluno, atividade, atribuicao, questao);
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

    private record Fixture(Aluno aluno, Atividade atividade, Atribuicao atribuicao, Questao questao) {}
}
