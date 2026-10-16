package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.TagProfessor;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {
    @Mock TagProfessorRepository repository;
    private TagService service;

    @BeforeEach void setUp() { service = new TagService(repository); }

    @Test
    void normalizaUnicodeEspacosECaseParaUnicidade() {
        when(repository.existsByProfessorIdAndNomeNormalizado(1L, "revisão final")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.criar(1L, "  Revisão   FINAL  ");

        var captor = ArgumentCaptor.forClass(TagProfessor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Revisão FINAL");
        assertThat(captor.getValue().getNomeNormalizado()).isEqualTo("revisão final");
    }

    @Test
    void duplicidadeEhIsoladaPorProfessor() {
        when(repository.existsByProfessorIdAndNomeNormalizado(1L, "revisão")).thenReturn(true);
        assertThatThrownBy(() -> service.criar(1L, "Revisão"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
        verify(repository, never()).save(any());
    }
}
