package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ClassificacaoAtividadeService {
    private final AtividadeService atividadeService;
    private final HabilidadeBnccRepository habilidadeRepository;
    private final TagProfessorRepository tagRepository;
    private final JdbcTemplate jdbcTemplate;

    public ClassificacaoAtividadeService(AtividadeService atividadeService,
                                         HabilidadeBnccRepository habilidadeRepository,
                                         TagProfessorRepository tagRepository,
                                         JdbcTemplate jdbcTemplate) {
        this.atividadeService = atividadeService;
        this.habilidadeRepository = habilidadeRepository;
        this.tagRepository = tagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public ClassificacaoAtividadeResponse buscar(Long professorId, Long atividadeId) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        var habilidades = jdbcTemplate.queryForList(
                "SELECT habilidade_id FROM atividade_habilidade_bncc WHERE atividade_id = ? ORDER BY habilidade_id",
                Long.class, atividadeId);
        var tags = jdbcTemplate.queryForList(
                "SELECT tag_id FROM atividade_tag WHERE atividade_id = ? ORDER BY tag_id",
                Long.class, atividadeId);
        return new ClassificacaoAtividadeResponse(habilidades, tags);
    }

    @Transactional
    public ClassificacaoAtividadeResponse salvar(Long professorId, Long atividadeId, ClassificacaoAtividadeRequest request) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        atividadeService.validarSemHistorico(atividadeId);

        var habilidadeIds = distintos(request.habilidadeIds(), "Habilidade BNCC repetida.");
        var tagIds = distintos(request.tagIds(), "Tag repetida.");

        if (!habilidadeIds.isEmpty() && habilidadeRepository.findAllById(habilidadeIds).size() != habilidadeIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais habilidades BNCC são inválidas.");
        }
        for (var tagId : tagIds) {
            tagRepository.findByIdAndProfessorId(tagId, professorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais tags são inválidas."));
        }

        jdbcTemplate.update("DELETE FROM atividade_habilidade_bncc WHERE atividade_id = ?", atividadeId);
        jdbcTemplate.update("DELETE FROM atividade_tag WHERE atividade_id = ?", atividadeId);
        for (var habilidadeId : habilidadeIds) {
            jdbcTemplate.update("INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id) VALUES (?, ?)", atividadeId, habilidadeId);
        }
        for (var tagId : tagIds) {
            jdbcTemplate.update("INSERT INTO atividade_tag (atividade_id, tag_id) VALUES (?, ?)", atividadeId, tagId);
        }
        return new ClassificacaoAtividadeResponse(habilidadeIds, tagIds);
    }

    private List<Long> distintos(List<Long> ids, String mensagem) {
        var set = new LinkedHashSet<>(ids);
        if (set.size() != ids.size() || set.contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return List.copyOf(set);
    }
}
