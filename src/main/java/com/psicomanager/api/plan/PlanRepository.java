package com.psicomanager.api.plan;

import com.psicomanager.api.plan.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, String> {
    List<Plan> findByPatientId(String patientId);
    List<Plan> findByPatientIdAndIsActiveTrue(String patientId);
}
