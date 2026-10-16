package br.com.mathinclui.questao.repository;

import br.com.mathinclui.questao.domain.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {
    List<Alternativa> findAllByQuestaoIdOrderByOrdemAsc(Long questaoId);
    Optional<Alternativa> findByIdAndQuestaoId(Long id, Long questaoId);
    long countByQuestaoId(Long questaoId);
    long countByQuestaoIdAndCorretaTrue(Long questaoId);
    void deleteAllByQuestaoId(Long questaoId);
}
