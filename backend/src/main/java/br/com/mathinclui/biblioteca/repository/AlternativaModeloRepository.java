package br.com.mathinclui.biblioteca.repository;

import br.com.mathinclui.biblioteca.domain.AlternativaModelo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlternativaModeloRepository extends JpaRepository<AlternativaModelo, Long> {
    List<AlternativaModelo> findAllByQuestaoModeloIdOrderByOrdemAsc(Long questaoModeloId);
    void deleteAllByQuestaoModeloId(Long questaoModeloId);
}
