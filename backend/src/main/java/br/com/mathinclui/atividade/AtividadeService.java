package br.com.mathinclui.atividade;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AtividadeService {
    private final AtividadeRepository atividadeRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;

    public AtividadeService(AtividadeRepository atividadeRepository, QuestaoRepository questaoRepository,
                            AlternativaRepository alternativaRepository) {
        this.atividadeRepository = atividadeRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
    }

    @Transactional
    public AtividadeResponse criar(Long professorId, CreateAtividadeRequest request) {
        var atividade = new Atividade(professorId, request.titulo(), request.descricao(), request.anoEscolar(),
                request.conteudo(), request.dificuldade(), request.limiteTentativas());
        return AtividadeResponse.from(atividadeRepository.save(atividade));
    }

    @Transactional(readOnly = true)
    public List<AtividadeResponse> listar(Long professorId) {
        var result = new ArrayList<AtividadeResponse>();
        atividadeRepository.findAllByProfessorIdOrderByCriadoEmDesc(professorId)
                .stream().map(AtividadeResponse::from).forEach(result::add);
        atividadeRepository.findAllByTipoOrigemAndPublicadaTrueOrderByTituloAsc(TipoOrigemAtividade.SISTEMA)
                .stream().map(AtividadeResponse::from).forEach(result::add);
        return result;
    }

    @Transactional(readOnly = true)
    public AtividadeResponse buscar(Long professorId, Long atividadeId) {
        return AtividadeResponse.from(obterVisivel(professorId, atividadeId));
    }

    @Transactional
    public AtividadeResponse atualizar(Long professorId, Long atividadeId, UpdateAtividadeRequest request) {
        var atividade = obterEditavelDoProfessor(professorId, atividadeId);
        validarSemHistorico(atividadeId);
        atividade.atualizar(request.titulo(), request.descricao(), request.anoEscolar(), request.conteudo(),
                request.dificuldade(), request.limiteTentativas());
        return AtividadeResponse.from(atividade);
    }

    @Transactional
    public void excluir(Long professorId, Long atividadeId) {
        var atividade = obterEditavelDoProfessor(professorId, atividadeId);
        validarSemHistorico(atividadeId);
        atividadeRepository.delete(atividade);
    }

    @Transactional
    public AtividadeResponse publicar(Long professorId, Long atividadeId) {
        var atividade = obterEditavelDoProfessor(professorId, atividadeId);
        validarPublicacao(atividadeId);
        atividade.publicar();
        return AtividadeResponse.from(atividade);
    }

    @Transactional(readOnly = true)
    public Atividade obterEditavelDoProfessor(Long professorId, Long atividadeId) {
        return atividadeRepository.findByIdAndProfessorId(atividadeId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."));
    }

    @Transactional(readOnly = true)
    public Atividade obterAtribuivel(Long professorId, Long atividadeId) {
        var atividade = obterVisivel(professorId, atividadeId);
        if (!atividade.isPublicada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A atividade precisa estar publicada.");
        }
        return atividade;
    }

    @Transactional(readOnly = true)
    public void validarSemHistorico(Long atividadeId) {
        if (atividadeRepository.possuiHistorico(atividadeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A atividade possui histórico de aluno e não aceita alteração destrutiva.");
        }
    }

    private Atividade obterVisivel(Long professorId, Long atividadeId) {
        return atividadeRepository.findById(atividadeId)
                .filter(a -> a.pertenceAoProfessor(professorId) || a.isSistemaPublicada())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."));
    }

    private void validarPublicacao(Long atividadeId) {
        var questoes = questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(atividadeId);
        if (questoes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A atividade precisa ter pelo menos uma questão para ser publicada.");
        }
        for (var questao : questoes) {
            if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
                var alternativas = alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(questao.getId());
                if (alternativas.size() < 2) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Questão de múltipla escolha precisa ter pelo menos duas alternativas.");
                }
                long corretas = alternativas.stream().filter(a -> a.isCorreta()).count();
                if (corretas != 1) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Questão de múltipla escolha precisa ter exatamente uma alternativa correta.");
                }
            } else if (questao.getRespostaCorreta() == null || questao.getRespostaCorreta().isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Questão numérica ou de texto curto precisa de resposta correta.");
            }
            questao.limiteEfetivo(obterLimiteAtividade(atividadeId));
        }
    }

    private int obterLimiteAtividade(Long atividadeId) {
        return atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."))
                .getLimiteTentativas();
    }
}
