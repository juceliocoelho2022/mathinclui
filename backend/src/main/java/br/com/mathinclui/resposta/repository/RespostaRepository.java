package br.com.mathinclui.resposta.repository;

import br.com.mathinclui.resposta.domain.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    Optional<Resposta> findByTentativaIdAndQuestaoId(Long tentativaId, Long questaoId);
    List<Resposta> findAllByTentativaId(Long tentativaId);
}
