package com.psicomanager.api.domain.address.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.psicomanager.api.domain.address.dto.AddressOnPatientDTO;
import com.psicomanager.api.domain.patient.model.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Table(name = "addresses")
@Entity(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Size(max = 100)
    @Column(name = "STREET", nullable = false, length = 100)
    private String street;

    @Size(max = 100)
    @Column(name = "DISTRICT", nullable = false, length = 100)
    private String district;

    @NotNull
    @Size(max = 9)
    @Column(name = "ZIPCODE", length = 9)
    private String zipcode;

    @Size(max = 100)
    @Column(name = "COMPLEMENT", length = 100)
    private String complement;

    @Size(max = 20)
    @Column(name = "NUMBER", nullable = false, length = 20)
    private String number;

    @NotNull
    @Size(max = 100)
    @Column(name = "STATE", nullable = false, length = 100)
    private String state = "MATO-GROSSO";

    @Size(max = 5)
    @Column(name = "ABBREVIATION", length = 5)
    private String abbreviation = "MT";

    @NotNull
    @Size(max = 100)
    @Column(name = "CITY", nullable = false, length = 100)
    private String city = "CUIABÁ";

    @NotNull
    @ManyToOne
    @JoinColumn(name = "patient" , nullable = false)
    @JsonBackReference
    private Patient patient;


    public Address(AddressOnPatientDTO dto, Patient patient){
        this.street = dto.street();
        this.district = dto.district();
        this.zipcode = dto.zipcode();
        this.complement = dto.complement();
        this.number = dto.number();
        this.state = dto.state() == null ? "MATO-GROSSO" : dto.state();
        this.abbreviation = dto.abbreviation() == null ? "MT" : dto.abbreviation();
        this.city = dto.city() == null ? "Cuiabá" : dto.city();
        this.patient = patient;
    }
}
