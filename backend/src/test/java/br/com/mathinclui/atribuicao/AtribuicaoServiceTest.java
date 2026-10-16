package br.com.mathinclui.atribuicao;

import br.com.mathinclui.aluno.AlunoService;
import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.turma.TurmaService;
import br.com.mathinclui.turma.domain.Turma;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtribuicaoServiceTest {
    @Mock AtribuicaoRepository repository;
    @Mock AtividadeService atividadeService;
    @Mock TurmaService turmaService;
    @Mock AlunoService alunoService;
    private AtribuicaoService service;
    private final Instant now = Instant.parse("2026-10-16T00:00:00Z");

    @BeforeEach
    void setUp() {
        service = new AtribuicaoService(repository, atividadeService, turmaService, alunoService,
                Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void deve_exigir_exatamente_um_destino() {
        assertThatThrownBy(() -> service.criar(1L,
                new CreateAtribuicaoRequest(10L, null, null, now, null)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.criar(1L,
                new CreateAtribuicaoRequest(10L, 20L, 30L, now, null)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deve_rejeitar_data_limite_anterior_ao_inicio() {
        prepararAtividadeETurma();
        assertThatThrownBy(() -> service.criar(1L,
                new CreateAtribuicaoRequest(10L, 20L, null, now, now.minusSeconds(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limite");
    }

    @Test
    void deve_propagar_bloqueio_de_atividade_nao_publicada() {
        when(atividadeService.obterAtribuivel(1L, 10L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "publicada"));
        assertThatThrownBy(() -> service.criar(1L,
                new CreateAtribuicaoRequest(10L, 20L, null, now, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("publicada");
    }

    @Test
    void deve_validar_ownership_da_turma() {
        when(atividadeService.obterAtribuivel(1L, 10L)).thenReturn(atividade());
        when(turmaService.obterDoProfessor(1L, 20L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Turma não encontrada."));
        assertThatThrownBy(() -> service.criar(1L,
                new CreateAtribuicaoRequest(10L, 20L, null, now, null)))
                .isInstanceOf(ResponseStatusException.class);
    }

    private void prepararAtividadeETurma() {
        when(atividadeService.obterAtribuivel(1L, 10L)).thenReturn(atividade());
        when(turmaService.obterDoProfessor(1L, 20L)).thenReturn(new Turma(1L, "7A", (short) 7, "7A"));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Atividade atividade() {
        var atividade = new Atividade(1L, "A", null, (short) 7, "NUMEROS", DificuldadeAtividade.BASICA, 3);
        atividade.publicar();
        return atividade;
    }
}
