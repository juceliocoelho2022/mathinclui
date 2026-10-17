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

    public TagService(TagProfessorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listar(Long professorId) {
        return repository.findAllByProfessorIdOrderByNomeAsc(professorId).stream().map(TagResponse::from).toList();
    }

    @Transactional
    public TagResponse criar(Long professorId, String nome) {
        var display = normalizarExibicao(nome);
        var normalizado = normalizarChave(display);
        if (repository.existsByProfessorIdAndNomeNormalizado(professorId, normalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma tag com esse nome.");
        }
        return TagResponse.from(repository.save(new TagProfessor(professorId, display, normalizado)));
    }

    @Transactional
    public TagResponse renomear(Long professorId, Long tagId, String nome) {
        var tag = repository.findByIdAndProfessorId(tagId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag não encontrada."));
        var display = normalizarExibicao(nome);
        var normalizado = normalizarChave(display);
        if (repository.existsByProfessorIdAndNomeNormalizadoAndIdNot(professorId, normalizado, tagId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma tag com esse nome.");
        }
        tag.renomear(display, normalizado);
        return TagResponse.from(tag);
    }

    @Transactional
    public void excluir(Long professorId, Long tagId) {
        var tag = repository.findByIdAndProfessorId(tagId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag não encontrada."));
        repository.delete(tag);
    }

    private String normalizarExibicao(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da tag é obrigatório.");
        }
        var nfc = Normalizer.normalize(nome, Normalizer.Form.NFC).trim().replaceAll("\\s+", " ");
        if (nfc.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da tag deve ter no máximo 80 caracteres.");
        }
        return nfc;
    }

    private String normalizarChave(String nome) {
        return nome.toLowerCase(Locale.ROOT);
    }
}
