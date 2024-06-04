package com.psicomanager.api.domain.address;

import com.psicomanager.api.domain.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

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
    private String street;
    private String district;
    private String zipcode;
    private String complement;
    private String number;
    private String state;
    private String abbreviation;
    private String city;
    @ManyToOne
    @JoinColumn(name = "patient")
    private Patient patient;


    public Address(AddressOnPatientDTO dto, Patient patient){
        this.street = dto.street();
        this.district = dto.district();
        this.zipcode = dto.zipcode();
        this.complement = dto.complement();
        this.number = dto.number();
        this.state = dto.state();
        this.abbreviation = dto.abbreviation();
        this.city = dto.city();
        this.patient = patient;
    }
}
