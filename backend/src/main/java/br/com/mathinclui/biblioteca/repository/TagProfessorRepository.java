package br.com.mathinclui.biblioteca.repository;

import br.com.mathinclui.biblioteca.domain.TagProfessor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagProfessorRepository extends JpaRepository<TagProfessor, Long> {
    List<TagProfessor> findAllByProfessorIdOrderByNomeAsc(Long professorId);
    Optional<TagProfessor> findByIdAndProfessorId(Long id, Long professorId);
    boolean existsByProfessorIdAndNomeNormalizado(Long professorId, String nomeNormalizado);
}
