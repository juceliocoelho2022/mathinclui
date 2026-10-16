package br.com.mathinclui.atribuicao;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atribuicoes")
public class AtribuicaoController {
    private final AtribuicaoService service;

    public AtribuicaoController(AtribuicaoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtribuicaoResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateAtribuicaoRequest request) {
        return service.criar(professorId(jwt), request);
    }

    @GetMapping
    public List<AtribuicaoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(professorId(jwt));
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
