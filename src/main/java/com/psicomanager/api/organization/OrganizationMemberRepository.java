package com.psicomanager.api.organization;

import com.psicomanager.api.organization.model.MemberRole;
import com.psicomanager.api.organization.model.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, String> {

    List<OrganizationMember> findByUserId(String userId);

    List<OrganizationMember> findByOrganizationId(String organizationId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(String organizationId, String userId);

    boolean existsByOrganizationIdAndUserId(String organizationId, String userId);

    boolean existsByOrganizationIdAndUserIdAndRole(String organizationId, String userId, MemberRole role);
}
