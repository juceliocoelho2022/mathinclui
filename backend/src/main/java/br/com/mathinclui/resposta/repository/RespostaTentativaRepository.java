package br.com.mathinclui.resposta.repository;

import br.com.mathinclui.resposta.domain.RespostaTentativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RespostaTentativaRepository extends JpaRepository<RespostaTentativa, Long> {
    long countByTentativaIdAndQuestaoId(Long tentativaId, Long questaoId);
    List<RespostaTentativa> findAllByTentativaIdOrderByRespondidaEmAsc(Long tentativaId);
    List<RespostaTentativa> findAllByTentativaIdAndQuestaoIdOrderByNumeroTentativaAsc(Long tentativaId, Long questaoId);
    long countByTentativaId(Long tentativaId);
}
