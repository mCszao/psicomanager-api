package com.psicomanager.api.infra.tenant;

/**
 * Armazena o {@code organization_id} do tenant ativo para a requisição atual.
 *
 * <p>Usa {@link ThreadLocal} para garantir que cada thread (requisição) tenha
 * seu próprio contexto isolado. O valor é definido pelo {@link TenantFilter}
 * no início de cada requisição e limpo ao final.</p>
 *
 * <p>Todos os services que precisam do tenant atual devem usar
 * {@link #get()} — nunca devem receber o organizationId como parâmetro de método.</p>
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    /**
     * Define o tenant ativo para a thread atual.
     * Chamado exclusivamente pelo {@link TenantFilter}.
     *
     * @param organizationId ID da organização ativa
     */
    public static void set(String organizationId) {
        CURRENT_TENANT.set(organizationId);
    }

    /**
     * Retorna o ID da organização ativa na thread atual.
     *
     * @return organizationId, ou {@code null} se não houver tenant definido
     */
    public static String get() {
        return CURRENT_TENANT.get();
    }

    /**
     * Remove o tenant da thread atual.
     * Deve ser chamado no {@code finally} do {@link TenantFilter} para evitar
     * vazamento de contexto entre requisições em pools de threads.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Verifica se há um tenant ativo na thread atual.
     *
     * @return {@code true} se o tenant estiver definido
     */
    public static boolean isSet() {
        return CURRENT_TENANT.get() != null;
    }
}
