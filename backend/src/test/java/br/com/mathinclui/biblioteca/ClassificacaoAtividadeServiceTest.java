package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.biblioteca.domain.TagProfessor;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificacaoAtividadeServiceTest {
    @Mock AtividadeService atividadeService;
    @Mock HabilidadeBnccRepository habilidadeRepository;
    @Mock TagProfessorRepository tagRepository;
    @Mock NamedParameterJdbcTemplate jdbc;
    private ClassificacaoAtividadeService service;

    @BeforeEach void setUp() { service = new ClassificacaoAtividadeService(atividadeService, habilidadeRepository, tagRepository, jdbc); }

    @Test
    void classificacaoComHistoricoEhBloqueadaAntesDeAlterarRelacoes() {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(
                new Atividade(1L, "A", null, (short) 7, "ALGEBRA", DificuldadeAtividade.BASICA, 3));
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "histórico"))
                .when(atividadeService).validarSemHistorico(10L);

        assertThatThrownBy(() -> service.atualizar(1L, 10L, new ClassificacaoAtividadeRequest(List.of(), List.of())))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
        verifyNoInteractions(jdbc);
    }

    @Test
    void rejeitaTagQueNaoPertenceAoProfessor() {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(
                new Atividade(1L, "A", null, (short) 7, "ALGEBRA", DificuldadeAtividade.BASICA, 3));
        when(tagRepository.findByIdAndProfessorId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(1L, 10L, new ClassificacaoAtividadeRequest(List.of(), List.of(5L))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
        verify(jdbc, never()).update(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class));
    }
}
