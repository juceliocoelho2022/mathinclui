package br.com.mathinclui.biblioteca.repository;

import br.com.mathinclui.biblioteca.domain.HabilidadeBncc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HabilidadeBnccRepository extends JpaRepository<HabilidadeBncc, Long> {
    Optional<HabilidadeBncc> findByCodigo(String codigo);
    List<HabilidadeBncc> findAllByAnoEscolarOrderByCodigoAsc(short anoEscolar);
}
