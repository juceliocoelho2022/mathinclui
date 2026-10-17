package br.com.mathinclui.biblioteca;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/atividades/{atividadeId}/classificacao")
public class AtividadeClassificacaoController {
    private final ClassificacaoAtividadeService service;

    public AtividadeClassificacaoController(ClassificacaoAtividadeService service) {
        this.service = service;
    }

    @GetMapping
    public ClassificacaoAtividadeResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long atividadeId) {
        return service.buscar(professorId(jwt), atividadeId);
    }

    @PutMapping
    public ClassificacaoAtividadeResponse salvar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long atividadeId,
                                                 @RequestBody ClassificacaoAtividadeRequest request) {
        return service.salvar(professorId(jwt), atividadeId, request);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt == null ? null : jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        try {
            return Long.parseLong(subject.substring("PROFESSOR:".length()));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
    }
}
