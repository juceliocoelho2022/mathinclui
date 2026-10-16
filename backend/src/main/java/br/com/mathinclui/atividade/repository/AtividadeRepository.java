package br.com.mathinclui.atividade.repository;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
    List<Atividade> findAllByProfessorIdOrderByCriadoEmDesc(Long professorId);
    List<Atividade> findAllByTipoOrigemAndPublicadaTrueOrderByTituloAsc(TipoOrigemAtividade tipoOrigem);
    Optional<Atividade> findByIdAndProfessorId(Long id, Long professorId);
}
