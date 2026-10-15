package br.com.mathinclui.professor.repository;
import br.com.mathinclui.professor.domain.Professor; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface ProfessorRepository extends JpaRepository<Professor,Long>{Optional<Professor> findByEmailIgnoreCase(String email); boolean existsByEmailIgnoreCase(String email);}
