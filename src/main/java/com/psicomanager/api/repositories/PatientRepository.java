package com.psicomanager.api.repositories;

import com.psicomanager.api.domain.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, String> {
    Patient findByName(String name);
    Patient findByEmail(String email);
    Patient findByPhone(String phone);
    Patient findByCpf(String cpf);


}
