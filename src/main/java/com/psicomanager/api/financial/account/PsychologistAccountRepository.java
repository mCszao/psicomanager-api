package com.psicomanager.api.financial.account;

import com.psicomanager.api.financial.account.model.PsychologistAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsychologistAccountRepository extends JpaRepository<PsychologistAccount, String> {

    Optional<PsychologistAccount> findByUserId(String userId);
}
