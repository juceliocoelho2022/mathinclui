package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.repository.HabilidadeBnccRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BnccService {
    private final HabilidadeBnccRepository repository;
    public BnccService(HabilidadeBnccRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<HabilidadeBnccResponse> listar(Short ano) {
        var items = ano == null ? repository.findAll() : repository.findAllByAnoEscolarOrderByCodigoAsc(ano);
        return items.stream().map(HabilidadeBnccResponse::from).toList();
    }
}
