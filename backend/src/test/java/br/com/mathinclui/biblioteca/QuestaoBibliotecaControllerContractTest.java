package br.com.mathinclui.biblioteca;

import br.com.mathinclui.questao.QuestaoController;
import br.com.mathinclui.questao.QuestaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestaoBibliotecaControllerContractTest {
    @Mock QuestaoModeloService modeloService;
    @Mock QuestaoService questaoService;
    private QuestaoBibliotecaController bibliotecaController;
    private QuestaoController questaoController;

    @BeforeEach
    void setUp() {
        bibliotecaController = new QuestaoBibliotecaController(modeloService);
        questaoController = new QuestaoController(questaoService, modeloService);
    }

    @Test
    void controllerDaBibliotecaDeveExporPrefixoDeQuestoes() {
        var mapping = QuestaoBibliotecaController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping.value()).containsExactly("/api/v1/biblioteca/questoes");
        assertThat(QuestaoBibliotecaController.class.getDeclaredMethods())
                .extracting(Method::getName)
                .contains("listar", "detalhar", "copiar");
    }

    @Test
    void copiarModeloDeveUsarProfessorDoJwt() {
        when(modeloService.copiarModelo(7L, 40L)).thenReturn(null);

        bibliotecaController.copiar(jwt("PROFESSOR:7"), 40L);

        verify(modeloService).copiarModelo(7L, 40L);
    }

    @Test
    void alunoNaoPodeInvocarBancoDeQuestoesDoProfessor() {
        assertThatThrownBy(() -> bibliotecaController.listar(jwt("ALUNO:9")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("professor");
    }

    @Test
    void questaoControllerDeveExporSalvarNoBancoESalvarModeloComoAliases() throws Exception {
        var method = QuestaoController.class.getDeclaredMethod("salvarNoBanco", Jwt.class, Long.class, Long.class,
                SalvarQuestaoNoBancoRequest.class);
        var mapping = method.getAnnotation(PostMapping.class);
        assertThat(mapping.value()).containsExactlyInAnyOrder("/{questaoId}/salvar-no-banco", "/{questaoId}/salvar-modelo");
    }

    @Test
    void questaoControllerDeveExporImportacaoEUsarProfessorDoJwt() {
        var request = new ImportarQuestoesRequest(List.of(80L, 81L));
        when(modeloService.importar(7L, 10L, request)).thenReturn(List.of());

        questaoController.importar(jwt("PROFESSOR:7"), 10L, request);

        verify(modeloService).importar(7L, 10L, request);
    }

    private Jwt jwt(String subject) {
        return new Jwt("token", Instant.parse("2026-10-17T00:00:00Z"), Instant.parse("2026-10-17T01:00:00Z"),
                java.util.Map.of("alg", "none"), java.util.Map.of("sub", subject));
    }
}
