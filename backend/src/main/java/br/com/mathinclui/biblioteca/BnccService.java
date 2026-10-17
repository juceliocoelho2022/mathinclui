package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BnccService {
    private final HabilidadeBnccRepository repository;

    public BnccService(HabilidadeBnccRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<HabilidadeBnccResponse> listar(short anoEscolar) {
        if (anoEscolar < 6 || anoEscolar > 8) {
            throw new IllegalArgumentException("Ano escolar deve estar entre 6 e 8.");
        }
        return repository.findAllByAnoEscolarOrderByCodigoAsc(anoEscolar).stream()
                .map(HabilidadeBnccResponse::from)
                .toList();
    }
}
