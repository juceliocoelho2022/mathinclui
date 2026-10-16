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
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestaoModeloServiceTest {
    @Mock AtividadeService atividadeService;
    @Mock QuestaoRepository questaoRepository;
    @Mock AlternativaRepository alternativaRepository;
    @Mock QuestaoModeloRepository modeloRepository;
    @Mock AlternativaModeloRepository alternativaModeloRepository;
    @Mock HabilidadeBnccRepository habilidadeRepository;
    @Mock TagProfessorRepository tagRepository;
    @Mock NamedParameterJdbcTemplate jdbc;

    private QuestaoModeloService service;
    private final Instant now = Instant.parse("2026-10-16T12:00:00Z");

    @BeforeEach
    void setUp() {
        service = new QuestaoModeloService(atividadeService, questaoRepository, alternativaRepository,
                modeloRepository, alternativaModeloRepository, habilidadeRepository, tagRepository, jdbc,
                Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void salvarQuestaoConcretaCriaModeloIndependenteMesmoComHistorico() throws Exception {
        Atividade atividade = new Atividade(1L, "Frações", null, (short) 7, "FRACOES", DificuldadeAtividade.BASICA, 3);
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(atividade);
        Questao questao = new Questao(10L, "Quanto é metade de 10?", TipoQuestao.NUMERICA, "5",
                "Divida por 2", "Pense em dois grupos", null, null, null, 1, 2);
        setId(questao, 20L);
        when(questaoRepository.findByIdAndAtividadeId(20L, 10L)).thenReturn(Optional.of(questao));
        when(modeloRepository.save(any())).thenAnswer(inv -> { var m = (QuestaoModelo) inv.getArgument(0); setId(m, 100L); return m; });

        var response = service.salvarNoBanco(1L, 10L, 20L,
                new SalvarQuestaoNoBancoRequest((short) 7, "FRACOES", DificuldadeAtividade.BASICA, List.of(), List.of()));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.tipoOrigem()).isEqualTo(TipoOrigemBiblioteca.PROFESSOR);
        assertThat(response.respostaCorreta()).isEqualTo("5");
        verify(atividadeService, never()).validarSemHistorico(anyLong());
    }

    @Test
    void salvarMultiplaEscolhaCopiaAlternativasEGabaritoSomenteParaModeloDoProfessor() throws Exception {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(
                new Atividade(1L, "A", null, (short) 6, "NUMEROS", DificuldadeAtividade.BASICA, 3));
        Questao questao = new Questao(10L, "Escolha", TipoQuestao.MULTIPLA_ESCOLHA, null,
                "Explica", null, null, null, null, 1, null);
        setId(questao, 20L);
        when(questaoRepository.findByIdAndAtividadeId(20L, 10L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(20L)).thenReturn(List.of(
                new Alternativa(20L, "A", false, 1), new Alternativa(20L, "B", true, 2)));
        when(modeloRepository.save(any())).thenAnswer(inv -> { var m=(QuestaoModelo) inv.getArgument(0); setId(m, 101L); return m; });
        when(alternativaModeloRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.salvarNoBanco(1L, 10L, 20L,
                new SalvarQuestaoNoBancoRequest((short) 6, "NUMEROS", DificuldadeAtividade.BASICA, List.of(), List.of()));

        assertThat(response.alternativas()).hasSize(2);
        assertThat(response.alternativas()).anyMatch(a -> a.correta() && a.texto().equals("B"));
    }

    @Test
    void copiaModeloSistemaComoModeloPessoalComProveniencia() throws Exception {
        QuestaoModelo fonte = new QuestaoModelo(null, TipoOrigemBiblioteca.SISTEMA, "2+2?", TipoQuestao.NUMERICA,
                "4", "Some", null, null, null, null, 3, (short) 6, "NUMEROS", DificuldadeAtividade.BASICA);
        setId(fonte, 50L);
        when(modeloRepository.findById(50L)).thenReturn(Optional.of(fonte));
        when(modeloRepository.save(any())).thenAnswer(inv -> { var m=(QuestaoModelo) inv.getArgument(0); setId(m, 51L); return m; });
        when(alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(50L)).thenReturn(List.of());

        var response = service.copiar(1L, 50L);

        assertThat(response.id()).isEqualTo(51L);
        assertThat(response.professorId()).isEqualTo(1L);
        assertThat(response.origemModeloId()).isEqualTo(50L);
        assertThat(response.copiadoEm()).isEqualTo(now);
    }

    @Test
    void outroProfessorNaoCopiaModeloPessoal() throws Exception {
        QuestaoModelo fonte = new QuestaoModelo(99L, TipoOrigemBiblioteca.PROFESSOR, "2+2?", TipoQuestao.NUMERICA,
                "4", null, null, null, null, null, 3, (short) 6, "NUMEROS", DificuldadeAtividade.BASICA);
        setId(fonte, 50L);
        when(modeloRepository.findById(50L)).thenReturn(Optional.of(fonte));

        assertThatThrownBy(() -> service.copiar(1L, 50L))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
        verify(modeloRepository, never()).save(any());
    }

    @Test
    void importarAnexaDepoisDaMaiorOrdemEPreservaIndependencia() throws Exception {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(
                new Atividade(1L, "A", null, (short) 7, "ALGEBRA", DificuldadeAtividade.BASICA, 3));
        Questao existente = new Questao(10L, "Existente", TipoQuestao.NUMERICA, "1", null, null, null, null, null, 3, null);
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(10L)).thenReturn(List.of(existente));
        QuestaoModelo modelo = new QuestaoModelo(null, TipoOrigemBiblioteca.SISTEMA, "x+1=3", TipoQuestao.TEXTO_CURTO,
                "x=2", "Subtraia 1", "Isole x", null, null, null, 2, (short) 7, "ALGEBRA", DificuldadeAtividade.INTERMEDIARIA);
        setId(modelo, 70L);
        when(modeloRepository.findById(70L)).thenReturn(Optional.of(modelo));
        when(questaoRepository.save(any())).thenAnswer(inv -> { var q=(Questao) inv.getArgument(0); setId(q, 700L); return q; });
        when(alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(70L)).thenReturn(List.of());

        var novas = service.importar(1L, 10L, new ImportarQuestoesRequest(List.of(70L)));

        assertThat(novas).hasSize(1);
        var captor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(captor.capture());
        assertThat(captor.getValue().getOrdem()).isEqualTo(4);
        assertThat(captor.getValue().getOrigemQuestaoModeloId()).isEqualTo(70L);
        assertThat(captor.getValue().getEnunciado()).isEqualTo("x+1=3");
        verify(atividadeService).validarSemHistorico(10L);
    }

    @Test
    void importarRejeitaIdsDuplicadosAntesDeMutar() {
        assertThatThrownBy(() -> service.importar(1L, 10L, new ImportarQuestoesRequest(List.of(7L, 7L))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        verifyNoInteractions(atividadeService, questaoRepository, modeloRepository);
    }

    @Test
    void importarPropagaBloqueioDeHistoricoAntesDeSalvar() {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(
                new Atividade(1L, "A", null, (short) 7, "ALGEBRA", DificuldadeAtividade.BASICA, 3));
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "histórico"))
                .when(atividadeService).validarSemHistorico(10L);

        assertThatThrownBy(() -> service.importar(1L, 10L, new ImportarQuestoesRequest(List.of(7L))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
        verify(questaoRepository, never()).save(any());
    }

    private static void setId(Object entity, long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
