package br.com.mathinclui.turma;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/turmas")
public class TurmaController {
    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurmaResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateTurmaRequest request) {
        return turmaService.criar(professorId(jwt), request);
    }

    @GetMapping
    public List<TurmaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return turmaService.listar(professorId(jwt));
    }

    @GetMapping("/{id}")
    public TurmaResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return turmaService.buscar(professorId(jwt), id);
    }

    @PutMapping("/{id}")
    public TurmaResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                   @Valid @RequestBody UpdateTurmaRequest request) {
        return turmaService.atualizar(professorId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        turmaService.desativar(professorId(jwt), id);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
