package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeResponse;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import br.com.mathinclui.questao.AlternativaResponse;
import br.com.mathinclui.questao.QuestaoProfessorResponse;
import br.com.mathinclui.questao.domain.Alternativa;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.*;

@Service
public class BibliotecaAtividadeService {
    private final AtividadeRepository atividadeRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    public BibliotecaAtividadeService(AtividadeRepository atividadeRepository, QuestaoRepository questaoRepository,
                                      AlternativaRepository alternativaRepository, NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.atividadeRepository = atividadeRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<BibliotecaAtividadeResumoResponse> listar(Long professorId, BibliotecaAtividadeFiltro f) {
        var params = new MapSqlParameterSource("professorId", professorId);
        String where = montarWhere(f, params);
        Long total = jdbc.queryForObject("SELECT COUNT(DISTINCT a.id) FROM atividades a " + where, params, Long.class);
        params.addValue("limit", f.size()).addValue("offset", f.page() * f.size());
        List<Long> ids = jdbc.queryForList("SELECT DISTINCT a.id FROM atividades a " + where +
                " ORDER BY a.titulo, a.id LIMIT :limit OFFSET :offset", params, Long.class);
        var content = ids.stream().map(id -> atividadeRepository.findById(id)
                .map(a -> BibliotecaAtividadeResumoResponse.from(a, questaoRepository.countByAtividadeId(id),
                        habilidades(id), tags(id))).orElseThrow()).toList();
        return PageResponse.of(content, f.page(), f.size(), total == null ? 0 : total);
    }

    @Transactional(readOnly = true)
    public BibliotecaAtividadeDetalheResponse buscar(Long professorId, Long id) {
        Atividade atividade = fonteVisivel(professorId, id);
        var questoes = questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(id).stream().map(q ->
                QuestaoProfessorResponse.from(q, alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(q.getId()).stream()
                        .map(AlternativaResponse::from).toList())).toList();
        return BibliotecaAtividadeDetalheResponse.from(atividade, habilidades(id), tags(id), questoes);
    }

    @Transactional
    public AtividadeResponse copiar(Long professorId, Long fonteId) {
        Atividade fonte = fonteVisivel(professorId, fonteId);
        var nova = new Atividade(professorId, fonte.getTitulo(), fonte.getDescricao(), fonte.getAnoEscolar(),
                fonte.getConteudo(), fonte.getDificuldade(), fonte.getLimiteTentativas());
        nova.registrarCopiaDe(fonteId, clock.instant());
        nova = atividadeRepository.save(nova);

        for (Long habilidadeId : ids("SELECT habilidade_id FROM atividade_habilidade_bncc WHERE atividade_id=:id", fonteId)) {
            jdbc.update("INSERT INTO atividade_habilidade_bncc(atividade_id,habilidade_id) VALUES (:atividadeId,:id)",
                    new MapSqlParameterSource("atividadeId", nova.getId()).addValue("id", habilidadeId));
        }
        if (fonte.pertenceAoProfessor(professorId)) {
            for (Long tagId : ids("SELECT tag_id FROM atividade_tag WHERE atividade_id=:id", fonteId)) {
                jdbc.update("INSERT INTO atividade_tag(atividade_id,tag_id) VALUES (:atividadeId,:id)",
                        new MapSqlParameterSource("atividadeId", nova.getId()).addValue("id", tagId));
            }
        }

        for (Questao q : questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(fonteId)) {
            var copia = new Questao(nova.getId(), q.getEnunciado(), q.getTipo(), q.getRespostaCorreta(), q.getExplicacao(),
                    q.getDica1(), q.getDica2(), q.getDica3(), q.getImagemUrl(), q.getOrdem(), q.getLimiteTentativas());
            if (q.getOrigemQuestaoModeloId() != null) copia.registrarCopiaDeModelo(q.getOrigemQuestaoModeloId(), clock.instant());
            copia = questaoRepository.save(copia);
            for (Alternativa alternativa : alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(q.getId())) {
                alternativaRepository.save(new Alternativa(copia.getId(), alternativa.getTexto(), alternativa.isCorreta(), alternativa.getOrdem()));
            }
        }
        return AtividadeResponse.from(nova);
    }

    private Atividade fonteVisivel(Long professorId, Long id) {
        return atividadeRepository.findById(id)
                .filter(a -> a.pertenceAoProfessor(professorId) || a.isSistemaPublicada())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade da biblioteca não encontrada."));
    }

    private String montarWhere(BibliotecaAtividadeFiltro f, MapSqlParameterSource p) {
        StringBuilder w = new StringBuilder("WHERE (a.professor_id = :professorId OR (a.tipo_origem = 'SISTEMA' AND a.publicada = TRUE))");
        if (f.origem() != null) { w.append(" AND a.tipo_origem=:origem"); p.addValue("origem", f.origem().name()); }
        if (f.ano() != null) { w.append(" AND a.ano_escolar=:ano"); p.addValue("ano", f.ano()); }
        if (f.conteudo() != null && !f.conteudo().isBlank()) { w.append(" AND a.conteudo=:conteudo"); p.addValue("conteudo", f.conteudo().trim()); }
        if (f.dificuldade() != null) { w.append(" AND a.dificuldade=:dificuldade"); p.addValue("dificuldade", f.dificuldade().name()); }
        if (f.busca() != null && !f.busca().isBlank()) { w.append(" AND (a.titulo ILIKE :busca OR COALESCE(a.descricao,'') ILIKE :busca OR a.conteudo ILIKE :busca)"); p.addValue("busca", "%" + f.busca().trim() + "%"); }
        if ((f.unidadeTematica() != null && !f.unidadeTematica().isBlank()) || (f.objetoConhecimento() != null && !f.objetoConhecimento().isBlank()) || (f.habilidade() != null && !f.habilidade().isBlank())) {
            w.append(" AND EXISTS (SELECT 1 FROM atividade_habilidade_bncc ah JOIN habilidades_bncc h ON h.id=ah.habilidade_id WHERE ah.atividade_id=a.id");
            if (f.unidadeTematica() != null && !f.unidadeTematica().isBlank()) { w.append(" AND h.unidade_tematica ILIKE :unidade"); p.addValue("unidade", f.unidadeTematica().trim()); }
            if (f.objetoConhecimento() != null && !f.objetoConhecimento().isBlank()) { w.append(" AND h.objeto_conhecimento ILIKE :objeto"); p.addValue("objeto", "%" + f.objetoConhecimento().trim() + "%"); }
            if (f.habilidade() != null && !f.habilidade().isBlank()) { w.append(" AND h.codigo=:habilidade"); p.addValue("habilidade", f.habilidade().trim()); }
            w.append(")");
        }
        if (f.tag() != null && !f.tag().isBlank()) {
            w.append(" AND EXISTS (SELECT 1 FROM atividade_tag atg JOIN tags_professor tp ON tp.id=atg.tag_id WHERE atg.atividade_id=a.id AND tp.professor_id=:professorId AND tp.nome_normalizado=:tag)");
            p.addValue("tag", TagService.normalizarNome(f.tag()));
        }
        return w.toString();
    }

    private List<Long> ids(String sql, Long id) {
        return jdbc.queryForList(sql, new MapSqlParameterSource("id", id), Long.class);
    }

    private List<HabilidadeBnccResponse> habilidades(Long atividadeId) {
        var repo = new HabilidadeLookup(jdbc);
        return repo.listar(atividadeId);
    }

    private List<TagResponse> tags(Long atividadeId) {
        return jdbc.query("SELECT tp.id,tp.nome FROM atividade_tag atg JOIN tags_professor tp ON tp.id=atg.tag_id WHERE atg.atividade_id=:id ORDER BY tp.nome",
                new MapSqlParameterSource("id", atividadeId),
                (rs, row) -> new TagResponse(rs.getLong("id"), rs.getString("nome")));
    }

    private static class HabilidadeLookup {
        private final NamedParameterJdbcTemplate jdbc;
        HabilidadeLookup(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
        List<HabilidadeBnccResponse> listar(Long atividadeId) {
            return jdbc.query("SELECT h.id,h.codigo,h.ano_escolar,h.unidade_tematica,h.objeto_conhecimento,h.descricao FROM atividade_habilidade_bncc ah JOIN habilidades_bncc h ON h.id=ah.habilidade_id WHERE ah.atividade_id=:id ORDER BY h.codigo",
                    new MapSqlParameterSource("id", atividadeId), (rs, row) -> new HabilidadeBnccResponse(
                            rs.getLong("id"), rs.getString("codigo"), rs.getShort("ano_escolar"),
                            rs.getString("unidade_tematica"), rs.getString("objeto_conhecimento"), rs.getString("descricao")));
        }
    }
}
