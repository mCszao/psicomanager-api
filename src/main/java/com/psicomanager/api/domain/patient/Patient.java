package com.psicomanager.api.domain.patient;

import com.psicomanager.api.domain.address.Address;
import com.psicomanager.api.domain.address.AddressOnPatientDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
    private String Id;
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
    private List<Address> addresses = new ArrayList<>();
    private LocalDate birthdayDate;

    public Patient(PatientRegisterDTO dto){
        this.name = dto.name();
        this.email = dto.email();
        this.phone = dto.phone();
        this.cpf = dto.cpf();
        this.birthdayDate = dto.birthdayDate();
        if(dto.address() != null){
            Address transactAddress = new Address(dto.address(), this);
            this.addresses.add(transactAddress);
        }
    }
}
