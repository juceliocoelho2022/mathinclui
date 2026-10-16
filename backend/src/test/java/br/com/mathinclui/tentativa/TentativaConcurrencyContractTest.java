package br.com.mathinclui.tentativa;

import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.correcao.CorrecaoService;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TentativaConcurrencyContractTest {
    interface LockingAtribuicaoRepository extends AtribuicaoRepository {
        Optional<Atribuicao> findForUpdate(Long id);
    }

    @Mock LockingAtribuicaoRepository atribuicaoRepository;
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
    void iniciar_deve_bloquear_atribuicao_antes_de_verificar_tentativa_existente() {
        var fixture = fixture();
        var existente = new Tentativa(fixture.atribuicao.getId(), fixture.aluno.getId(), now.minusSeconds(30));
        setId(existente, 500L);

        when(alunoRepository.findById(fixture.aluno.getId())).thenReturn(Optional.of(fixture.aluno));
        when(atribuicaoRepository.findById(fixture.atribuicao.getId()))
                .thenThrow(new IllegalStateException("consulta sem lock não é permitida neste contrato"));
        when(atribuicaoRepository.findForUpdate(fixture.atribuicao.getId())).thenReturn(Optional.of(fixture.atribuicao));
        when(atividadeRepository.findById(fixture.atividade.getId())).thenReturn(Optional.of(fixture.atividade));
        when(tentativaRepository.findByAtribuicaoIdAndAlunoId(fixture.atribuicao.getId(), fixture.aluno.getId()))
                .thenReturn(Optional.of(existente));

        var response = service.iniciar(fixture.aluno.getId(), fixture.atribuicao.getId());

        assertThat(response.id()).isEqualTo(500L);
    }

    @Test
    void responder_deve_bloquear_tentativa_antes_de_calcular_proxima_submissao() {
        var fixture = fixture();
        var tentativa = new Tentativa(fixture.atribuicao.getId(), fixture.aluno.getId(), now.minusSeconds(30));
        setId(tentativa, 500L);
        var encerrada = new Resposta(500L, fixture.questao.getId(), "5", true, (short) 0, 5, now);

        when(tentativaRepository.findByIdAndAlunoId(500L, fixture.aluno.getId()))
                .thenThrow(new IllegalStateException("consulta sem lock não é permitida neste contrato"));
        when(tentativaRepository.findForUpdate(500L, fixture.aluno.getId())).thenReturn(Optional.of(tentativa));
        when(atribuicaoRepository.findById(fixture.atribuicao.getId())).thenReturn(Optional.of(fixture.atribuicao));
        when(atividadeRepository.findById(fixture.atividade.getId())).thenReturn(Optional.of(fixture.atividade));
        when(questaoRepository.findByIdAndAtividadeId(fixture.questao.getId(), fixture.atividade.getId()))
                .thenReturn(Optional.of(fixture.questao));
        when(respostaRepository.findByTentativaIdAndQuestaoId(500L, fixture.questao.getId()))
                .thenReturn(Optional.of(encerrada));

        assertThatThrownBy(() -> service.responder(fixture.aluno.getId(), 500L, fixture.questao.getId(),
                new RespostaQuestaoRequest("5", 1)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("encerrada");
    }

    private Fixture fixture() {
        var aluno = new Aluno(20L, "Ana", "ana_lock", "hash");
        setId(aluno, 30L);
        var atividade = new Atividade(1L, "Frações", null, (short) 7, "FRACOES",
                DificuldadeAtividade.BASICA, 3);
        atividade.publicar();
        setId(atividade, 40L);
        var atribuicao = new Atribuicao(40L, 1L, null, 30L, now.minusSeconds(120), now.plusSeconds(3600));
        setId(atribuicao, 50L);
        var questao = new Questao(40L, "Qual é a metade de 10?", TipoQuestao.NUMERICA, "5",
                "Metade de 10 é 5", "Pense em metade de 10", null, null, null, 1, null);
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
