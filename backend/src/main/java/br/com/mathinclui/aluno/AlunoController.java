package br.com.mathinclui.aluno;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class AlunoController {
    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping("/api/v1/turmas/{turmaId}/alunos")
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse criar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long turmaId,
                               @Valid @RequestBody CreateAlunoRequest request) {
        return alunoService.criar(professorId(jwt), turmaId, request);
    }

    @GetMapping("/api/v1/turmas/{turmaId}/alunos")
    public List<AlunoResponse> listar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long turmaId) {
        return alunoService.listar(professorId(jwt), turmaId);
    }

    @GetMapping("/api/v1/alunos/{id}")
    public AlunoResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return alunoService.buscar(professorId(jwt), id);
    }

    @PutMapping("/api/v1/alunos/{id}")
    public AlunoResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                   @Valid @RequestBody UpdateAlunoRequest request) {
        return alunoService.atualizar(professorId(jwt), id, request);
    }

    @PatchMapping("/api/v1/alunos/{id}/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirPin(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                             @Valid @RequestBody UpdatePinRequest request) {
        alunoService.redefinirPin(professorId(jwt), id, request);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
