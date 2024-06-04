package com.psicomanager.api.repositories;

import com.psicomanager.api.domain.patient.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, String> {
}
