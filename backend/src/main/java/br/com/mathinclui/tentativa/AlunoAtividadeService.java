package br.com.mathinclui.tentativa;

import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.perfilpedagogico.PerfilPedagogicoResponse;
import br.com.mathinclui.perfilpedagogico.repository.PerfilPedagogicoRepository;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import br.com.mathinclui.resposta.domain.Resposta;
import br.com.mathinclui.resposta.repository.RespostaRepository;
import br.com.mathinclui.resposta.repository.RespostaTentativaRepository;
import br.com.mathinclui.tentativa.domain.StatusTentativa;
import br.com.mathinclui.tentativa.domain.Tentativa;
import br.com.mathinclui.tentativa.repository.TentativaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;

@Service
public class AlunoAtividadeService {
    private final AlunoRepository alunoRepository;
    private final AtribuicaoRepository atribuicaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final TentativaRepository tentativaRepository;
    private final RespostaRepository respostaRepository;
    private final RespostaTentativaRepository historicoRepository;
    private final PerfilPedagogicoRepository perfilRepository;
    private final Clock clock;

    public AlunoAtividadeService(AlunoRepository alunoRepository, AtribuicaoRepository atribuicaoRepository,
                                 AtividadeRepository atividadeRepository, QuestaoRepository questaoRepository,
                                 AlternativaRepository alternativaRepository, TentativaRepository tentativaRepository,
                                 RespostaRepository respostaRepository, RespostaTentativaRepository historicoRepository,
                                 PerfilPedagogicoRepository perfilRepository, Clock clock) {
        this.alunoRepository = alunoRepository;
        this.atribuicaoRepository = atribuicaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.tentativaRepository = tentativaRepository;
        this.respostaRepository = respostaRepository;
        this.historicoRepository = historicoRepository;
        this.perfilRepository = perfilRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AlunoAtividadeResumoResponse> listar(Long alunoId) {
        var aluno = obterAluno(alunoId);
        return atribuicaoRepository.findTodasParaAluno(alunoId, aluno.getTurmaId()).stream()
                .map(a -> resumo(a, atividade(a.getAtividadeId()), tentativaRepository.findByAtribuicaoIdAndAlunoId(a.getId(), alunoId).orElse(null)))
                .filter(r -> !"EXPIRADA".equals(r.estado()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AlunoAtividadeDetalheResponse detalhar(Long alunoId, Long atribuicaoId) {
        var aluno = obterAluno(alunoId);
        var atribuicao = obterAtribuicaoDoAluno(aluno, atribuicaoId);
        var atividade = atividade(atribuicao.getAtividadeId());
        var tentativa = tentativaRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId).orElse(null);
        var perfil = perfilRepository.findByAlunoId(alunoId).map(PerfilPedagogicoResponse::from).orElse(null);
        var questoes = questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(atividade.getId()).stream()
                .map(q -> {
                    var resposta = tentativa == null ? null : respostaRepository
                            .findByTentativaIdAndQuestaoId(tentativa.getId(), q.getId()).orElse(null);
                    long usadas = tentativa == null ? 0 : historicoRepository.countByTentativaIdAndQuestaoId(tentativa.getId(), q.getId());
                    int limite = q.limiteEfetivo(atividade.getLimiteTentativas());
                    boolean encerrada = resposta != null && (resposta.isCorreta() || usadas >= limite);
                    int dicas = resposta == null ? 0 : resposta.getQuantidadeDicas();
                    String dicaAtual = encerrada ? null : dica(q.getDica1(), q.getDica2(), q.getDica3(), dicas);
                    var alternativas = alternativaRepository.findAllByQuestaoIdOrderByOrdemAsc(q.getId()).stream()
                            .map(AlunoAlternativaResponse::from).toList();
                    return new AlunoQuestaoResponse(q.getId(), q.getEnunciado(), q.getTipo(), q.getImagemUrl(), q.getOrdem(),
                            Math.toIntExact(usadas), limite, dicas, dicaAtual, encerrada, alternativas);
                }).toList();
        var estado = estado(atribuicao, tentativa);
        return new AlunoAtividadeDetalheResponse(atribuicaoId, atividade.getId(), atividade.getTitulo(), atividade.getDescricao(),
                atividade.getAnoEscolar(), atividade.getConteudo(), estado, tentativa == null ? null : tentativa.getId(), perfil, questoes);
    }

    private AlunoAtividadeResumoResponse resumo(Atribuicao atribuicao, Atividade atividade, Tentativa tentativa) {
        return new AlunoAtividadeResumoResponse(atribuicao.getId(), atividade.getId(), atividade.getTitulo(),
                atividade.getAnoEscolar(), atividade.getConteudo(), estado(atribuicao, tentativa), atribuicao.getDataInicio(),
                atribuicao.getDataLimite(), tentativa == null ? null : tentativa.getId());
    }

    private String estado(Atribuicao atribuicao, Tentativa tentativa) {
        if (tentativa != null && tentativa.getStatus() == StatusTentativa.FINALIZADA) return "CONCLUIDA";
        if (tentativa != null) return "EM_ANDAMENTO";
        if (atribuicao.getDataLimite() != null && atribuicao.getDataLimite().isBefore(clock.instant())) return "EXPIRADA";
        return "PARA_FAZER";
    }

    private Aluno obterAluno(Long alunoId) {
        return alunoRepository.findById(alunoId).filter(Aluno::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
    }

    private Atribuicao obterAtribuicaoDoAluno(Aluno aluno, Long atribuicaoId) {
        return atribuicaoRepository.findById(atribuicaoId)
                .filter(a -> a.destinadaAo(aluno.getId(), aluno.getTurmaId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade atribuída não encontrada."));
    }

    private Atividade atividade(Long atividadeId) {
        return atividadeRepository.findById(atividadeId).filter(Atividade::isPublicada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."));
    }

    private String dica(String dica1, String dica2, String dica3, int nivel) {
        return switch (nivel) {
            case 1 -> dica1;
            case 2 -> dica2;
            case 3 -> dica3;
            default -> null;
        };
    }
}
