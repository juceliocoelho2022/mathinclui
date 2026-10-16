package br.com.mathinclui.biblioteca.repository;

import br.com.mathinclui.biblioteca.domain.QuestaoModelo;
import br.com.mathinclui.biblioteca.domain.TipoOrigemBiblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuestaoModeloRepository extends JpaRepository<QuestaoModelo, Long> {
    List<QuestaoModelo> findAllByTipoOrigemOrderByCriadoEmDesc(TipoOrigemBiblioteca origem);
    List<QuestaoModelo> findAllByProfessorIdOrderByCriadoEmDesc(Long professorId);
    Optional<QuestaoModelo> findByIdAndProfessorId(Long id, Long professorId);
}
