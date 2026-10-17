package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.questao.domain.Alternativa;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

@Service
public class BibliotecaAtividadeService {
    private final AtividadeRepository atividadeRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public BibliotecaAtividadeService(AtividadeRepository atividadeRepository,
                                      QuestaoRepository questaoRepository,
                                      AlternativaRepository alternativaRepository,
                                      JdbcTemplate jdbcTemplate,
                                      Clock clock) {
        this.atividadeRepository = atividadeRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<BibliotecaAtividadeResumoResponse> listar(Long professorId, BibliotecaAtividadeFiltro filtro) {
        var atividades = new ArrayList<Atividade>();
        atividadeRepository.findAllByProfessorIdOrderByCriadoEmDesc(professorId).stream()
                .filter(a -> filtro.origem() == null || filtro.origem() == TipoOrigemAtividade.PROFESSOR)
                .forEach(atividades::add);
        atividadeRepository.findAllByTipoOrigemAndPublicadaTrueOrderByTituloAsc(TipoOrigemAtividade.SISTEMA).stream()
                .filter(a -> filtro.origem() == null || filtro.origem() == TipoOrigemAtividade.SISTEMA)
                .forEach(atividades::add);

        var filtradas = atividades.stream()
                .filter(a -> filtro.ano() == null || a.getAnoEscolar() == filtro.ano())
                .filter(a -> filtro.conteudo() == null || a.getConteudo().equalsIgnoreCase(filtro.conteudo()))
                .filter(a -> filtro.dificuldade() == null || a.getDificuldade() == filtro.dificuldade())
                .filter(a -> correspondeBusca(a, filtro.busca()))
                .filter(a -> correspondeCurriculo(a.getId(), filtro))
                .sorted(Comparator.comparing(Atividade::getTitulo, String.CASE_INSENSITIVE_ORDER).thenComparing(Atividade::getId))
                .toList();

        int from = Math.min(filtro.page() * filtro.size(), filtradas.size());
        int to = Math.min(from + filtro.size(), filtradas.size());
        var items = filtradas.subList(from, to).stream().map(BibliotecaAtividadeResumoResponse::from).toList();
        return new PageResponse<>(items, filtro.page(), filtro.size(), filtradas.size());
    }

    @Transactional(readOnly = true)
    public BibliotecaAtividadeDetalheResponse detalhar(Long professorId, Long atividadeId) {
        return BibliotecaAtividadeDetalheResponse.from(obterVisivel(professorId, atividadeId));
    }

    @Transactional
    public BibliotecaAtividadeDetalheResponse copiar(Long professorId, Long atividadeId) {
        var origem = obterVisivel(professorId, atividadeId);
        var copia = new Atividade(professorId, origem.getTitulo(), origem.getDescricao(), origem.getAnoEscolar(),
                origem.getConteudo(), origem.getDificuldade(), origem.getLimiteTentativas());
        copia.marcarOrigem(origem.getId(), clock.instant());
        copia = atividadeRepository.save(copia);

        copiarClassificacao(origem.getId(), copia.getId());
        copiarQuestoes(origem.getId(), copia.getId());
        return BibliotecaAtividadeDetalheResponse.from(copia);
    }

    private Atividade obterVisivel(Long professorId, Long atividadeId) {
        return atividadeRepository.findById(atividadeId)
                .filter(a -> a.pertenceAoProfessor(professorId) || a.isSistemaPublicada())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade da biblioteca não encontrada."));
    }

    private boolean correspondeBusca(Atividade atividade, String busca) {
        if (busca == null || busca.isBlank()) return true;
        var termo = busca.trim().toLowerCase(Locale.ROOT);
        if (atividade.getTitulo().toLowerCase(Locale.ROOT).contains(termo)
                || (atividade.getDescricao() != null && atividade.getDescricao().toLowerCase(Locale.ROOT).contains(termo))
                || atividade.getConteudo().toLowerCase(Locale.ROOT).contains(termo)) {
            return true;
        }
        var padrao = "%" + termo + "%";
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM atividade_habilidade_bncc ah "
                        + "JOIN habilidades_bncc h ON h.id = ah.habilidade_id "
                        + "WHERE ah.atividade_id = ? AND ("
                        + "LOWER(h.codigo) LIKE ? OR LOWER(h.unidade_tematica) LIKE ? "
                        + "OR LOWER(h.objeto_conhecimento) LIKE ? OR LOWER(h.descricao) LIKE ?)",
                Integer.class, atividade.getId(), padrao, padrao, padrao, padrao);
        return total != null && total > 0;
    }

    private boolean correspondeCurriculo(Long atividadeId, BibliotecaAtividadeFiltro filtro) {
        if (filtro.unidadeTematica() != null && !existeCurriculo(atividadeId, "h.unidade_tematica", filtro.unidadeTematica())) return false;
        if (filtro.objetoConhecimento() != null && !existeCurriculo(atividadeId, "h.objeto_conhecimento", filtro.objetoConhecimento())) return false;
        if (filtro.habilidade() != null && !existeCurriculo(atividadeId, "h.codigo", filtro.habilidade())) return false;
        if (filtro.tag() != null) {
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM atividade_tag WHERE atividade_id = ? AND tag_id = ?", Integer.class,
                    atividadeId, filtro.tag());
            if (total == null || total == 0) return false;
        }
        return true;
    }

    private boolean existeCurriculo(Long atividadeId, String coluna, String valor) {
        var sql = "SELECT COUNT(*) FROM atividade_habilidade_bncc ah JOIN habilidades_bncc h ON h.id = ah.habilidade_id "
                + "WHERE ah.atividade_id = ? AND LOWER(" + coluna + ") = LOWER(?)";
        Integer total = jdbcTemplate.queryForObject(sql, Integer.class, atividadeId, valor);
        return total != null && total > 0;
    }

    private void copiarClassificacao(Long origemId, Long copiaId) {
        jdbcTemplate.update(
                "INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id) "
                        + "SELECT ?, habilidade_id FROM atividade_habilidade_bncc WHERE atividade_id = ?",
                copiaId, origemId);
        jdbcTemplate.update(
                "INSERT INTO atividade_tag (atividade_id, tag_id) "
                        + "SELECT ?, tag_id FROM atividade_tag WHERE atividade_id = ?",
                copiaId, origemId);
    }

    private void copiarQuestoes(Long origemId, Long copiaId) {
        for (var questaoOrigem : questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(origemId)) {
            var copiaQuestao = new Questao(copiaId, questaoOrigem.getEnunciado(), questaoOrigem.getTipo(),
                    questaoOrigem.getRespostaCorreta(), questaoOrigem.getExplicacao(), questaoOrigem.getDica1(),
                    questaoOrigem.getDica2(), questaoOrigem.getDica3(), questaoOrigem.getImagemUrl(),
                    questaoOrigem.getOrdem(), questaoOrigem.getLimiteTentativas());
            copiaQuestao = questaoRepository.save(copiaQuestao);
            for (var alternativaOrigem : alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(questaoOrigem.getId())) {
                alternativaRepository.save(new Alternativa(copiaQuestao.getId(), alternativaOrigem.getTexto(),
                        alternativaOrigem.isCorreta(), alternativaOrigem.getOrdem()));
            }
        }
    }
}
