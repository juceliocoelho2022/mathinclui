package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/biblioteca")
public class BibliotecaController {
    private final BibliotecaAtividadeService atividadeService;
    private final BnccService bnccService;
    private final TagService tagService;

    public BibliotecaController(BibliotecaAtividadeService atividadeService, BnccService bnccService, TagService tagService) {
        this.atividadeService = atividadeService;
        this.bnccService = bnccService;
        this.tagService = tagService;
    }

    @GetMapping("/atividades")
    public PageResponse<BibliotecaAtividadeResumoResponse> listarAtividades(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) TipoOrigemAtividade origem,
            @RequestParam(required = false) Short ano,
            @RequestParam(required = false) String unidadeTematica,
            @RequestParam(required = false) String objetoConhecimento,
            @RequestParam(required = false) String habilidade,
            @RequestParam(required = false) String conteudo,
            @RequestParam(required = false) DificuldadeAtividade dificuldade,
            @RequestParam(required = false) Long tag,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return atividadeService.listar(professorId(jwt), new BibliotecaAtividadeFiltro(
                origem, ano, unidadeTematica, objetoConhecimento, habilidade, conteudo, dificuldade, tag, busca, page, size));
    }

    @GetMapping("/atividades/{id}")
    public BibliotecaAtividadeDetalheResponse detalharAtividade(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return atividadeService.detalhar(professorId(jwt), id);
    }

    @PostMapping("/atividades/{id}/copiar")
    @ResponseStatus(HttpStatus.CREATED)
    public BibliotecaAtividadeDetalheResponse copiarAtividade(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return atividadeService.copiar(professorId(jwt), id);
    }

    @GetMapping({"/bncc", "/bncc/habilidades"})
    public List<HabilidadeBnccResponse> listarHabilidades(@AuthenticationPrincipal Jwt jwt, @RequestParam short ano) {
        professorId(jwt);
        return bnccService.listar(ano);
    }

    @GetMapping("/tags")
    public List<TagResponse> listarTags(@AuthenticationPrincipal Jwt jwt) {
        return tagService.listar(professorId(jwt));
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse criarTag(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TagNomeRequest request) {
        return tagService.criar(professorId(jwt), request.nome());
    }

    @PutMapping("/tags/{id}")
    public TagResponse renomearTag(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                   @Valid @RequestBody TagNomeRequest request) {
        return tagService.renomear(professorId(jwt), id, request.nome());
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirTag(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        tagService.excluir(professorId(jwt), id);
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
