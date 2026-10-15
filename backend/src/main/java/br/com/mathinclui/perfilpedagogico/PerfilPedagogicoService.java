package br.com.mathinclui.perfilpedagogico;

import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.perfilpedagogico.domain.PerfilPedagogico;
import br.com.mathinclui.perfilpedagogico.repository.PerfilPedagogicoRepository;
import br.com.mathinclui.turma.TurmaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilPedagogicoService {
    private final PerfilPedagogicoRepository perfilRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaService turmaService;

    public PerfilPedagogicoService(PerfilPedagogicoRepository perfilRepository,
                                   AlunoRepository alunoRepository,
                                   TurmaService turmaService) {
        this.perfilRepository = perfilRepository;
        this.alunoRepository = alunoRepository;
        this.turmaService = turmaService;
    }

    @Transactional
    public PerfilPedagogicoResponse criarPadrao(Long alunoId) {
        return PerfilPedagogicoResponse.from(
                perfilRepository.findByAlunoId(alunoId)
                        .orElseGet(() -> perfilRepository.save(new PerfilPedagogico(alunoId)))
        );
    }

    @Transactional(readOnly = true)
    public PerfilPedagogicoResponse buscar(Long professorId, Long alunoId) {
        validarOwnership(professorId, alunoId);
        var perfil = perfilRepository.findByAlunoId(alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil pedagógico não encontrado."));
        return PerfilPedagogicoResponse.from(perfil);
    }

    @Transactional
    public PerfilPedagogicoResponse atualizar(Long professorId, Long alunoId, PerfilPedagogicoRequest request) {
        validarOwnership(professorId, alunoId);
        var perfil = perfilRepository.findByAlunoId(alunoId)
                .orElseGet(() -> perfilRepository.save(new PerfilPedagogico(alunoId)));
        perfil.atualizar(
                request.tamanhoFonte(), request.leituraVoz(), request.apoioVisual(), request.dicasPassoAPasso(),
                request.repeticaoGuiada(), request.questoesPorTela(), request.tempoAmpliado(), request.nivelApoio()
        );
        return PerfilPedagogicoResponse.from(perfil);
    }

    private void validarOwnership(Long professorId, Long alunoId) {
        var aluno = alunoRepository.findById(alunoId)
                .filter(a -> a.isAtivo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
        turmaService.obterDoProfessor(professorId, aluno.getTurmaId());
    }
}
