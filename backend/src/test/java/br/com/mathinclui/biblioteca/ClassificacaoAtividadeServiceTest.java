package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.biblioteca.domain.HabilidadeBncc;
import br.com.mathinclui.biblioteca.domain.TagProfessor;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassificacaoAtividadeServiceTest {
    @Mock AtividadeService atividadeService;
    @Mock HabilidadeBnccRepository habilidadeRepository;
    @Mock TagProfessorRepository tagRepository;
    @Mock JdbcTemplate jdbcTemplate;
    private ClassificacaoAtividadeService service;

    @BeforeEach void setUp(){ service = new ClassificacaoAtividadeService(atividadeService, habilidadeRepository, tagRepository, jdbcTemplate); }

    @Test
    void salvarDeveBloquearQuandoAtividadeTemHistorico() {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(atividade());
        org.mockito.Mockito.doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "histórico"))
                .when(atividadeService).validarSemHistorico(10L);
        assertThatThrownBy(() -> service.salvar(1L, 10L, new ClassificacaoAtividadeRequest(List.of(), List.of())))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void salvarDeveValidarHabilidadesETagsERegravarRelacoes() {
        when(atividadeService.obterEditavelDoProfessor(1L, 10L)).thenReturn(atividade());
        when(habilidadeRepository.findAllById(List.of(2L))).thenReturn(List.of(new HabilidadeBncc("EF07MA01", (short)7, "Números", "Objeto", "Descrição")));
        when(tagRepository.findByIdAndProfessorId(3L, 1L)).thenReturn(Optional.of(new TagProfessor(1L, "Frações", "frações")));

        var response = service.salvar(1L, 10L, new ClassificacaoAtividadeRequest(List.of(2L), List.of(3L)));

        verify(atividadeService).validarSemHistorico(10L);
        verify(jdbcTemplate).update("DELETE FROM atividade_habilidade_bncc WHERE atividade_id = ?", 10L);
        verify(jdbcTemplate).update("DELETE FROM atividade_tag WHERE atividade_id = ?", 10L);
        assertThat(response.habilidadeIds()).containsExactly(2L);
        assertThat(response.tagIds()).containsExactly(3L);
    }

    private Atividade atividade(){ return new Atividade(1L,"A",null,(short)7,"NUMEROS", DificuldadeAtividade.BASICA,3); }
}
