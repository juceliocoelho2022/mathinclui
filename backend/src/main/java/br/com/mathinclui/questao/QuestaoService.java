package br.com.mathinclui.questao;

import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.questao.domain.Alternativa;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class QuestaoService {
    private final AtividadeService atividadeService;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;

    public QuestaoService(AtividadeService atividadeService, QuestaoRepository questaoRepository,
                          AlternativaRepository alternativaRepository) {
        this.atividadeService = atividadeService;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
    }

    @Transactional
    public QuestaoProfessorResponse criar(Long professorId, Long atividadeId, CreateQuestaoRequest request) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        atividadeService.validarSemHistorico(atividadeId);
        validarAlternativas(request.tipo(), request.alternativas());
        var questao = questaoRepository.save(new Questao(atividadeId, request.enunciado(), request.tipo(),
                request.respostaCorreta(), request.explicacao(), request.dica1(), request.dica2(), request.dica3(),
                request.imagemUrl(), request.ordem(), request.limiteTentativas()));
        var alternativas = salvarAlternativas(questao, request.alternativas());
        return response(questao, alternativas);
    }

    @Transactional(readOnly = true)
    public List<QuestaoProfessorResponse> listar(Long professorId, Long atividadeId) {
        atividadeService.buscar(professorId, atividadeId);
        return questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(atividadeId).stream()
                .map(q -> response(q, alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(q.getId())))
                .toList();
    }

    @Transactional
    public QuestaoProfessorResponse atualizar(Long professorId, Long atividadeId, Long questaoId,
                                              UpdateQuestaoRequest request) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        atividadeService.validarSemHistorico(atividadeId);
        validarAlternativas(request.tipo(), request.alternativas());
        var questao = obter(atividadeId, questaoId);
        questao.atualizar(request.enunciado(), request.tipo(), request.respostaCorreta(), request.explicacao(),
                request.dica1(), request.dica2(), request.dica3(), request.imagemUrl(), request.ordem(),
                request.limiteTentativas());
        alternativaRepository.deleteAllByQuestaoId(questaoId);
        alternativaRepository.flush();
        var alternativas = salvarAlternativas(questao, request.alternativas());
        return response(questao, alternativas);
    }

    @Transactional
    public void excluir(Long professorId, Long atividadeId, Long questaoId) {
        atividadeService.obterEditavelDoProfessor(professorId, atividadeId);
        atividadeService.validarSemHistorico(atividadeId);
        var questao = obter(atividadeId, questaoId);
        questaoRepository.delete(questao);
    }

    private Questao obter(Long atividadeId, Long questaoId) {
        return questaoRepository.findByIdAndAtividadeId(questaoId, atividadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
    }

    private void validarAlternativas(TipoQuestao tipo, List<AlternativaRequest> alternativas) {
        var lista = alternativas == null ? List.<AlternativaRequest>of() : alternativas;
        if (tipo != TipoQuestao.MULTIPLA_ESCOLHA && !lista.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas questão de múltipla escolha pode possuir alternativas.");
        }
        if (tipo == TipoQuestao.MULTIPLA_ESCOLHA && lista.stream().filter(AlternativaRequest::correta).count() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Questão de múltipla escolha pode ter no máximo uma alternativa correta durante a edição.");
        }
    }

    private List<Alternativa> salvarAlternativas(Questao questao, List<AlternativaRequest> requests) {
        if (requests == null || requests.isEmpty()) return List.of();
        return requests.stream()
                .map(r -> alternativaRepository.save(new Alternativa(questao.getId(), r.texto(), r.correta(), r.ordem())))
                .toList();
    }

    private QuestaoProfessorResponse response(Questao questao, List<Alternativa> alternativas) {
        return QuestaoProfessorResponse.from(questao,
                alternativas.stream().map(AlternativaResponse::from).toList());
    }
}
