package com.psicomanager.api.domain.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressOnPatientDTO (
        @NotBlank
        String street,
        @NotBlank
        String district,
        @NotBlank
        @Pattern(regexp = "\\d{6,8}")
        String zipcode,

        String complement,
        @NotBlank
        String number,

        String state,
        String abbreviation,
        String city
){}
