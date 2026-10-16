package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.repository.AdaptacaoAlunoAtribuicaoRepository;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAtribuicaoRepository;
import br.com.mathinclui.aluno.AlunoService;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.atribuicao.AtribuicaoService;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdaptacaoAtribuicaoServiceTest {
    @Mock AtribuicaoService atribuicaoService;
    @Mock AlunoService alunoService;
    @Mock AdaptacaoAtribuicaoRepository geralRepository;
    @Mock AdaptacaoAlunoAtribuicaoRepository individualRepository;
    private AdaptacaoAtribuicaoService service;

    @BeforeEach void setUp() { service = new AdaptacaoAtribuicaoService(atribuicaoService, alunoService, geralRepository, individualRepository); }

    @Test
    void professorNaoAlteraAtribuicaoDeOutroProfessor() {
        when(atribuicaoService.obterDoProfessor(2L, 10L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada."));
        assertThatThrownBy(() -> service.salvarGeral(2L, 10L, request()))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
        verifyNoInteractions(geralRepository);
    }

    @Test
    void atribuicaoDiretaRejeitaOverrideIndividual() {
        when(atribuicaoService.obterDoProfessor(1L, 10L)).thenReturn(
                new Atribuicao(5L, 1L, null, 20L, Instant.parse("2026-10-16T00:00:00Z"), null));
        assertThatThrownBy(() -> service.salvarIndividual(1L, 10L, 20L, request()))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
        verifyNoInteractions(alunoService, individualRepository);
    }

    @Test
    void alunoForaDaTurmaNaoRecebeOverrideIndividual() {
        when(atribuicaoService.obterDoProfessor(1L, 10L)).thenReturn(
                new Atribuicao(5L, 1L, 30L, null, Instant.parse("2026-10-16T00:00:00Z"), null));
        when(alunoService.obterDoProfessor(1L, 20L)).thenReturn(new Aluno(99L, "Ana", "ana", "hash"));
        assertThatThrownBy(() -> service.salvarIndividual(1L, 10L, 20L, request()))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
        verifyNoInteractions(individualRepository);
    }

    @Test
    void salvaOverrideIndividualParaAlunoDaTurma() {
        when(atribuicaoService.obterDoProfessor(1L, 10L)).thenReturn(
                new Atribuicao(5L, 1L, 30L, null, Instant.parse("2026-10-16T00:00:00Z"), null));
        when(alunoService.obterDoProfessor(1L, 20L)).thenReturn(new Aluno(30L, "Ana", "ana", "hash"));
        when(individualRepository.findByAtribuicaoIdAndAlunoId(10L, 20L)).thenReturn(Optional.empty());
        when(individualRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = service.salvarIndividual(1L, 10L, 20L, request());

        assertThat(response.tamanhoFonteMinima()).isEqualTo(TamanhoFonte.GRANDE);
        assertThat(response.apoioVisualMinimo()).isTrue();
        assertThat(response.nivelApoio()).isEqualTo(NivelApoio.MODERADO);
        verify(individualRepository).save(any());
    }

    private AdaptacaoRequest request() {
        return new AdaptacaoRequest(TamanhoFonte.GRANDE, null, true, true, null, (short) 1, null, NivelApoio.MODERADO);
    }
}
