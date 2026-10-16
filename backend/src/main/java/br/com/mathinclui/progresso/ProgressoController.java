package br.com.mathinclui.progresso;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProgressoController {
    private final ProgressoService service;

    public ProgressoController(ProgressoService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/atribuicoes/{id}/resultados")
    public AtividadeResultadosResponse resultados(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.resultados(professorId(jwt), id);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
