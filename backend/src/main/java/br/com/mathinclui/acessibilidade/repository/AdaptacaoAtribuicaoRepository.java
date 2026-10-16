package br.com.mathinclui.acessibilidade.repository;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAtribuicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdaptacaoAtribuicaoRepository extends JpaRepository<AdaptacaoAtribuicao, Long> {
    Optional<AdaptacaoAtribuicao> findByAtribuicaoId(Long atribuicaoId);
}
