package com.psicomanager.api.domain.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressOnPatientDTO (
        String street,
        String district,
        @NotBlank
        @Pattern(regexp = "\\d{6,8}")
        String zipcode,

        String complement,
        String number,

        String state,
        String abbreviation,
        String city
){}
