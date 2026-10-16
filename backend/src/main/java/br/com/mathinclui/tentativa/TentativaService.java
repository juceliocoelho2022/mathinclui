package br.com.mathinclui.tentativa;

import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.correcao.CorrecaoService;
import br.com.mathinclui.questao.domain.Questao;
import br.com.mathinclui.questao.domain.TipoQuestao;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import br.com.mathinclui.resposta.domain.Resposta;
import br.com.mathinclui.resposta.domain.RespostaTentativa;
import br.com.mathinclui.resposta.repository.RespostaRepository;
import br.com.mathinclui.resposta.repository.RespostaTentativaRepository;
import br.com.mathinclui.tentativa.domain.Tentativa;
import br.com.mathinclui.tentativa.repository.TentativaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TentativaService {
    private final AtribuicaoRepository atribuicaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final TentativaRepository tentativaRepository;
    private final RespostaRepository respostaRepository;
    private final RespostaTentativaRepository historicoRepository;
    private final AlunoRepository alunoRepository;
    private final CorrecaoService correcaoService;
    private final Clock clock;

    public TentativaService(AtribuicaoRepository atribuicaoRepository, AtividadeRepository atividadeRepository,
                            QuestaoRepository questaoRepository, AlternativaRepository alternativaRepository,
                            TentativaRepository tentativaRepository, RespostaRepository respostaRepository,
                            RespostaTentativaRepository historicoRepository, AlunoRepository alunoRepository,
                            CorrecaoService correcaoService, Clock clock) {
        this.atribuicaoRepository = atribuicaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.tentativaRepository = tentativaRepository;
        this.respostaRepository = respostaRepository;
        this.historicoRepository = historicoRepository;
        this.alunoRepository = alunoRepository;
        this.correcaoService = correcaoService;
        this.clock = clock;
    }

    @Transactional
    public TentativaResponse iniciar(Long alunoId, Long atribuicaoId) {
        var aluno = alunoRepository.findById(alunoId).filter(a -> a.isAtivo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
        var atribuicao = atribuicaoRepository.findForUpdate(atribuicaoId)
                .filter(a -> a.destinadaAo(alunoId, aluno.getTurmaId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade atribuída não encontrada."));
        if (!atribuicao.disponivelEm(clock.instant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A atividade não está disponível neste momento.");
        }
        var atividade = atividadeRepository.findById(atribuicao.getAtividadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."));
        if (!atividade.isPublicada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A atividade não está publicada.");
        }
        var existente = tentativaRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId);
        if (existente.isPresent()) return TentativaResponse.from(existente.get());
        return TentativaResponse.from(tentativaRepository.save(new Tentativa(atribuicaoId, alunoId, clock.instant())));
    }

    @Transactional
    public RespostaQuestaoResponse responder(Long alunoId, Long tentativaId, Long questaoId,
                                             RespostaQuestaoRequest request) {
        var tentativa = tentativaRepository.findForUpdate(tentativaId, alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada."));
        if (tentativa.isFinalizada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tentativa já foi finalizada.");
        }
        var atribuicao = atribuicaoRepository.findById(tentativa.getAtribuicaoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada."));
        if (!atribuicao.disponivelEm(clock.instant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O prazo da atividade não permite novas respostas.");
        }
        var atividade = atividadeRepository.findById(atribuicao.getAtividadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."));
        var questao = questaoRepository.findByIdAndAtividadeId(questaoId, atividade.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
        int limite = questao.limiteEfetivo(atividade.getLimiteTentativas());

        var consolidada = respostaRepository.findByTentativaIdAndQuestaoId(tentativaId, questaoId);
        long tentativasAnteriores = historicoRepository.countByTentativaIdAndQuestaoId(tentativaId, questaoId);
        if (consolidada.filter(Resposta::isCorreta).isPresent() || tentativasAnteriores >= limite) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A questão já está encerrada.");
        }

        int numeroTentativa = Math.toIntExact(tentativasAnteriores + 1);
        var correcao = correcaoService.corrigir(questao, request.resposta());
        boolean encerrou = correcao.correta() || numeroTentativa >= limite;
        short nivelDica = correcao.correta() ? 0 : dicaLiberada(questao, numeroTentativa, encerrou);
        var agora = clock.instant();

        historicoRepository.save(new RespostaTentativa(tentativaId, questaoId, numeroTentativa,
                correcao.respostaNormalizada(), correcao.correta(), nivelDica, request.tempoSegundos(), agora));

        var resposta = consolidada.orElseGet(() -> new Resposta(tentativaId, questaoId,
                correcao.respostaNormalizada(), correcao.correta(), nivelDica, request.tempoSegundos(), agora));
        if (consolidada.isPresent()) {
            short totalDicas = (short) Math.max(resposta.getQuantidadeDicas(), nivelDica);
            resposta.atualizar(correcao.respostaNormalizada(), correcao.correta(), totalDicas,
                    resposta.getTempoSegundos() + request.tempoSegundos(), agora);
        }
        respostaRepository.save(resposta);

        boolean atividadeFinalizada = encerrou && finalizarSeCompleta(tentativa, atividade);
        String dica = !encerrou && !correcao.correta() ? dicaTexto(questao, nivelDica) : null;
        String explicacao = encerrou ? questao.getExplicacao() : null;
        String respostaCorreta = encerrou ? respostaCorreta(questao) : null;
        return new RespostaQuestaoResponse(correcao.correta(), encerrou, numeroTentativa, limite,
                nivelDica, dica, explicacao, respostaCorreta, atividadeFinalizada);
    }

    @Transactional(readOnly = true)
    public TentativaResultadoResponse resultado(Long alunoId, Long tentativaId) {
        var tentativa = tentativaRepository.findByIdAndAlunoId(tentativaId, alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada."));
        return TentativaResultadoResponse.from(tentativa);
    }

    private boolean finalizarSeCompleta(Tentativa tentativa, Atividade atividade) {
        var questoes = questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(atividade.getId());
        if (questoes.isEmpty()) return false;
        var respostas = respostaRepository.findAllByTentativaId(tentativa.getId()).stream()
                .collect(Collectors.toMap(Resposta::getQuestaoId, Function.identity()));
        int acertos = 0;
        int erros = 0;
        int tempo = 0;
        for (var questao : questoes) {
            var resposta = respostas.get(questao.getId());
            if (resposta == null) return false;
            tempo += resposta.getTempoSegundos();
            if (resposta.isCorreta()) {
                acertos++;
                continue;
            }
            int limite = questao.limiteEfetivo(atividade.getLimiteTentativas());
            long envios = historicoRepository.countByTentativaIdAndQuestaoId(tentativa.getId(), questao.getId());
            if (envios < limite) return false;
            erros++;
        }
        var percentual = BigDecimal.valueOf(acertos)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(questoes.size()), 2, RoundingMode.HALF_UP);
        tentativa.finalizar(acertos, erros, percentual, tempo, clock.instant());
        return true;
    }

    private short dicaLiberada(Questao questao, int numeroTentativa, boolean encerrou) {
        if (encerrou) return (short) Math.min(3, Math.max(0, numeroTentativa - 1));
        int nivel = Math.min(3, numeroTentativa);
        while (nivel > 0 && dicaTexto(questao, (short) nivel) == null) nivel--;
        return (short) nivel;
    }

    private String dicaTexto(Questao questao, short nivel) {
        return switch (nivel) {
            case 1 -> questao.getDica1();
            case 2 -> questao.getDica2();
            case 3 -> questao.getDica3();
            default -> null;
        };
    }

    private String respostaCorreta(Questao questao) {
        if (questao.getTipo() != TipoQuestao.MULTIPLA_ESCOLHA) return questao.getRespostaCorreta();
        return alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(questao.getId()).stream()
                .filter(a -> a.isCorreta())
                .findFirst()
                .map(a -> Long.toString(a.getId()))
                .orElse(null);
    }
}
