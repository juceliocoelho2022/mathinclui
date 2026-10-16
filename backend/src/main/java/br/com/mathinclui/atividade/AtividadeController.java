package br.com.mathinclui.atividade;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atividades")
public class AtividadeController {
    private final AtividadeService atividadeService;

    public AtividadeController(AtividadeService atividadeService) {
        this.atividadeService = atividadeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtividadeResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateAtividadeRequest request) {
        return atividadeService.criar(professorId(jwt), request);
    }

    @GetMapping
    public List<AtividadeResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return atividadeService.listar(professorId(jwt));
    }

    @GetMapping("/{id}")
    public AtividadeResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return atividadeService.buscar(professorId(jwt), id);
    }

    @PutMapping("/{id}")
    public AtividadeResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                       @Valid @RequestBody UpdateAtividadeRequest request) {
        return atividadeService.atualizar(professorId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        atividadeService.excluir(professorId(jwt), id);
    }

    @PostMapping("/{id}/publicar")
    public AtividadeResponse publicar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return atividadeService.publicar(professorId(jwt), id);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
