package br.com.mathinclui.perfilpedagogico;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PerfilPedagogicoController {
    private final PerfilPedagogicoService perfilService;

    public PerfilPedagogicoController(PerfilPedagogicoService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/api/v1/alunos/{alunoId}/perfil-pedagogico")
    public PerfilPedagogicoResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long alunoId) {
        return perfilService.buscar(professorId(jwt), alunoId);
    }

    @PutMapping("/api/v1/alunos/{alunoId}/perfil-pedagogico")
    public PerfilPedagogicoResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long alunoId,
                                              @Valid @RequestBody PerfilPedagogicoRequest request) {
        return perfilService.atualizar(professorId(jwt), alunoId, request);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
