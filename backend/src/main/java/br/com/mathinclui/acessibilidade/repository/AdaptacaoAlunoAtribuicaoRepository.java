package br.com.mathinclui.acessibilidade.repository;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAlunoAtribuicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdaptacaoAlunoAtribuicaoRepository extends JpaRepository<AdaptacaoAlunoAtribuicao, Long> {
    Optional<AdaptacaoAlunoAtribuicao> findByAtribuicaoIdAndAlunoId(Long atribuicaoId, Long alunoId);
    void deleteByAtribuicaoIdAndAlunoId(Long atribuicaoId, Long alunoId);
}
