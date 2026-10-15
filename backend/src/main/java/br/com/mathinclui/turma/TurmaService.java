package br.com.mathinclui.turma;

import br.com.mathinclui.turma.domain.Turma;
import br.com.mathinclui.turma.repository.TurmaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TurmaService {
    private final TurmaRepository turmaRepository;

    public TurmaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    @Transactional
    public TurmaResponse criar(Long professorId, CreateTurmaRequest request) {
        if (turmaRepository.existsByProfessorIdAndCodigoIgnoreCase(professorId, request.codigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma turma com esse código.");
        }
        var turma = turmaRepository.save(new Turma(professorId, request.nome(), request.anoEscolar(), request.codigo()));
        return TurmaResponse.from(turma);
    }

    @Transactional(readOnly = true)
    public List<TurmaResponse> listar(Long professorId) {
        return turmaRepository.findAllByProfessorIdAndAtivaTrueOrderByNomeAsc(professorId)
                .stream().map(TurmaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TurmaResponse buscar(Long professorId, Long turmaId) {
        return TurmaResponse.from(obterDoProfessor(professorId, turmaId));
    }

    @Transactional
    public TurmaResponse atualizar(Long professorId, Long turmaId, UpdateTurmaRequest request) {
        var turma = obterDoProfessor(professorId, turmaId);
        if (turmaRepository.existsByProfessorIdAndCodigoIgnoreCaseAndIdNot(professorId, request.codigo(), turmaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma turma com esse código.");
        }
        turma.atualizar(request.nome(), request.anoEscolar(), request.codigo());
        return TurmaResponse.from(turma);
    }

    @Transactional
    public void desativar(Long professorId, Long turmaId) {
        var turma = obterDoProfessor(professorId, turmaId);
        turma.desativar();
    }

    @Transactional(readOnly = true)
    public Turma obterDoProfessor(Long professorId, Long turmaId) {
        return turmaRepository.findByIdAndProfessorId(turmaId, professorId)
                .filter(Turma::isAtiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));
    }
}
