package com.psicomanager.api.infra.tenant;

import com.psicomanager.api.user.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro que resolve o tenant ativo a partir do usuário autenticado
 * e popula o {@link TenantContext} para uso durante toda a requisição.
 *
 * <p>O tenant é derivado de {@code user.getActiveOrganizationId()}, que é
 * atualizado quando o usuário troca de organização via
 * {@code PATCH /organizations/switch}.</p>
 *
 * <p>Rotas públicas (auth, onboarding) são ignoradas — não exigem tenant.</p>
 *
 * <p>Executado após o {@code FilterSecurity} (que autentica o JWT), por isso
 * o {@code SecurityContextHolder} já está populado quando este filtro roda.</p>
 */
@Component
@Order(2)
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    /** Rotas que não exigem tenant ativo. */
    private static final Set<String> TENANT_FREE_PREFIXES = Set.of(
            "/auth/",
            "/organizations/create",
            "/organizations/join",
            "/organizations/my"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            boolean isTenantFree = TENANT_FREE_PREFIXES.stream().anyMatch(path::startsWith);

            if (!isTenantFree) {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof User user) {
                    String orgId = user.getActiveOrganizationId();
                    if (orgId != null) {
                        TenantContext.set(orgId);
                        log.debug("TenantContext definido: organizationId={} para path={}", orgId, path);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Sempre limpa — evita vazamento de contexto no pool de threads
            TenantContext.clear();
        }
    }
}
