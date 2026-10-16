package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BibliotecaAtividadeServiceTest {
    @Mock AtividadeRepository atividadeRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock AlternativaRepository alternativaRepository;
    @Mock NamedParameterJdbcTemplate jdbc;
    private BibliotecaAtividadeService service;

    @BeforeEach
    void setUp() {
        service = new BibliotecaAtividadeService(atividadeRepository, questaoRepository, alternativaRepository, jdbc,
                Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void buscaNoBancoUsaVisibilidadeSistemaOuProfessorAutenticado() {
        when(jdbc.queryForList(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), eq(Long.class)))
                .thenReturn(List.of());
        when(jdbc.queryForObject(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);

        service.listar(7L, BibliotecaAtividadeFiltro.padrao());

        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForList(sql.capture(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), eq(Long.class));
        assertThat(sql.getValue()).contains("a.professor_id = :professorId");
        assertThat(sql.getValue()).contains("a.tipo_origem = 'SISTEMA'");
        assertThat(sql.getValue()).contains("a.publicada = TRUE");
        assertThat(sql.getValue()).contains("ORDER BY a.titulo, a.id");
    }

    @Test
    void naoPermiteCopiarAtividadePessoalDeOutroProfessor() {
        Atividade fonte = new Atividade(99L, "Privada", null, (short) 7, "ALGEBRA", DificuldadeAtividade.BASICA, 3);
        when(atividadeRepository.findById(10L)).thenReturn(Optional.of(fonte));

        assertThatThrownBy(() -> service.copiar(7L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void copiaProfundaSistemaComoRascunhoDoProfessorELevaBncc() throws Exception {
        Atividade fonte = mock(Atividade.class);
        when(fonte.isSistemaPublicada()).thenReturn(true);
        when(fonte.getTitulo()).thenReturn("Frações oficiais");
        when(fonte.getDescricao()).thenReturn("Descrição");
        when(fonte.getAnoEscolar()).thenReturn((short) 6);
        when(fonte.getConteudo()).thenReturn("FRACOES");
        when(fonte.getDificuldade()).thenReturn(DificuldadeAtividade.INTERMEDIARIA);
        when(fonte.getLimiteTentativas()).thenReturn(3);
        when(atividadeRepository.findById(10L)).thenReturn(Optional.of(fonte));
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> {
            Atividade a = inv.getArgument(0);
            setId(a, 100L);
            return a;
        });

        Questao questao = new Questao(10L, "1/2 de 10?", TipoQuestao.NUMERICA, "5", "Divida por 2",
                "Pense em metade", null, null, null, 1, 2);
        setId(questao, 20L);
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(10L)).thenReturn(List.of(questao));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(inv -> {
            Questao q = inv.getArgument(0);
            setId(q, 200L);
            return q;
        });
        when(alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(20L)).thenReturn(List.of());
        when(jdbc.queryForList(contains("atividade_habilidade_bncc"), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), eq(Long.class)))
                .thenReturn(List.of(55L));

        var result = service.copiar(7L, 10L);

        assertThat(result.professorId()).isEqualTo(7L);
        assertThat(result.publicada()).isFalse();
        assertThat(result.tipoOrigem()).isEqualTo(TipoOrigemAtividade.PROFESSOR);
        var atividadeCaptor = ArgumentCaptor.forClass(Atividade.class);
        verify(atividadeRepository).save(atividadeCaptor.capture());
        assertThat(atividadeCaptor.getValue().getOrigemAtividadeId()).isEqualTo(10L);
        assertThat(atividadeCaptor.getValue().getCopiadoEm()).isEqualTo(Instant.parse("2026-10-16T12:00:00Z"));
        var questaoCaptor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(questaoCaptor.capture());
        assertThat(questaoCaptor.getValue().getAtividadeId()).isEqualTo(100L);
        assertThat(questaoCaptor.getValue().getEnunciado()).isEqualTo("1/2 de 10?");
        verify(jdbc).update(contains("atividade_habilidade_bncc"), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class));
    }

    private static void setId(Object entity, long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
