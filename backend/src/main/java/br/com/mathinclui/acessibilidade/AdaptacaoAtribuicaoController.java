package br.com.mathinclui.acessibilidade;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/atribuicoes")
public class AdaptacaoAtribuicaoController {
    private final AdaptacaoAtribuicaoService service;

    public AdaptacaoAtribuicaoController(AdaptacaoAtribuicaoService service) {
        this.service = service;
    }

    @GetMapping("/{id}/adaptacao")
    public AdaptacaoResponse buscarGeral(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.buscarGeral(professorId(jwt), id);
    }

    @PutMapping("/{id}/adaptacao")
    public AdaptacaoResponse salvarGeral(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                         @Valid @RequestBody AdaptacaoRequest request) {
        return service.salvarGeral(professorId(jwt), id, request);
    }

    @GetMapping("/{id}/alunos/{alunoId}/adaptacao")
    public AdaptacaoResponse buscarIndividual(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                              @PathVariable Long alunoId) {
        return service.buscarIndividual(professorId(jwt), id, alunoId);
    }

    @PutMapping("/{id}/alunos/{alunoId}/adaptacao")
    public AdaptacaoResponse salvarIndividual(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                              @PathVariable Long alunoId,
                                              @Valid @RequestBody AdaptacaoRequest request) {
        return service.salvarIndividual(professorId(jwt), id, alunoId, request);
    }

    @DeleteMapping("/{id}/alunos/{alunoId}/adaptacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirIndividual(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                  @PathVariable Long alunoId) {
        service.excluirIndividual(professorId(jwt), id, alunoId);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
