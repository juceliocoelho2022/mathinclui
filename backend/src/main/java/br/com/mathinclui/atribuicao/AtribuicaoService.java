package br.com.mathinclui.atribuicao;

import br.com.mathinclui.aluno.AlunoService;
import br.com.mathinclui.atividade.AtividadeService;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.turma.TurmaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class AtribuicaoService {
    private final AtribuicaoRepository repository;
    private final AtividadeService atividadeService;
    private final TurmaService turmaService;
    private final AlunoService alunoService;
    private final Clock clock;

    public AtribuicaoService(AtribuicaoRepository repository, AtividadeService atividadeService,
                             TurmaService turmaService, AlunoService alunoService, Clock clock) {
        this.repository = repository;
        this.atividadeService = atividadeService;
        this.turmaService = turmaService;
        this.alunoService = alunoService;
        this.clock = clock;
    }

    @Transactional
    public AtribuicaoResponse criar(Long professorId, CreateAtribuicaoRequest request) {
        if ((request.turmaId() == null) == (request.alunoId() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe exatamente uma turma ou um aluno.");
        }
        atividadeService.obterAtribuivel(professorId, request.atividadeId());
        if (request.turmaId() != null) turmaService.obterDoProfessor(professorId, request.turmaId());
        if (request.alunoId() != null) alunoService.obterDoProfessor(professorId, request.alunoId());
        Instant inicio = request.dataInicio() == null ? clock.instant() : request.dataInicio();
        var atribuicao = new Atribuicao(request.atividadeId(), professorId, request.turmaId(), request.alunoId(),
                inicio, request.dataLimite());
        return AtribuicaoResponse.from(repository.save(atribuicao));
    }

    @Transactional(readOnly = true)
    public List<AtribuicaoResponse> listar(Long professorId) {
        return repository.findAllByProfessorIdOrderByCriadoEmDesc(professorId).stream()
                .map(AtribuicaoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Atribuicao obterDoProfessor(Long professorId, Long atribuicaoId) {
        return repository.findByIdAndProfessorId(atribuicaoId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada."));
    }
}
