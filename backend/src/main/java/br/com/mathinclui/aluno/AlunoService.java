package br.com.mathinclui.aluno;

import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.perfilpedagogico.PerfilPedagogicoService;
import br.com.mathinclui.turma.TurmaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AlunoService {
    private final AlunoRepository alunoRepository;
    private final TurmaService turmaService;
    private final PerfilPedagogicoService perfilPedagogicoService;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(AlunoRepository alunoRepository, TurmaService turmaService,
                        PerfilPedagogicoService perfilPedagogicoService, PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.turmaService = turmaService;
        this.perfilPedagogicoService = perfilPedagogicoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AlunoResponse criar(Long professorId, Long turmaId, CreateAlunoRequest request) {
        turmaService.obterDoProfessor(professorId, turmaId);
        if (alunoRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse nome de usuário já está em uso.");
        }
        var aluno = alunoRepository.save(new Aluno(
                turmaId,
                request.nome(),
                request.username(),
                passwordEncoder.encode(request.pin())
        ));
        perfilPedagogicoService.criarPadrao(aluno.getId());
        return AlunoResponse.from(aluno);
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar(Long professorId, Long turmaId) {
        turmaService.obterDoProfessor(professorId, turmaId);
        return alunoRepository.findAllByTurmaIdAndAtivoTrueOrderByNomeAsc(turmaId)
                .stream().map(AlunoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(Long professorId, Long alunoId) {
        return AlunoResponse.from(obterDoProfessor(professorId, alunoId));
    }

    @Transactional
    public AlunoResponse atualizar(Long professorId, Long alunoId, UpdateAlunoRequest request) {
        var aluno = obterDoProfessor(professorId, alunoId);
        if (alunoRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), alunoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse nome de usuário já está em uso.");
        }
        aluno.atualizar(request.nome(), request.username());
        return AlunoResponse.from(aluno);
    }

    @Transactional
    public void redefinirPin(Long professorId, Long alunoId, UpdatePinRequest request) {
        var aluno = obterDoProfessor(professorId, alunoId);
        aluno.alterarPinHash(passwordEncoder.encode(request.pin()));
    }

    @Transactional(readOnly = true)
    public Aluno obterDoProfessor(Long professorId, Long alunoId) {
        var aluno = alunoRepository.findById(alunoId)
                .filter(Aluno::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
        turmaService.obterDoProfessor(professorId, aluno.getTurmaId());
        return aluno;
    }
}
