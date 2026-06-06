package com.psicomanager.api.patient;

import com.psicomanager.api.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {
    Patient findByName(String name);
    Patient findByEmail(String email);
    Patient findByPhone(String phone);
    Patient findByCpf(String cpf);

    /** Retorna todos os pacientes de uma organização (tenant). */
    List<Patient> findByOrganizationId(String organizationId);

    /** Busca por ID garantindo que o paciente pertence ao tenant. */
    Optional<Patient> findByIdAndOrganizationId(String id, String organizationId);
}
