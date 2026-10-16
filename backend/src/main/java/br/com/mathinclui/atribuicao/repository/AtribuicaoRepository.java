package br.com.mathinclui.atribuicao.repository;

import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.domain.StatusAtribuicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AtribuicaoRepository extends JpaRepository<Atribuicao, Long> {
    List<Atribuicao> findAllByProfessorIdOrderByCriadoEmDesc(Long professorId);
    Optional<Atribuicao> findByIdAndProfessorId(Long id, Long professorId);

    @Query("select a from Atribuicao a where a.status = :status and (a.alunoId = :alunoId or a.turmaId = :turmaId) and a.dataInicio <= :agora and (a.dataLimite is null or a.dataLimite >= :agora) order by a.criadoEm desc")
    List<Atribuicao> findDisponiveisParaAluno(@Param("alunoId") Long alunoId,
                                              @Param("turmaId") Long turmaId,
                                              @Param("status") StatusAtribuicao status,
                                              @Param("agora") Instant agora);

    @Query("select a from Atribuicao a where a.status <> br.com.mathinclui.atribuicao.domain.StatusAtribuicao.CANCELADA and (a.alunoId = :alunoId or a.turmaId = :turmaId) order by a.criadoEm desc")
    List<Atribuicao> findTodasParaAluno(@Param("alunoId") Long alunoId, @Param("turmaId") Long turmaId);
}
