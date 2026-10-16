package br.com.mathinclui.questao.repository;

import br.com.mathinclui.questao.domain.Questao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {
    List<Questao> findAllByAtividadeIdOrderByOrdemAsc(Long atividadeId);
    Optional<Questao> findByIdAndAtividadeId(Long id, Long atividadeId);
    boolean existsByAtividadeId(Long atividadeId);
    long countByAtividadeId(Long atividadeId);
}
