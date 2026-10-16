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
    private final AlunoRepository alunoRepository;
    private final PreferenciaInterfaceAlunoRepository repository;

    public PreferenciaInterfaceService(AlunoRepository alunoRepository, PreferenciaInterfaceAlunoRepository repository) {
        this.alunoRepository = alunoRepository;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PreferenciaInterfaceResponse buscar(Long alunoId) {
        validarAluno(alunoId);
        return repository.findById(alunoId).map(PreferenciaInterfaceResponse::from)
                .orElseGet(PreferenciaInterfaceResponse::padrao);
    }

    @Transactional
    public PreferenciaInterfaceResponse atualizar(Long alunoId, PreferenciaInterfaceRequest request) {
        validarAluno(alunoId);
        var pref = repository.findById(alunoId).orElseGet(() -> new PreferenciaInterfaceAluno(alunoId));
        pref.atualizar(request.tamanhoFonte(), request.leituraVoz(), request.apoioVisual());
        return PreferenciaInterfaceResponse.from(repository.save(pref));
    }

    private Aluno validarAluno(Long alunoId) {
        return alunoRepository.findById(alunoId).filter(Aluno::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
    }
}
