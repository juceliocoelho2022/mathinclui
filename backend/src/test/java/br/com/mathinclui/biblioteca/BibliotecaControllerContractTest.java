package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.HabilidadeBncc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BibliotecaControllerContractTest {
    @Mock BibliotecaAtividadeService atividadeService;
    @Mock BnccService bnccService;
    @Mock TagService tagService;
    @Mock ClassificacaoAtividadeService classificacaoService;

    private BibliotecaController bibliotecaController;
    private AtividadeClassificacaoController classificacaoController;

    @BeforeEach
    void setUp() {
        bibliotecaController = new BibliotecaController(atividadeService, bnccService, tagService);
        classificacaoController = new AtividadeClassificacaoController(classificacaoService);
    }

    @Test
    void bibliotecaDeveUsarProfessorAutenticadoENaoProfessorDoPayload() {
        var jwt = jwt("PROFESSOR:7");
        when(atividadeService.listar(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0));

        bibliotecaController.listarAtividades(jwt, null, null, null, null, null, null, null, null, null, 0, 20);

        verify(atividadeService).listar(org.mockito.ArgumentMatchers.eq(7L), argThat(f -> f.page() == 0 && f.size() == 20));
    }

    @Test
    void bibliotecaDeveExporRotasOficialBnccETagsNoPrefixoCorreto() {
        var mapping = BibliotecaController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping.value()).containsExactly("/api/v1/biblioteca");
        assertThat(BibliotecaController.class.getDeclaredMethods())
                .extracting(java.lang.reflect.Method::getName)
                .contains("listarAtividades", "detalharAtividade", "copiarAtividade", "listarHabilidades", "listarTags", "criarTag", "renomearTag", "excluirTag");
    }

    @Test
    void bnccDeveSerFiltradaPorAnoEMapeadaSemEntidadeJPA() {
        var habilidade = new HabilidadeBncc("EF07MA01", (short) 7, "Números", "Objeto", "Descrição");
        when(bnccService.listar((short) 7)).thenReturn(List.of(HabilidadeBnccResponse.from(habilidade)));

        var response = bibliotecaController.listarHabilidades(jwt("PROFESSOR:7"), (short) 7);

        assertThat(response).singleElement().extracting(HabilidadeBnccResponse::codigo).isEqualTo("EF07MA01");
    }

    @Test
    void classificacaoDeveUsarProfessorDoJwt() {
        var request = new ClassificacaoAtividadeRequest(List.of(2L), List.of(3L));
        when(classificacaoService.salvar(7L, 10L, request))
                .thenReturn(new ClassificacaoAtividadeResponse(List.of(2L), List.of(3L)));

        classificacaoController.salvar(jwt("PROFESSOR:7"), 10L, request);

        verify(classificacaoService).salvar(7L, 10L, request);
    }

    @Test
    void alunoNaoPodeInvocarControllerDaBiblioteca() {
        assertThatThrownBy(() -> bibliotecaController.listarTags(jwt("ALUNO:9")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("professor");
    }

    private Jwt jwt(String subject) {
        return new Jwt("token", Instant.parse("2026-10-17T00:00:00Z"), Instant.parse("2026-10-17T01:00:00Z"),
                java.util.Map.of("alg", "none"), java.util.Map.of("sub", subject));
    }
}
