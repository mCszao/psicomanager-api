package com.psicomanager.api.domain.patient.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.psicomanager.api.domain.address.model.Address;
import com.psicomanager.api.domain.document.model.Document;
import com.psicomanager.api.domain.patient.dto.PatientRegisterDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "patients", uniqueConstraints = {
        @UniqueConstraint(columnNames = "NAME"),
        @UniqueConstraint(columnNames = "PHONE"),
        @UniqueConstraint(columnNames = "EMAIL"),
        @UniqueConstraint(columnNames = "CPF")
})
@Entity(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotNull
    @Size(max = 255)
    @Column(name = "NAME", nullable = false, unique = true, length = 255)
    private String name;

    @Size(max = 100)
    @Email
    @Column(name = "EMAIL", unique = true, length = 100)
    private String email;

    @NotNull
    @Size(max = 100)
    @Column(name = "PHONE", nullable = false, unique = true, length = 100)
    private String phone;

    @Size(max = 11)
    @Column(name = "CPF", unique = true, length = 11)
    private String cpf;

    private Boolean isActive = Boolean.TRUE;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();
    private LocalDate birthdayDate;

    public void addAddress(Address address){
        this.addresses.add(address);
    }
}
