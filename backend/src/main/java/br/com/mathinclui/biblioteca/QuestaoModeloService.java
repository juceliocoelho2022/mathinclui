package br.com.mathinclui.biblioteca;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.biblioteca.domain.AlternativaModelo;
import br.com.mathinclui.biblioteca.domain.QuestaoModelo;
import br.com.mathinclui.biblioteca.domain.TipoOrigemBiblioteca;
import br.com.mathinclui.biblioteca.repository.AlternativaModeloRepository;
import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import br.com.mathinclui.biblioteca.repository.QuestaoModeloRepository;
import br.com.mathinclui.biblioteca.repository.TagProfessorRepository;
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
import java.util.HashSet;
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
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public QuestaoModeloService(AtividadeService atividadeService,
                                QuestaoRepository questaoRepository,
                                AlternativaRepository alternativaRepository,
                                QuestaoModeloRepository modeloRepository,
                                AlternativaModeloRepository alternativaModeloRepository,
                                HabilidadeBnccRepository habilidadeRepository,
                                TagProfessorRepository tagRepository,
                                JdbcTemplate jdbcTemplate,
                                Clock clock) {
        this.atividadeService = atividadeService;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.modeloRepository = modeloRepository;
        this.alternativaModeloRepository = alternativaModeloRepository;
        this.habilidadeRepository = habilidadeRepository;
        this.tagRepository = tagRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Transactional
    public QuestaoModeloResponse salvarNoBanco(Long professorId, Long atividadeId, Long questaoId,
                                                SalvarQuestaoNoBancoRequest request) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        validarMetadados(request);
        var questao = questaoRepository.findByIdAndAtividadeId(questaoId, atividadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
        validarHabilidades(request.habilidadeIds());
        validarTags(professorId, request.tagIds());

        var modelo = new QuestaoModelo(professorId, TipoOrigemBiblioteca.PROFESSOR,
                questao.getEnunciado(), questao.getTipo(), questao.getRespostaCorreta(), questao.getExplicacao(),
                questao.getDica1(), questao.getDica2(), questao.getDica3(), questao.getImagemUrl(),
                questao.getLimiteTentativas(), request.anoEscolar(), request.conteudo().trim(), request.dificuldade());
        modelo = modeloRepository.save(modelo);

        var alternativas = new ArrayList<AlternativaModelo>();
        for (var alternativa : alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(questaoId)) {
            var copiaAlternativa = new AlternativaModelo(modelo.getId(), alternativa.getTexto(), alternativa.isCorreta(), alternativa.getOrdem());
            alternativaModeloRepository.save(copiaAlternativa);
            alternativas.add(copiaAlternativa);
        }
        salvarClassificacao(modelo.getId(), request.habilidadeIds(), request.tagIds());
        return QuestaoModeloResponse.from(modelo, alternativas);
    }

    @Transactional
    public QuestaoModeloResponse copiarModelo(Long professorId, Long modeloId) {
        var origem = obterVisivel(professorId, modeloId);
        var copia = new QuestaoModelo(professorId, TipoOrigemBiblioteca.PROFESSOR,
                origem.getEnunciado(), origem.getTipo(), origem.getRespostaCorreta(), origem.getExplicacao(),
                origem.getDica1(), origem.getDica2(), origem.getDica3(), origem.getImagemUrl(),
                origem.getLimiteTentativas(), origem.getAnoEscolar(), origem.getConteudo(), origem.getDificuldade());
        copia.marcarOrigem(origem.getId(), clock.instant());
        copia = modeloRepository.save(copia);

        var alternativas = new ArrayList<AlternativaModelo>();
        for (var alternativa : alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(origem.getId())) {
            var copiaAlternativa = new AlternativaModelo(copia.getId(), alternativa.getTexto(), alternativa.isCorreta(), alternativa.getOrdem());
            alternativaModeloRepository.save(copiaAlternativa);
            alternativas.add(copiaAlternativa);
        }
        copiarClassificacao(origem.getId(), copia.getId());
        return QuestaoModeloResponse.from(copia, alternativas);
    }

    @Transactional
    public List<QuestaoImportadaResponse> importar(Long professorId, Long atividadeId, ImportarQuestoesRequest request) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        if (request.questoesModeloIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos uma questão-modelo.");
        }
        var ids = new HashSet<Long>();
        for (var id : request.questoesModeloIds()) {
            if (id == null || !ids.add(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de questão-modelo repetido ou inválido.");
            }
        }
        atividadeService.validarSemHistorico(atividadeId);

        int proximaOrdem = questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(atividadeId).stream()
                .mapToInt(Questao::getOrdem).max().orElse(0) + 1;
        var result = new ArrayList<QuestaoImportadaResponse>();
        for (var modeloId : request.questoesModeloIds()) {
            var modelo = obterVisivel(professorId, modeloId);
            var questao = new Questao(atividadeId, modelo.getEnunciado(), modelo.getTipo(), modelo.getRespostaCorreta(),
                    modelo.getExplicacao(), modelo.getDica1(), modelo.getDica2(), modelo.getDica3(), modelo.getImagemUrl(),
                    proximaOrdem++, modelo.getLimiteTentativas());
            questao.marcarOrigemModelo(modelo.getId(), clock.instant());
            questao = questaoRepository.save(questao);
            for (var alternativa : alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(modelo.getId())) {
                alternativaRepository.save(new Alternativa(questao.getId(), alternativa.getTexto(),
                        alternativa.isCorreta(), alternativa.getOrdem()));
            }
            result.add(QuestaoImportadaResponse.from(questao));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public QuestaoModeloResponse detalhar(Long professorId, Long modeloId) {
        var modelo = obterVisivel(professorId, modeloId);
        return QuestaoModeloResponse.from(modelo,
                alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(modeloId));
    }

    @Transactional(readOnly = true)
    public List<QuestaoModeloResponse> listar(Long professorId) {
        var result = new ArrayList<QuestaoModeloResponse>();
        modeloRepository.findAllByProfessorIdOrderByCriadoEmDesc(professorId).stream()
                .map(m -> QuestaoModeloResponse.from(m,
                        alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(m.getId())))
                .forEach(result::add);
        modeloRepository.findAllByTipoOrigemOrderByCriadoEmDesc(TipoOrigemBiblioteca.SISTEMA).stream()
                .map(m -> QuestaoModeloResponse.from(m,
                        alternativaModeloRepository.findAllByQuestaoModeloIdOrderByOrdemAsc(m.getId())))
                .forEach(result::add);
        return result;
    }

    private QuestaoModelo obterVisivel(Long professorId, Long modeloId) {
        return modeloRepository.findById(modeloId)
                .filter(m -> m.getTipoOrigem() == TipoOrigemBiblioteca.SISTEMA
                        || (m.getTipoOrigem() == TipoOrigemBiblioteca.PROFESSOR
                        && professorId.equals(m.getProfessorId())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão da biblioteca não encontrada."));
    }

    private void validarMetadados(SalvarQuestaoNoBancoRequest request) {
        if (request.anoEscolar() < 6 || request.anoEscolar() > 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano escolar deve estar entre 6 e 8.");
        }
        if (request.conteudo() == null || request.conteudo().isBlank() || request.dificuldade() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conteúdo e dificuldade são obrigatórios.");
        }
    }

    private void validarHabilidades(List<Long> ids) {
        var encontradas = habilidadeRepository.findAllById(ids);
        if (encontradas.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais habilidades BNCC são inválidas.");
        }
    }

    private void validarTags(Long professorId, List<Long> ids) {
        for (var id : ids) {
            tagRepository.findByIdAndProfessorId(id, professorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais tags são inválidas."));
        }
    }

    private void salvarClassificacao(Long modeloId, List<Long> habilidadeIds, List<Long> tagIds) {
        for (var habilidadeId : habilidadeIds) {
            jdbcTemplate.update("INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id) VALUES (?, ?)",
                    modeloId, habilidadeId);
        }
        for (var tagId : tagIds) {
            jdbcTemplate.update("INSERT INTO questao_modelo_tag (questao_modelo_id, tag_id) VALUES (?, ?)",
                    modeloId, tagId);
        }
    }

    private void copiarClassificacao(Long origemId, Long copiaId) {
        jdbcTemplate.update("INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id) "
                + "SELECT ?, habilidade_id FROM questao_modelo_habilidade_bncc WHERE questao_modelo_id = ?", copiaId, origemId);
        jdbcTemplate.update("INSERT INTO questao_modelo_tag (questao_modelo_id, tag_id) "
                + "SELECT ?, tag_id FROM questao_modelo_tag WHERE questao_modelo_id = ?", copiaId, origemId);
    }
}
