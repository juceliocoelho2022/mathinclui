package br.com.mathinclui.perfilpedagogico.repository;

import br.com.mathinclui.perfilpedagogico.domain.PerfilPedagogico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilPedagogicoRepository extends JpaRepository<PerfilPedagogico, Long> {
    Optional<PerfilPedagogico> findByAlunoId(Long alunoId);
}
