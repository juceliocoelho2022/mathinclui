package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.PreferenciaInterfaceAluno;
import br.com.mathinclui.acessibilidade.repository.PreferenciaInterfaceAlunoRepository;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PreferenciaInterfaceService {
    private final PreferenciaInterfaceAlunoRepository repository;
    private final AlunoRepository alunoRepository;

    public PreferenciaInterfaceService(PreferenciaInterfaceAlunoRepository repository, AlunoRepository alunoRepository) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional(readOnly = true)
    public PreferenciaInterfaceResponse buscar(Long alunoId) {
        validarAluno(alunoId);
        return repository.findByAlunoId(alunoId)
                .map(PreferenciaInterfaceResponse::from)
                .orElseGet(() -> PreferenciaInterfaceResponse.padrao(alunoId));
    }

    @Transactional
    public PreferenciaInterfaceResponse atualizar(Long alunoId, PreferenciaInterfaceRequest request) {
        validarAluno(alunoId);
        var existente = repository.findByAlunoId(alunoId);
        var preferencia = existente.orElseGet(() -> new PreferenciaInterfaceAluno(alunoId));
        preferencia.atualizar(request.tamanhoFonte(), request.leituraVoz(), request.apoioVisual());
        if (existente.isEmpty()) preferencia = repository.save(preferencia);
        return PreferenciaInterfaceResponse.from(preferencia);
    }

    private Aluno validarAluno(Long alunoId) {
        return alunoRepository.findById(alunoId)
                .filter(Aluno::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
    }
}
