package br.com.mathinclui.tentativa;

import br.com.mathinclui.questao.domain.Alternativa;

public record AlunoAlternativaResponse(Long id, String texto, int ordem) {
    public static AlunoAlternativaResponse from(Alternativa alternativa) {
        return new AlunoAlternativaResponse(alternativa.getId(), alternativa.getTexto(), alternativa.getOrdem());
    }
}
