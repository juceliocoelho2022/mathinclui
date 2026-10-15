package br.com.mathinclui.auth.api;

import br.com.mathinclui.auth.security.AuthPrincipal;

public record SessionUser(Long id, String nome, String login, String tipo, String role) {
    public static SessionUser from(AuthPrincipal principal) {
        return new SessionUser(principal.id(), principal.displayName(), principal.login(), principal.subjectType().name(), principal.role());
    }
}
