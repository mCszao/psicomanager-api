package com.psicomanager.api.organization.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationDTO(
        @NotBlank @Size(max = 255) String name
) {}
