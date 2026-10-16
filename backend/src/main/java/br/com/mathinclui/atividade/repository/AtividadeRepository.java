package br.com.mathinclui.atividade.repository;

import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.domain.TipoOrigemAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
    List<Atividade> findAllByProfessorIdOrderByCriadoEmDesc(Long professorId);
    List<Atividade> findAllByTipoOrigemAndPublicadaTrueOrderByTituloAsc(TipoOrigemAtividade tipoOrigem);
    Optional<Atividade> findByIdAndProfessorId(Long id, Long professorId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tentativas t JOIN atribuicoes a ON a.id = t.atribuicao_id WHERE a.atividade_id = :atividadeId)", nativeQuery = true)
    boolean possuiHistorico(@Param("atividadeId") Long atividadeId);
}
