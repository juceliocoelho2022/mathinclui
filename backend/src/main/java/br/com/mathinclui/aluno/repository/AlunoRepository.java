package br.com.mathinclui.aluno.repository;

import br.com.mathinclui.aluno.domain.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    Optional<Aluno> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    List<Aluno> findAllByTurmaIdAndAtivoTrueOrderByNomeAsc(Long turmaId);
}
