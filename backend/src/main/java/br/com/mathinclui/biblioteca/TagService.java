package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.TagProfessor;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class TagService {
    private final TagProfessorRepository repository;

    public TagService(TagProfessorRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<TagResponse> listar(Long professorId) {
        return repository.findAllByProfessorIdOrderByNomeAsc(professorId).stream().map(TagResponse::from).toList();
    }

    @Transactional
    public TagResponse criar(Long professorId, String nome) {
        String exibicao = normalizarExibicao(nome);
        String normalizado = normalizarNome(exibicao);
        if (normalizado.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da tag é obrigatório.");
        if (repository.existsByProfessorIdAndNomeNormalizado(professorId, normalizado))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma tag com esse nome.");
        return TagResponse.from(repository.save(new TagProfessor(professorId, exibicao, normalizado)));
    }

    @Transactional
    public void excluir(Long professorId, Long tagId) {
        var tag = repository.findByIdAndProfessorId(tagId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag não encontrada."));
        repository.delete(tag);
    }

    static String normalizarNome(String nome) {
        return Normalizer.normalize(normalizarExibicao(nome), Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
    }

    static String normalizarExibicao(String nome) {
        if (nome == null) return "";
        return Normalizer.normalize(nome.trim().replaceAll("\\s+", " "), Normalizer.Form.NFC);
    }
}
