package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAlunoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.AdaptacaoAtribuicao;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdaptacaoAtribuicaoServiceTest {
    @Mock AdaptacaoAtribuicaoRepository geralRepository;
    @Mock AdaptacaoAlunoAtribuicaoRepository individualRepository;
    @Mock AtribuicaoService atribuicaoService;
    @Mock AlunoService alunoService;

    private AdaptacaoAtribuicaoService service;

    @BeforeEach
    void setUp() {
        service = new AdaptacaoAtribuicaoService(geralRepository, individualRepository, atribuicaoService, alunoService);
    }

    @Test
    void deveSalvarAdaptacaoGeralSomenteAposValidarOwnership() {
        var atribuicao = turma(1L, 20L);
        when(atribuicaoService.obterDoProfessor(1L, 30L)).thenReturn(atribuicao);
        when(geralRepository.findByAtribuicaoId(30L)).thenReturn(Optional.empty());
        when(geralRepository.save(any(AdaptacaoAtribuicao.class))).thenAnswer(i -> i.getArgument(0));

        var response = service.salvarGeral(1L, 30L,
                new AdaptacaoRequest(TamanhoFonte.GRANDE, true, true, true, false,
                        (short) 2, true, NivelApoio.MODERADO));

        verify(atribuicaoService).obterDoProfessor(1L, 30L);
        assertThat(response.atribuicaoId()).isEqualTo(30L);
        assertThat(response.alunoId()).isNull();
        assertThat(response.tamanhoFonteMinima()).isEqualTo(TamanhoFonte.GRANDE);
        assertThat(response.leituraVozMinima()).isTrue();
    }

    @Test
    void deveRejeitarOverrideIndividualEmAtribuicaoDireta() {
        when(atribuicaoService.obterDoProfessor(1L, 30L)).thenReturn(direta(1L, 2L));

        assertThatThrownBy(() -> service.salvarIndividual(1L, 30L, 2L, AdaptacaoRequest.herdarTudo()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void deveRejeitarAlunoForaDaTurmaDaAtribuicao() {
        when(atribuicaoService.obterDoProfessor(1L, 30L)).thenReturn(turma(1L, 20L));
        when(alunoService.obterDoProfessor(1L, 2L)).thenReturn(new Aluno(99L, "Ana", "ana", "hash"));

        assertThatThrownBy(() -> service.salvarIndividual(1L, 30L, 2L, AdaptacaoRequest.herdarTudo()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deveSalvarOverrideIndividualParaAlunoDaTurma() {
        when(atribuicaoService.obterDoProfessor(1L, 30L)).thenReturn(turma(1L, 20L));
        when(alunoService.obterDoProfessor(1L, 2L)).thenReturn(new Aluno(20L, "Ana", "ana", "hash"));
        when(individualRepository.findByAtribuicaoIdAndAlunoId(30L, 2L)).thenReturn(Optional.empty());
        when(individualRepository.save(any(AdaptacaoAlunoAtribuicao.class))).thenAnswer(i -> i.getArgument(0));

        var response = service.salvarIndividual(1L, 30L, 2L,
                new AdaptacaoRequest(TamanhoFonte.EXTRA_GRANDE, null, true, null, true,
                        (short) 1, null, NivelApoio.AMPLIADO));

        assertThat(response.alunoId()).isEqualTo(2L);
        assertThat(response.tamanhoFonteMinima()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(response.apoioVisualMinimo()).isTrue();
        assertThat(response.nivelApoio()).isEqualTo(NivelApoio.AMPLIADO);
    }

    @Test
    void devePropagarNotFoundQuandoProfessorNaoPossuiAtribuicao() {
        when(atribuicaoService.obterDoProfessor(99L, 30L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada."));

        assertThatThrownBy(() -> service.salvarGeral(99L, 30L, AdaptacaoRequest.herdarTudo()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Atribuicao turma(Long professorId, Long turmaId) {
        return new Atribuicao(10L, professorId, turmaId, null, Instant.parse("2026-10-16T00:00:00Z"), null);
    }

    private Atribuicao direta(Long professorId, Long alunoId) {
        return new Atribuicao(10L, professorId, null, alunoId, Instant.parse("2026-10-16T00:00:00Z"), null);
    }
}
