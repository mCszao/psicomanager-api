package com.psicomanager.api.organization;

import com.psicomanager.api.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, String> {
    Optional<Organization> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
