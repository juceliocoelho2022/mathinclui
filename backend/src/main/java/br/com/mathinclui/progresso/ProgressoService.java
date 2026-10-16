package br.com.mathinclui.progresso;

import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.atribuicao.AtribuicaoService;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import br.com.mathinclui.resposta.repository.RespostaRepository;
import br.com.mathinclui.resposta.repository.RespostaTentativaRepository;
import br.com.mathinclui.tentativa.domain.StatusTentativa;
import br.com.mathinclui.tentativa.domain.Tentativa;
import br.com.mathinclui.tentativa.repository.TentativaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProgressoService {
    private final AtribuicaoService atribuicaoService;
    private final AtividadeRepository atividadeRepository;
    private final QuestaoRepository questaoRepository;
    private final TentativaRepository tentativaRepository;
    private final RespostaRepository respostaRepository;
    private final RespostaTentativaRepository historicoRepository;
    private final AlunoRepository alunoRepository;

    public ProgressoService(AtribuicaoService atribuicaoService, AtividadeRepository atividadeRepository,
                            QuestaoRepository questaoRepository, TentativaRepository tentativaRepository,
                            RespostaRepository respostaRepository, RespostaTentativaRepository historicoRepository,
                            AlunoRepository alunoRepository) {
        this.atribuicaoService = atribuicaoService;
        this.atividadeRepository = atividadeRepository;
        this.questaoRepository = questaoRepository;
        this.tentativaRepository = tentativaRepository;
        this.respostaRepository = respostaRepository;
        this.historicoRepository = historicoRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional(readOnly = true)
    public AtividadeResultadosResponse resultados(Long professorId, Long atribuicaoId) {
        var atribuicao = atribuicaoService.obterDoProfessor(professorId, atribuicaoId);
        var atividade = atividadeRepository.findById(atribuicao.getAtividadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."));
        int totalAlunos = totalAlunos(atribuicao.getAlunoId(), atribuicao.getTurmaId());
        var tentativas = tentativaRepository.findAllByAtribuicaoId(atribuicaoId);
        int participantes = tentativas.size();
        int concluidos = (int) tentativas.stream().filter(Tentativa::isFinalizada).count();
        var participacao = percentual(participantes, totalAlunos);
        var media = mediaPercentual(tentativas);

        var alunos = tentativas.stream().map(t -> alunoResultado(t)).toList();
        var questoes = questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(atividade.getId()).stream()
                .map(q -> {
                    int acertos = 0;
                    int erros = 0;
                    long submissoes = 0;
                    for (var tentativa : tentativas) {
                        var resposta = respostaRepository.findByTentativaIdAndQuestaoId(tentativa.getId(), q.getId()).orElse(null);
                        long envios = historicoRepository.countByTentativaIdAndQuestaoId(tentativa.getId(), q.getId());
                        submissoes += envios;
                        if (resposta == null) continue;
                        if (resposta.isCorreta()) {
                            acertos++;
                        } else if (tentativa.isFinalizada() || envios >= q.limiteEfetivo(atividade.getLimiteTentativas())) {
                            erros++;
                        }
                    }
                    return new QuestaoResultadoResponse(q.getId(), q.getEnunciado(), acertos, erros, submissoes);
                }).toList();

        return new AtividadeResultadosResponse(atribuicaoId, atividade.getId(), atividade.getTitulo(), totalAlunos,
                participantes, concluidos, participacao, media, alunos, questoes);
    }

    private AlunoResultadoResponse alunoResultado(Tentativa tentativa) {
        var aluno = alunoRepository.findById(tentativa.getAlunoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno do resultado não encontrado."));
        var respostas = respostaRepository.findAllByTentativaId(tentativa.getId());
        int dicas = respostas.stream().mapToInt(r -> r.getQuantidadeDicas()).sum();
        long submissoes = historicoRepository.countByTentativaId(tentativa.getId());
        return new AlunoResultadoResponse(aluno.getId(), aluno.getNome(), tentativa.getStatus(),
                tentativa.getQuantidadeAcertos(), tentativa.getQuantidadeErros(), tentativa.getPercentual(),
                tentativa.getTempoTotalSegundos(), submissoes, dicas);
    }

    private int totalAlunos(Long alunoId, Long turmaId) {
        if (alunoId != null) {
            return alunoRepository.findById(alunoId).filter(Aluno::isAtivo).isPresent() ? 1 : 0;
        }
        return alunoRepository.findAllByTurmaIdAndAtivoTrueOrderByNomeAsc(turmaId).size();
    }

    private BigDecimal percentual(int valor, int total) {
        if (total == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(valor).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal mediaPercentual(List<Tentativa> tentativas) {
        var percentuais = tentativas.stream().filter(Tentativa::isFinalizada)
                .map(Tentativa::getPercentual).filter(p -> p != null).toList();
        if (percentuais.isEmpty()) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        var soma = percentuais.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(percentuais.size()), 2, RoundingMode.HALF_UP);
    }
}
