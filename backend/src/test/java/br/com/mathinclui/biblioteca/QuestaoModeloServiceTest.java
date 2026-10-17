package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.biblioteca.domain.AlternativaModelo;
import br.com.mathinclui.biblioteca.domain.QuestaoModelo;
import br.com.mathinclui.biblioteca.domain.TipoOrigemBiblioteca;
import br.com.mathinclui.biblioteca.repository.AlternativaModeloRepository;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.QuestaoModeloRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import br.com.mathinclui.questao.domain.Alternativa;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestaoModeloServiceTest {
    @Mock AtividadeService atividadeService;
    @Mock QuestaoRepository questaoRepository;
    @Mock AlternativaRepository alternativaRepository;
    @Mock QuestaoModeloRepository modeloRepository;
    @Mock AlternativaModeloRepository alternativaModeloRepository;
    @Mock HabilidadeBnccRepository habilidadeRepository;
    @Mock TagProfessorRepository tagRepository;
    @Mock JdbcTemplate jdbcTemplate;
    private QuestaoModeloService service;

    @BeforeEach
    void setUp() {
        service = new QuestaoModeloService(atividadeService, questaoRepository, alternativaRepository,
                modeloRepository, alternativaModeloRepository, habilidadeRepository, tagRepository,
                jdbcTemplate, Clock.fixed(Instant.parse("2026-10-17T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void salvarQuestaoNoBancoDeveCriarModeloIndependenteComAlternativas() {
        var atividade = atividade(1L, 10L);
        var questao = questao(10L, 20L, 1);
        var a1 = alternativa(20L, 101L, "3", false, 1);
        var a2 = alternativa(20L, 102L, "4", true, 2);
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(atividade);
        when(questaoRepository.findByIdAndAtividadeId(20L, 10L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(20L)).thenReturn(List.of(a1, a2));
        when(modeloRepository.save(any(QuestaoModelo.class))).thenAnswer(inv -> {
            var m = inv.getArgument(0, QuestaoModelo.class); ReflectionTestUtils.setField(m, "id", 30L); return m;
        });
        when(habilidadeRepository.findAllById(List.of())).thenReturn(List.of());

        var result = service.salvarNoBanco(1L, 10L, 20L,
                new SalvarQuestaoNoBancoRequest((short)7, "NUMEROS", DificuldadeAtividade.BASICA, List.of(), List.of()));

        assertThat(result.id()).isEqualTo(30L);
        assertThat(result.professorId()).isEqualTo(1L);
        assertThat(result.tipoOrigem()).isEqualTo(TipoOrigemBiblioteca.PROFESSOR);
        assertThat(result.enunciado()).isEqualTo("Quanto é 2 + 2?");
        var captor = ArgumentCaptor.forClass(AlternativaModelo.class);
        verify(alternativaModeloRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(AlternativaModelo::getTexto).containsExactly("3", "4");
        assertThat(captor.getAllValues()).extracting(AlternativaModelo::isCorreta).containsExactly(false, true);
        verify(atividadeService, never()).validarSemHistorico(10L);
    }

    @Test
    void copiarModeloSistemaDeveCriarModeloPessoalComProveniencia() {
        var origem = modeloSistema(40L);
        when(modeloRepository.findById(40L)).thenReturn(Optional.of(origem));
        when(alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(40L)).thenReturn(List.of());
        when(modeloRepository.save(any(QuestaoModelo.class))).thenAnswer(inv -> {
            var m = inv.getArgument(0, QuestaoModelo.class); ReflectionTestUtils.setField(m, "id", 41L); return m;
        });

        var copia = service.copiarModelo(1L, 40L);

        assertThat(copia.id()).isEqualTo(41L);
        assertThat(copia.professorId()).isEqualTo(1L);
        assertThat(copia.tipoOrigem()).isEqualTo(TipoOrigemBiblioteca.PROFESSOR);
        assertThat(copia.origemModeloId()).isEqualTo(40L);
    }

    @Test
    void modeloPessoalDeOutroProfessorNaoDeveSerVisivelNemCopiavel() {
        var outro = modeloProfessor(99L, 50L);
        when(modeloRepository.findById(50L)).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.copiarModelo(1L, 50L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    void importarDeveAdicionarAposMaiorOrdemEPreservarSelecao() {
        var atividade = atividade(1L, 10L);
        var existente = new Questao(10L, "Existente", TipoQuestao.NUMERICA, "1", null, null, null, null, null, 3, 2);
        ReflectionTestUtils.setField(existente, "id", 70L);
        var m1 = modeloSistema(80L);
        var m2 = modeloSistema(81L);
        ReflectionTestUtils.setField(m2, "enunciado", "Segunda questão");
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(atividade);
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(10L)).thenReturn(List.of(existente));
        when(modeloRepository.findById(80L)).thenReturn(Optional.of(m1));
        when(modeloRepository.findById(81L)).thenReturn(Optional.of(m2));
        when(alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(anyLong())).thenReturn(List.of());
        when(questaoRepository.save(any(Questao.class))).thenAnswer(inv -> {
            var q = inv.getArgument(0, Questao.class); ReflectionTestUtils.setField(q, "id", 100L + q.getOrdem()); return q;
        });

        var imported = service.importar(1L, 10L, new ImportarQuestoesRequest(List.of(80L, 81L)));

        assertThat(imported).extracting(QuestaoImportadaResponse::ordem).containsExactly(4, 5);
        assertThat(imported).extracting(QuestaoImportadaResponse::enunciado).containsExactly("Quanto é 2 + 2?", "Segunda questão");
        verify(atividadeService).validarSemHistorico(10L);
    }

    @Test
    void importarDeveRejeitarIdsDuplicadosAntesDeGravar() {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(atividade(1L, 10L));

        assertThatThrownBy(() -> service.importar(1L, 10L, new ImportarQuestoesRequest(List.of(80L, 80L))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("repetido");

        verify(questaoRepository, never()).save(any());
    }

    private Atividade atividade(Long professorId, Long id) {
        var a = new Atividade(professorId, "Atividade", null, (short)7, "NUMEROS", DificuldadeAtividade.BASICA, 3);
        ReflectionTestUtils.setField(a, "id", id); return a;
    }

    private Questao questao(Long atividadeId, Long id, int ordem) {
        var q = new Questao(atividadeId, "Quanto é 2 + 2?", TipoQuestao.MULTIPLA_ESCOLHA, null,
                "Explique a soma.", "Conte duas vezes.", null, null, null, ordem, 3);
        ReflectionTestUtils.setField(q, "id", id); return q;
    }

    private Alternativa alternativa(Long questaoId, Long id, String texto, boolean correta, int ordem) {
        var a = new Alternativa(questaoId, texto, correta, ordem); ReflectionTestUtils.setField(a, "id", id); return a;
    }

    private QuestaoModelo modeloSistema(Long id) {
        var m = new QuestaoModelo(null, TipoOrigemBiblioteca.SISTEMA, "Quanto é 2 + 2?", TipoQuestao.NUMERICA,
                "4", "Explique a soma.", "Conte duas vezes.", null, null, null, 3,
                (short)7, "NUMEROS", DificuldadeAtividade.BASICA);
        ReflectionTestUtils.setField(m, "id", id); return m;
    }

    private QuestaoModelo modeloProfessor(Long professorId, Long id) {
        var m = new QuestaoModelo(professorId, TipoOrigemBiblioteca.PROFESSOR, "Pessoal", TipoQuestao.NUMERICA,
                "4", null, null, null, null, null, 3, (short)7, "NUMEROS", DificuldadeAtividade.BASICA);
        ReflectionTestUtils.setField(m, "id", id); return m;
    }
}
