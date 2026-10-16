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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenciaInterfaceServiceTest {
    @Mock AlunoRepository alunoRepository;
    @Mock PreferenciaInterfaceAlunoRepository repository;
    private PreferenciaInterfaceService service;

    @BeforeEach
    void setUp() {
        service = new PreferenciaInterfaceService(alunoRepository, repository);
    }

    @Test
    void buscarSemRegistroRetornaDefaultsSemCriarLinha() {
        when(alunoRepository.findById(5L)).thenReturn(Optional.of(new Aluno(2L, "Ana", "ana", "hash")));
        when(repository.findById(5L)).thenReturn(Optional.empty());

        var response = service.buscar(5L);

        assertThat(response.tamanhoFonte()).isEqualTo(TamanhoFonte.PADRAO);
        assertThat(response.leituraVoz()).isFalse();
        assertThat(response.apoioVisual()).isFalse();
    }

    @Test
    void atualizarFazUpsertSomenteDasTresPreferenciasDeInterface() {
        when(alunoRepository.findById(5L)).thenReturn(Optional.of(new Aluno(2L, "Ana", "ana", "hash")));
        when(repository.findById(5L)).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.atualizar(5L,
                new PreferenciaInterfaceRequest(TamanhoFonte.EXTRA_GRANDE, true, true));

        var captor = ArgumentCaptor.forClass(PreferenciaInterfaceAluno.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAlunoId()).isEqualTo(5L);
        assertThat(captor.getValue().getTamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(captor.getValue().isLeituraVoz()).isTrue();
        assertThat(captor.getValue().isApoioVisual()).isTrue();
        assertThat(response.tamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(response.leituraVoz()).isTrue();
        assertThat(response.apoioVisual()).isTrue();
    }

    @Test
    void rejeitaAlunoInexistente() {
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
