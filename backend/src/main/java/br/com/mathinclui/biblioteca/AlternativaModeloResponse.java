package br.com.mathinclui.biblioteca;

import br.com.mathinclui.biblioteca.domain.AlternativaModelo;

public record AlternativaModeloResponse(Long id, String texto, boolean correta, int ordem) {
    public static AlternativaModeloResponse from(AlternativaModelo a) {
        return new AlternativaModeloResponse(a.getId(), a.getTexto(), a.isCorreta(), a.getOrdem());
    }
}
