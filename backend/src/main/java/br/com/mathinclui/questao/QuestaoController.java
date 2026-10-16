package br.com.mathinclui.questao;

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
@RequestMapping("/api/v1/atividades/{atividadeId}/questoes")
public class QuestaoController {
    private final QuestaoService questaoService;

    public QuestaoController(QuestaoService questaoService) {
        this.questaoService = questaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoProfessorResponse criar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long atividadeId,
                                          @Valid @RequestBody CreateQuestaoRequest request) {
        return questaoService.criar(professorId(jwt), atividadeId, request);
    }

    @GetMapping
    public List<QuestaoProfessorResponse> listar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long atividadeId) {
        return questaoService.listar(professorId(jwt), atividadeId);
    }

    @PutMapping("/{questaoId}")
    public QuestaoProfessorResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long atividadeId,
                                              @PathVariable Long questaoId,
                                              @Valid @RequestBody UpdateQuestaoRequest request) {
        return questaoService.atualizar(professorId(jwt), atividadeId, questaoId, request);
    }

    @DeleteMapping("/{questaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long atividadeId, @PathVariable Long questaoId) {
        questaoService.excluir(professorId(jwt), atividadeId, questaoId);
    }

    private Long professorId(Jwt jwt) {
        var subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        }
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
