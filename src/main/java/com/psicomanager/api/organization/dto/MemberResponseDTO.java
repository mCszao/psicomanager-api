package com.psicomanager.api.organization.dto;

import com.psicomanager.api.organization.model.MemberRole;

import java.time.LocalDateTime;

public record MemberResponseDTO(
        String userId,
        String username,
        String email,
        MemberRole role,
        LocalDateTime joinedAt
) {}
