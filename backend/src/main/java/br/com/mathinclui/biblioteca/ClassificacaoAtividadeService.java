package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    private final NamedParameterJdbcTemplate jdbc;

    public ClassificacaoAtividadeService(AtividadeService atividadeService, HabilidadeBnccRepository habilidadeRepository,
                                         TagProfessorRepository tagRepository, NamedParameterJdbcTemplate jdbc) {
        this.atividadeService = atividadeService;
        this.habilidadeRepository = habilidadeRepository;
        this.tagRepository = tagRepository;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public ClassificacaoAtividadeResponse buscar(Long professorId, Long atividadeId) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        return carregar(atividadeId);
    }

    @Transactional
    public ClassificacaoAtividadeResponse atualizar(Long professorId, Long atividadeId, ClassificacaoAtividadeRequest request) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        atividadeService.validarSemHistorico(atividadeId);
        var habilidadeIds = new LinkedHashSet<>(request.habilidadeIds());
        var tagIds = new LinkedHashSet<>(request.tagIds());

        for (Long id : habilidadeIds) {
            if (!habilidadeRepository.existsById(id))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Habilidade BNCC não encontrada.");
        }
        for (Long id : tagIds) {
            if (tagRepository.findByIdAndProfessorId(id, professorId).isEmpty())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag não encontrada.");
        }

        var params = new MapSqlParameterSource("atividadeId", atividadeId);
        jdbc.update("DELETE FROM atividade_habilidade_bncc WHERE atividade_id=:atividadeId", params);
        jdbc.update("DELETE FROM atividade_tag WHERE atividade_id=:atividadeId", params);
        for (Long habilidadeId : habilidadeIds) {
            jdbc.update("INSERT INTO atividade_habilidade_bncc(atividade_id,habilidade_id) VALUES (:atividadeId,:id)",
                    new MapSqlParameterSource("atividadeId", atividadeId).addValue("id", habilidadeId));
        }
        for (Long tagId : tagIds) {
            jdbc.update("INSERT INTO atividade_tag(atividade_id,tag_id) VALUES (:atividadeId,:id)",
                    new MapSqlParameterSource("atividadeId", atividadeId).addValue("id", tagId));
        }
        return carregar(atividadeId);
    }

    private ClassificacaoAtividadeResponse carregar(Long atividadeId) {
        var p = new MapSqlParameterSource("id", atividadeId);
        List<Long> habilidades = jdbc.queryForList(
                "SELECT habilidade_id FROM atividade_habilidade_bncc WHERE atividade_id=:id ORDER BY habilidade_id", p, Long.class);
        List<Long> tags = jdbc.queryForList(
                "SELECT tag_id FROM atividade_tag WHERE atividade_id=:id ORDER BY tag_id", p, Long.class);
        var h = habilidades.stream().map(id -> habilidadeRepository.findById(id).orElseThrow()).map(HabilidadeBnccResponse::from).toList();
        var t = tags.stream().map(id -> tagRepository.findById(id).orElseThrow()).map(TagResponse::from).toList();
        return new ClassificacaoAtividadeResponse(h, t);
    }
}
