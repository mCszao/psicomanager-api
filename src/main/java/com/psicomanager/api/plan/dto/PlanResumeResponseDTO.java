package com.psicomanager.api.plan.dto;

/**
 * Resumo do plano retornado dentro de uma sessão de agendamento.
 * Contém apenas as informações necessárias para identificação e exibição no frontend.
 */
public record PlanResumeResponseDTO(
        String id,
        String title
) {}
