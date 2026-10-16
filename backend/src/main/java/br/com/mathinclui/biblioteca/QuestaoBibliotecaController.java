package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.biblioteca.domain.TipoOrigemBiblioteca;
import br.com.mathinclui.questao.domain.TipoQuestao;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/biblioteca/questoes")
public class QuestaoBibliotecaController {
    private final QuestaoModeloService service;

    public QuestaoBibliotecaController(QuestaoModeloService service) { this.service = service; }

    @GetMapping
    public PageResponse<QuestaoModeloResponse> listar(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required=false) TipoOrigemBiblioteca origem, @RequestParam(required=false) Short ano,
            @RequestParam(required=false) String conteudo, @RequestParam(required=false) DificuldadeAtividade dificuldade,
            @RequestParam(required=false) TipoQuestao tipo, @RequestParam(required=false) String habilidade,
            @RequestParam(required=false) String tag, @RequestParam(required=false) String busca,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return service.listar(professorId(jwt), new BibliotecaQuestaoFiltro(origem, ano, conteudo, dificuldade,
                tipo, habilidade, tag, busca, page, size));
    }

    @GetMapping("/{id}")
    public QuestaoModeloResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.buscar(professorId(jwt), id);
    }

    @PostMapping("/{id}/copiar")
    public QuestaoModeloResponse copiar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.copiar(professorId(jwt), id);
    }

    private Long professorId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
