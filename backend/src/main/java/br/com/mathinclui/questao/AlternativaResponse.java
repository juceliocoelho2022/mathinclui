package br.com.mathinclui.questao;

import br.com.mathinclui.questao.domain.Alternativa;

public record AlternativaResponse(Long id, String texto, boolean correta, int ordem) {
    public static AlternativaResponse from(Alternativa alternativa) {
        return new AlternativaResponse(alternativa.getId(), alternativa.getTexto(), alternativa.isCorreta(), alternativa.getOrdem());
    }
}
