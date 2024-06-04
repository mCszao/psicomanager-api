package com.psicomanager.api.domain.patient;

import com.psicomanager.api.domain.address.Address;
import com.psicomanager.api.domain.address.AddressOnPatientDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "patients")
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
    private String name;
    private String email;
    private String phone;
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
