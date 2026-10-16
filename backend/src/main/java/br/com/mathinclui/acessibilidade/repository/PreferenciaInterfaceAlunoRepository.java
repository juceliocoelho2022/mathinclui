package br.com.mathinclui.acessibilidade.repository;

import br.com.mathinclui.acessibilidade.domain.PreferenciaInterfaceAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciaInterfaceAlunoRepository extends JpaRepository<PreferenciaInterfaceAluno, Long> {
    Optional<PreferenciaInterfaceAluno> findByAlunoId(Long alunoId);
}
