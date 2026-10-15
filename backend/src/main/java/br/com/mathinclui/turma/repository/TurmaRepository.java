package br.com.mathinclui.turma.repository;

import br.com.mathinclui.turma.domain.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findAllByProfessorIdAndAtivaTrueOrderByNomeAsc(Long professorId);
    Optional<Turma> findByIdAndProfessorId(Long id, Long professorId);
    boolean existsByProfessorIdAndCodigoIgnoreCase(Long professorId, String codigo);
    boolean existsByProfessorIdAndCodigoIgnoreCaseAndIdNot(Long professorId, String codigo, Long id);
}
