package br.com.mathinclui.biblioteca;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BibliotecaAtividadeServiceTest {
    @Mock AtividadeRepository atividadeRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock AlternativaRepository alternativaRepository;
    @Mock JdbcTemplate jdbcTemplate;
    private BibliotecaAtividadeService service;

    @BeforeEach void setUp(){ service = new BibliotecaAtividadeService(atividadeRepository, questaoRepository, alternativaRepository, jdbcTemplate, Clock.fixed(Instant.parse("2026-10-16T00:00:00Z"), ZoneOffset.UTC)); }

    @Test
    void listarDeveIncluirSistemaPublicadoEConteudoProprioSemConteudoDeOutroProfessor() {
        var propria = atividade(1L, 10L, "Minha");
        var sistema = atividadeSistema(20L, "Oficial");
        when(atividadeRepository.findAllByProfessorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(propria));
        when(atividadeRepository.findAllByTipoOrigemAndPublicadaTrueOrderByTituloAsc(TipoOrigemAtividade.SISTEMA)).thenReturn(List.of(sistema));

        var page = service.listar(1L, new BibliotecaAtividadeFiltro(null,null,null,null,null,null,null,null,null,0,20));

        assertThat(page.items()).extracting(BibliotecaAtividadeResumoResponse::titulo).containsExactlyInAnyOrder("Minha", "Oficial");
    }

    @Test
    void buscaTextualDeveEncontrarAtividadePorMetadadoBncc() {
        var atividade = atividade(1L, 10L, "Exercícios");
        when(atividadeRepository.findAllByProfessorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(atividade));
        when(atividadeRepository.findAllByTipoOrigemAndPublicadaTrueOrderByTituloAsc(TipoOrigemAtividade.SISTEMA)).thenReturn(List.of());
        lenient().when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(1);

        var page = service.listar(1L, new BibliotecaAtividadeFiltro(null,null,null,null,null,null,null,null,"EF07MA01",0,20));

        assertThat(page.items()).extracting(BibliotecaAtividadeResumoResponse::id).containsExactly(10L);
    }

    @Test
    void detalharNaoDeveExporAtividadePessoalDeOutroProfessor() {
        var outra = atividade(99L, 30L, "Outra");
        when(atividadeRepository.findById(30L)).thenReturn(Optional.of(outra));
        assertThatThrownBy(() -> service.detalhar(1L, 30L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void copiarDeveCriarRascunhoIndependenteComProveniencia() {
        var origem = atividadeSistema(20L, "Oficial");
        when(atividadeRepository.findById(20L)).thenReturn(Optional.of(origem));
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> {
            Atividade a=inv.getArgument(0); ReflectionTestUtils.setField(a,"id",40L); return a;
        });

        var copia = service.copiar(1L, 20L);

        assertThat(copia.id()).isEqualTo(40L);
        assertThat(copia.professorId()).isEqualTo(1L);
        assertThat(copia.publicada()).isFalse();
        assertThat(copia.origemAtividadeId()).isEqualTo(20L);
    }

    @Test
    void copiarDeveDuplicarClassificacaoQuestoesEAlternativasSemCompartilharLinhas() {
        var origem = atividadeSistema(20L, "Oficial");
        var questao = new Questao(20L, "Quanto é 2 + 2?", TipoQuestao.MULTIPLA_ESCOLHA, null,
                "Somar duas unidades duas vezes.", "Conte 2 e mais 2.", null, null, null, 1, 3);
        ReflectionTestUtils.setField(questao, "id", 100L);
        var alternativaErrada = new Alternativa(100L, "3", false, 1);
        var alternativaCorreta = new Alternativa(100L, "4", true, 2);

        when(atividadeRepository.findById(20L)).thenReturn(Optional.of(origem));
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> {
            Atividade a = inv.getArgument(0); ReflectionTestUtils.setField(a, "id", 40L); return a;
        });
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(20L)).thenReturn(List.of(questao));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(inv -> {
            Questao q = inv.getArgument(0); ReflectionTestUtils.setField(q, "id", 200L); return q;
        });
        when(alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(100L))
                .thenReturn(List.of(alternativaErrada, alternativaCorreta));

        service.copiar(1L, 20L);

        var questaoCaptor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(questaoCaptor.capture());
        assertThat(questaoCaptor.getValue().getAtividadeId()).isEqualTo(40L);
        assertThat(questaoCaptor.getValue().getEnunciado()).isEqualTo("Quanto é 2 + 2?");
        assertThat(questaoCaptor.getValue().getExplicacao()).isEqualTo("Somar duas unidades duas vezes.");

        var alternativaCaptor = ArgumentCaptor.forClass(Alternativa.class);
        verify(alternativaRepository, times(2)).save(alternativaCaptor.capture());
        assertThat(alternativaCaptor.getAllValues()).allSatisfy(a -> assertThat(a.getQuestaoId()).isEqualTo(200L));
        assertThat(alternativaCaptor.getAllValues()).extracting(Alternativa::getTexto).containsExactly("3", "4");
        assertThat(alternativaCaptor.getAllValues()).extracting(Alternativa::isCorreta).containsExactly(false, true);

        verify(jdbcTemplate, times(2)).update(anyString(), eq(40L), eq(20L));
    }

    private Atividade atividade(Long professorId, Long id, String titulo){
        var a=new Atividade(professorId,titulo,null,(short)7,"NUMEROS", DificuldadeAtividade.BASICA,3);
        ReflectionTestUtils.setField(a,"id",id); return a;
    }
    private Atividade atividadeSistema(Long id,String titulo){
        var a=new Atividade(1L,titulo,null,(short)7,"NUMEROS", DificuldadeAtividade.BASICA,3);
        ReflectionTestUtils.setField(a,"id",id); ReflectionTestUtils.setField(a,"professorId",null);
        ReflectionTestUtils.setField(a,"tipoOrigem",TipoOrigemAtividade.SISTEMA); ReflectionTestUtils.setField(a,"publicada",true); return a;
    }
}
