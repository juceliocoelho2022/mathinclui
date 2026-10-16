package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.PreferenciaInterfaceAluno;
import br.com.mathinclui.acessibilidade.repository.PreferenciaInterfaceAlunoRepository;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenciaInterfaceServiceTest {
    @Mock PreferenciaInterfaceAlunoRepository repository;
    @Mock AlunoRepository alunoRepository;
    private PreferenciaInterfaceService service;

    @BeforeEach
    void setUp() {
        service = new PreferenciaInterfaceService(repository, alunoRepository);
    }

    @Test
    void deveRetornarDefaultsSemPersistirQuandoAlunoAindaNaoTemPreferencia() {
        when(alunoRepository.findById(2L)).thenReturn(Optional.of(new Aluno(20L, "Ana", "ana", "hash")));
        when(repository.findByAlunoId(2L)).thenReturn(Optional.empty());

        var resposta = service.buscar(2L);

        assertThat(resposta.alunoId()).isEqualTo(2L);
        assertThat(resposta.tamanhoFonte()).isEqualTo(TamanhoFonte.PADRAO);
        assertThat(resposta.leituraVoz()).isFalse();
        assertThat(resposta.apoioVisual()).isFalse();
    }

    @Test
    void deveCriarPreferenciaDoProprioAluno() {
        when(alunoRepository.findById(2L)).thenReturn(Optional.of(new Aluno(20L, "Ana", "ana", "hash")));
        when(repository.findByAlunoId(2L)).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(PreferenciaInterfaceAluno.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = service.atualizar(2L,
                new PreferenciaInterfaceRequest(TamanhoFonte.EXTRA_GRANDE, true, true));

        var captor = ArgumentCaptor.forClass(PreferenciaInterfaceAluno.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAlunoId()).isEqualTo(2L);
        assertThat(resposta.tamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(resposta.leituraVoz()).isTrue();
        assertThat(resposta.apoioVisual()).isTrue();
    }

    @Test
    void deveAtualizarPreferenciaExistenteSemTrocarIdentidade() {
        var existente = new PreferenciaInterfaceAluno(2L);
        existente.atualizar(TamanhoFonte.GRANDE, false, false);
        when(alunoRepository.findById(2L)).thenReturn(Optional.of(new Aluno(20L, "Ana", "ana", "hash")));
        when(repository.findByAlunoId(2L)).thenReturn(Optional.of(existente));

        var resposta = service.atualizar(2L,
                new PreferenciaInterfaceRequest(TamanhoFonte.EXTRA_GRANDE, true, false));

        assertThat(resposta.alunoId()).isEqualTo(2L);
        assertThat(resposta.tamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(resposta.leituraVoz()).isTrue();
        assertThat(resposta.apoioVisual()).isFalse();
    }

    @Test
    void deveRetornar404ParaAlunoInativoOuInexistente() {
        when(alunoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(404L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
