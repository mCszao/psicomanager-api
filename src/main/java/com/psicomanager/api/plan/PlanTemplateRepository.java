package com.psicomanager.api.plan;

import com.psicomanager.api.plan.template.model.PlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanTemplateRepository extends JpaRepository<PlanTemplate, String> {
    List<PlanTemplate> findByOrganizationId(String organizationId);
}
