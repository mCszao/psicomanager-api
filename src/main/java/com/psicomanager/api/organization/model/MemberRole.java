package com.psicomanager.api.organization.model;

/**
 * Papel do membro dentro de uma organização.
 *
 * <ul>
 *   <li>{@link #OWNER} — criador da organização. Pode excluir a org e gerenciar todos os membros.</li>
 *   <li>{@link #ADMIN} — pode convidar/remover membros, mas não pode excluir a organização.</li>
 *   <li>{@link #MEMBER} — acesso completo aos dados da organização, sem poderes administrativos.</li>
 * </ul>
 */
public enum MemberRole {
    OWNER,
    ADMIN,
    MEMBER
}
