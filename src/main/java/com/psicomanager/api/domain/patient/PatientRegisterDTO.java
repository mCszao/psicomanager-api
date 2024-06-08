package com.psicomanager.api.domain.patient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psicomanager.api.domain.address.AddressOnPatientDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Date;

public record PatientRegisterDTO (
        @NotBlank
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Pattern(regexp = "\\d{11}")
        String cpf,
        @NotBlank
        @Pattern(regexp = "\\d{11,15}")
        String phone,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        LocalDate birthdayDate,
        @Valid
        AddressOnPatientDTO address
){
}
