package com.psicomanager.api.organization.dto;

import com.psicomanager.api.organization.model.MemberRole;

import java.time.LocalDateTime;

public record OrganizationResponseDTO(
        String id,
        String name,
        String slug,
        MemberRole myRole,
        LocalDateTime createdAt
) {}
