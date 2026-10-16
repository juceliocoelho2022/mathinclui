package br.com.mathinclui.tentativa.repository;

import br.com.mathinclui.tentativa.domain.Tentativa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TentativaRepository extends JpaRepository<Tentativa, Long> {
    Optional<Tentativa> findByAtribuicaoIdAndAlunoId(Long atribuicaoId, Long alunoId);
    Optional<Tentativa> findByIdAndAlunoId(Long id, Long alunoId);
    List<Tentativa> findAllByAtribuicaoId(Long atribuicaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tentativa t where t.id = :id and t.alunoId = :alunoId")
    Optional<Tentativa> findForUpdate(@Param("id") Long id, @Param("alunoId") Long alunoId);
}
