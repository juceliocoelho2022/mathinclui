package br.com.mathinclui.tentativa;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aluno")
public class AlunoAtividadeController {
    private final AlunoAtividadeService atividadeService;
    private final TentativaService tentativaService;

    public AlunoAtividadeController(AlunoAtividadeService atividadeService, TentativaService tentativaService) {
        this.atividadeService = atividadeService;
        this.tentativaService = tentativaService;
    }

    @GetMapping("/atividades")
    public List<AlunoAtividadeResumoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return atividadeService.listar(alunoId(jwt));
    }

    @GetMapping("/atividades/{atribuicaoId}")
    public AlunoAtividadeDetalheResponse detalhar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long atribuicaoId) {
        return atividadeService.detalhar(alunoId(jwt), atribuicaoId);
    }

    @PostMapping("/atribuicoes/{id}/iniciar")
    public TentativaResponse iniciar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return tentativaService.iniciar(alunoId(jwt), id);
    }

    @PostMapping("/tentativas/{tentativaId}/questoes/{questaoId}/responder")
    public RespostaQuestaoResponse responder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long tentativaId,
                                             @PathVariable Long questaoId,
                                             @Valid @RequestBody RespostaQuestaoRequest request) {
        return tentativaService.responder(alunoId(jwt), tentativaId, questaoId, request);
    }

    @GetMapping("/tentativas/{id}/resultado")
    public TentativaResultadoResponse resultado(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return tentativaService.resultado(alunoId(jwt), id);
    }

    private Long alunoId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("ALUNO:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do aluno.");
        }
        return Long.parseLong(subject.substring("ALUNO:".length()));
    }
}
