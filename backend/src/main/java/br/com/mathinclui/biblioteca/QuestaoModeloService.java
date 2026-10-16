package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.biblioteca.domain.AlternativaModelo;
import br.com.mathinclui.biblioteca.domain.QuestaoModelo;
import br.com.mathinclui.biblioteca.domain.TipoOrigemBiblioteca;
import br.com.mathinclui.biblioteca.repository.AlternativaModeloRepository;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.QuestaoModeloRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
import br.com.mathinclui.questao.AlternativaResponse;
import br.com.mathinclui.questao.QuestaoProfessorResponse;
import br.com.mathinclui.questao.domain.Alternativa;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class QuestaoModeloService {
    private final AtividadeService atividadeService;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final QuestaoModeloRepository modeloRepository;
    private final AlternativaModeloRepository alternativaModeloRepository;
    private final HabilidadeBnccRepository habilidadeRepository;
    private final TagProfessorRepository tagRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    public QuestaoModeloService(AtividadeService atividadeService, QuestaoRepository questaoRepository,
                                AlternativaRepository alternativaRepository, QuestaoModeloRepository modeloRepository,
                                AlternativaModeloRepository alternativaModeloRepository,
                                HabilidadeBnccRepository habilidadeRepository, TagProfessorRepository tagRepository,
                                NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.atividadeService = atividadeService;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.modeloRepository = modeloRepository;
        this.alternativaModeloRepository = alternativaModeloRepository;
        this.habilidadeRepository = habilidadeRepository;
        this.tagRepository = tagRepository;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestaoModeloResponse> listar(Long professorId, BibliotecaQuestaoFiltro f) {
        var params = new MapSqlParameterSource("professorId", professorId);
        String where = montarWhere(f, params);
        Long total = jdbc.queryForObject("SELECT COUNT(DISTINCT q.id) FROM questoes_modelo q " + where, params, Long.class);
        params.addValue("limit", f.size()).addValue("offset", f.page() * f.size());
        List<Long> ids = jdbc.queryForList("SELECT DISTINCT q.id FROM questoes_modelo q " + where +
                " ORDER BY q.enunciado, q.id LIMIT :limit OFFSET :offset", params, Long.class);
        var content = ids.stream().map(id -> response(visivel(professorId, id))).toList();
        return PageResponse.of(content, f.page(), f.size(), total == null ? 0 : total);
    }

    @Transactional(readOnly = true)
    public QuestaoModeloResponse buscar(Long professorId, Long id) {
        return response(visivel(professorId, id));
    }

    @Transactional
    public QuestaoModeloResponse salvarNoBanco(Long professorId, Long atividadeId, Long questaoId,
                                                SalvarQuestaoNoBancoRequest request) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        Questao questao = questaoRepository.findByIdAndAtividadeId(questaoId, atividadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
        validarClassificacao(professorId, request.habilidadeIds(), request.tagIds());
        List<Alternativa> alternativas = alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(questaoId);
        validarAlternativasReutilizaveis(questao.getTipo(), alternativas.stream().map(Alternativa::isCorreta).toList());

        QuestaoModelo modelo = new QuestaoModelo(professorId, TipoOrigemBiblioteca.PROFESSOR, questao.getEnunciado(),
                questao.getTipo(), questao.getRespostaCorreta(), questao.getExplicacao(), questao.getDica1(),
                questao.getDica2(), questao.getDica3(), questao.getImagemUrl(), questao.getLimiteTentativas(),
                request.anoEscolar(), request.conteudo(), request.dificuldade());
        modelo = modeloRepository.save(modelo);
        var alternativasSalvas = new ArrayList<AlternativaModeloResponse>();
        for (Alternativa a : alternativas) {
            AlternativaModelo salva = alternativaModeloRepository.save(
                    new AlternativaModelo(modelo.getId(), a.getTexto(), a.isCorreta(), a.getOrdem()));
            alternativasSalvas.add(AlternativaModeloResponse.from(salva));
        }
        salvarClassificacao(modelo.getId(), request.habilidadeIds(), request.tagIds());
        return response(modelo, alternativasSalvas);
    }

    @Transactional
    public QuestaoModeloResponse copiar(Long professorId, Long modeloId) {
        QuestaoModelo fonte = visivel(professorId, modeloId);
        QuestaoModelo copia = new QuestaoModelo(professorId, TipoOrigemBiblioteca.PROFESSOR, fonte.getEnunciado(),
                fonte.getTipo(), fonte.getRespostaCorreta(), fonte.getExplicacao(), fonte.getDica1(), fonte.getDica2(),
                fonte.getDica3(), fonte.getImagemUrl(), fonte.getLimiteTentativas(), fonte.getAnoEscolar(),
                fonte.getConteudo(), fonte.getDificuldade());
        copia.registrarCopiaDe(modeloId, clock.instant());
        copia = modeloRepository.save(copia);
        var alternativasSalvas = new ArrayList<AlternativaModeloResponse>();
        for (AlternativaModelo a : alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(modeloId)) {
            AlternativaModelo salva = alternativaModeloRepository.save(
                    new AlternativaModelo(copia.getId(), a.getTexto(), a.isCorreta(), a.getOrdem()));
            alternativasSalvas.add(AlternativaModeloResponse.from(salva));
        }
        List<Long> habilidades = ids("SELECT habilidade_id FROM questao_modelo_habilidade_bncc WHERE questao_modelo_id=:id", modeloId);
        List<Long> tags = fonte.getTipoOrigem() == TipoOrigemBiblioteca.PROFESSOR
                ? ids("SELECT tag_id FROM questao_modelo_tag WHERE questao_modelo_id=:id", modeloId) : List.of();
        salvarClassificacao(copia.getId(), habilidades, tags);
        return response(copia, alternativasSalvas);
    }

    @Transactional
    public List<QuestaoProfessorResponse> importar(Long professorId, Long atividadeId, ImportarQuestoesRequest request) {
        List<Long> solicitados = request.questoesModeloIds();
        if (solicitados == null || solicitados.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione ao menos uma questão.");
        if (new LinkedHashSet<>(solicitados).size() != solicitados.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mesma questão não pode ser importada duas vezes no mesmo pedido.");

        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        atividadeService.validarSemHistorico(atividadeId);
        List<QuestaoModelo> modelos = solicitados.stream().map(id -> visivel(professorId, id)).toList();
        for (QuestaoModelo m : modelos) {
            List<Boolean> corretas = alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(m.getId())
                    .stream().map(AlternativaModelo::isCorreta).toList();
            validarAlternativasReutilizaveis(m.getTipo(), corretas);
        }

        int ordem = questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(atividadeId).stream()
                .mapToInt(Questao::getOrdem).max().orElse(0);
        var result = new ArrayList<QuestaoProfessorResponse>();
        for (QuestaoModelo modelo : modelos) {
            List<AlternativaModelo> alternativasModelo = alternativaModeloRepository
                    .findAllByQuestaoModeloIdOrderByOrdemAsc(modelo.getId());
            Questao q = new Questao(atividadeId, modelo.getEnunciado(), modelo.getTipo(), modelo.getRespostaCorreta(),
                    modelo.getExplicacao(), modelo.getDica1(), modelo.getDica2(), modelo.getDica3(), modelo.getImagemUrl(),
                    ++ordem, modelo.getLimiteTentativas());
            q.registrarCopiaDeModelo(modelo.getId(), clock.instant());
            q = questaoRepository.save(q);
            var alternativas = new ArrayList<AlternativaResponse>();
            for (AlternativaModelo a : alternativasModelo) {
                Alternativa salva = alternativaRepository.save(new Alternativa(q.getId(), a.getTexto(), a.isCorreta(), a.getOrdem()));
                alternativas.add(AlternativaResponse.from(salva));
            }
            result.add(QuestaoProfessorResponse.from(q, alternativas));
        }
        return result;
    }

    private QuestaoModelo visivel(Long professorId, Long id) {
        return modeloRepository.findById(id)
                .filter(m -> m.getTipoOrigem() == TipoOrigemBiblioteca.SISTEMA || professorId.equals(m.getProfessorId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão da biblioteca não encontrada."));
    }

    private void validarClassificacao(Long professorId, List<Long> habilidadeIds, List<Long> tagIds) {
        for (Long id : new LinkedHashSet<>(habilidadeIds)) {
            if (!habilidadeRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Habilidade BNCC não encontrada.");
        }
        for (Long id : new LinkedHashSet<>(tagIds)) {
            if (tagRepository.findByIdAndProfessorId(id, professorId).isEmpty())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag não encontrada.");
        }
    }

    private void validarAlternativasReutilizaveis(TipoQuestao tipo, List<Boolean> corretas) {
        if (tipo == TipoQuestao.MULTIPLA_ESCOLHA) {
            long corretasCount = corretas.stream().filter(Boolean::booleanValue).count();
            if (corretas.size() < 2 || corretasCount != 1)
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Questão de múltipla escolha reutilizável exige ao menos duas alternativas e exatamente uma correta.");
        }
    }

    private void salvarClassificacao(Long modeloId, List<Long> habilidadeIds, List<Long> tagIds) {
        for (Long id : new LinkedHashSet<>(habilidadeIds)) {
            jdbc.update("INSERT INTO questao_modelo_habilidade_bncc(questao_modelo_id,habilidade_id) VALUES (:modeloId,:id)",
                    new MapSqlParameterSource("modeloId", modeloId).addValue("id", id));
        }
        for (Long id : new LinkedHashSet<>(tagIds)) {
            jdbc.update("INSERT INTO questao_modelo_tag(questao_modelo_id,tag_id) VALUES (:modeloId,:id)",
                    new MapSqlParameterSource("modeloId", modeloId).addValue("id", id));
        }
    }

    private QuestaoModeloResponse response(QuestaoModelo modelo) {
        var alternativas = alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(modelo.getId()).stream()
                .map(AlternativaModeloResponse::from).toList();
        return response(modelo, alternativas);
    }

    private QuestaoModeloResponse response(QuestaoModelo modelo, List<AlternativaModeloResponse> alternativas) {
        return QuestaoModeloResponse.from(modelo, alternativas,
                ids("SELECT habilidade_id FROM questao_modelo_habilidade_bncc WHERE questao_modelo_id=:id", modelo.getId()),
                ids("SELECT tag_id FROM questao_modelo_tag WHERE questao_modelo_id=:id", modelo.getId()));
    }

    private List<Long> ids(String sql, Long id) {
        return jdbc.queryForList(sql, new MapSqlParameterSource("id", id), Long.class);
    }

    private String montarWhere(BibliotecaQuestaoFiltro f, MapSqlParameterSource p) {
        StringBuilder w = new StringBuilder("WHERE (q.professor_id=:professorId OR q.tipo_origem='SISTEMA')");
        if (f.origem() != null) { w.append(" AND q.tipo_origem=:origem"); p.addValue("origem", f.origem().name()); }
        if (f.ano() != null) { w.append(" AND q.ano_escolar=:ano"); p.addValue("ano", f.ano()); }
        if (f.conteudo() != null && !f.conteudo().isBlank()) { w.append(" AND q.conteudo=:conteudo"); p.addValue("conteudo", f.conteudo().trim()); }
        if (f.dificuldade() != null) { w.append(" AND q.dificuldade=:dificuldade"); p.addValue("dificuldade", f.dificuldade().name()); }
        if (f.tipo() != null) { w.append(" AND q.tipo=:tipo"); p.addValue("tipo", f.tipo().name()); }
        if (f.busca() != null && !f.busca().isBlank()) { w.append(" AND q.enunciado ILIKE :busca"); p.addValue("busca", "%" + f.busca().trim() + "%"); }
        if (f.habilidade() != null && !f.habilidade().isBlank()) {
            w.append(" AND EXISTS (SELECT 1 FROM questao_modelo_habilidade_bncc qh JOIN habilidades_bncc h ON h.id=qh.habilidade_id WHERE qh.questao_modelo_id=q.id AND h.codigo=:habilidade)");
            p.addValue("habilidade", f.habilidade().trim());
        }
        if (f.tag() != null && !f.tag().isBlank()) {
            w.append(" AND EXISTS (SELECT 1 FROM questao_modelo_tag qt JOIN tags_professor tp ON tp.id=qt.tag_id WHERE qt.questao_modelo_id=q.id AND tp.professor_id=:professorId AND tp.nome_normalizado=:tag)");
            p.addValue("tag", TagService.normalizarNome(f.tag()));
        }
        return w.toString();
    }
}
