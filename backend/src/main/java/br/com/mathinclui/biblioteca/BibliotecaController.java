package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeResponse;
import br.com.mathinclui.atividade.domain.DificuldadeAtividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BibliotecaController {
    private final BibliotecaAtividadeService biblioteca;
    private final BnccService bncc;
    private final TagService tags;
    private final ClassificacaoAtividadeService classificacao;

    public BibliotecaController(BibliotecaAtividadeService biblioteca, BnccService bncc, TagService tags,
                                ClassificacaoAtividadeService classificacao) {
        this.biblioteca = biblioteca; this.bncc = bncc; this.tags = tags; this.classificacao = classificacao;
    }

    @GetMapping("/biblioteca/atividades")
    public PageResponse<BibliotecaAtividadeResumoResponse> listar(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required=false) TipoOrigemAtividade origem, @RequestParam(required=false) Short ano,
            @RequestParam(required=false) String unidadeTematica, @RequestParam(required=false) String objetoConhecimento,
            @RequestParam(required=false) String habilidade, @RequestParam(required=false) String conteudo,
            @RequestParam(required=false) DificuldadeAtividade dificuldade, @RequestParam(required=false) String tag,
            @RequestParam(required=false) String busca, @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return biblioteca.listar(professorId(jwt), new BibliotecaAtividadeFiltro(origem, ano, unidadeTematica,
                objetoConhecimento, habilidade, conteudo, dificuldade, tag, busca, page, size));
    }

    @GetMapping("/biblioteca/atividades/{id}")
    public BibliotecaAtividadeDetalheResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return biblioteca.buscar(professorId(jwt), id);
    }

    @PostMapping("/biblioteca/atividades/{id}/copiar")
    public AtividadeResponse copiar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return biblioteca.copiar(professorId(jwt), id);
    }

    @GetMapping("/biblioteca/bncc")
    public List<HabilidadeBnccResponse> bncc(@AuthenticationPrincipal Jwt jwt, @RequestParam(required=false) Short ano) {
        professorId(jwt); return bncc.listar(ano);
    }

    @GetMapping("/biblioteca/tags")
    public List<TagResponse> tags(@AuthenticationPrincipal Jwt jwt) { return tags.listar(professorId(jwt)); }

    @PostMapping("/biblioteca/tags")
    public TagResponse criarTag(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TagRequest request) {
        return tags.criar(professorId(jwt), request.nome());
    }

    @DeleteMapping("/biblioteca/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirTag(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) { tags.excluir(professorId(jwt), id); }

    @GetMapping("/atividades/{id}/classificacao")
    public ClassificacaoAtividadeResponse classificacao(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return classificacao.buscar(professorId(jwt), id);
    }

    @PutMapping("/atividades/{id}/classificacao")
    public ClassificacaoAtividadeResponse atualizarClassificacao(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                                  @Valid @RequestBody ClassificacaoAtividadeRequest request) {
        return classificacao.atualizar(professorId(jwt), id, request);
    }

    private Long professorId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || !subject.startsWith("PROFESSOR:"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do professor.");
        return Long.parseLong(subject.substring("PROFESSOR:".length()));
    }
}
