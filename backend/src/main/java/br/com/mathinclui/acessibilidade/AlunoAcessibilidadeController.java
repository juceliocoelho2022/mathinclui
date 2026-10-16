package br.com.mathinclui.acessibilidade;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/aluno")
public class AlunoAcessibilidadeController {
    private final AcessibilidadeEfetivaService acessibilidadeService;
    private final PreferenciaInterfaceService preferenciaService;

    public AlunoAcessibilidadeController(AcessibilidadeEfetivaService acessibilidadeService,
                                         PreferenciaInterfaceService preferenciaService) {
        this.acessibilidadeService = acessibilidadeService;
        this.preferenciaService = preferenciaService;
    }

    @GetMapping("/atribuicoes/{id}/acessibilidade")
    public ConfiguracaoAcessibilidadeResponse acessibilidade(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ConfiguracaoAcessibilidadeResponse.from(acessibilidadeService.calcular(alunoId(jwt), id));
    }

    @GetMapping("/preferencias-interface")
    public PreferenciaInterfaceResponse preferencias(@AuthenticationPrincipal Jwt jwt) {
        return preferenciaService.buscar(alunoId(jwt));
    }

    @PutMapping("/preferencias-interface")
    public PreferenciaInterfaceResponse atualizar(@AuthenticationPrincipal Jwt jwt,
                                                   @Valid @RequestBody PreferenciaInterfaceRequest request) {
        return preferenciaService.atualizar(alunoId(jwt), request);
    }

    private Long alunoId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("ALUNO:"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do aluno.");
        return Long.parseLong(subject.substring("ALUNO:".length()));
    }
}
