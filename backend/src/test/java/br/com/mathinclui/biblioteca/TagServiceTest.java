package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.TagProfessor;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {
    @Mock TagProfessorRepository repository;
    private TagService service;

    @BeforeEach void setUp(){ service = new TagService(repository); }

    @Test
    void deveNormalizarNomeComNfcEspacosELowercase() {
        when(repository.existsByProfessorIdAndNomeNormalizado(1L, "frações básicas")).thenReturn(false);
        when(repository.save(any(TagProfessor.class))).thenAnswer(i -> i.getArgument(0));
        var tag = service.criar(1L, "  FRAÇÕES   Básicas  ");
        assertThat(tag.nome()).isEqualTo("FRAÇÕES Básicas");
        assertThat(tag.nomeNormalizado()).isEqualTo("frações básicas");
    }

    @Test
    void deveBloquearDuplicidadeNormalizadaNoMesmoProfessor() {
        when(repository.existsByProfessorIdAndNomeNormalizado(1L, "frações")).thenReturn(true);
        assertThatThrownBy(() -> service.criar(1L, "Frações"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void listarDeveUsarSomenteProfessorAutenticado() {
        when(repository.findAllByProfessorIdOrderByNomeAsc(1L)).thenReturn(List.of());
        assertThat(service.listar(1L)).isEmpty();
    }

    @Test
    void renomearDeveExigirOwnership() {
        when(repository.findByIdAndProfessorId(9L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.renomear(1L, 9L, "Nova"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
