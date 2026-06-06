package com.psicomanager.api.financial.transaction;

import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import com.psicomanager.api.financial.transaction.model.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<FinancialTransaction, String> {

    List<FinancialTransaction> findByPatientAccountIdOrderByCreatedAtDesc(String patientAccountId);

    List<FinancialTransaction> findByStatus(TransactionStatusEnum status);

    List<FinancialTransaction> findByStatusAndDueDateBefore(TransactionStatusEnum status, LocalDate date);

    Optional<FinancialTransaction> findBySessionIdAndStatusIn(String sessionId, List<TransactionStatusEnum> statuses);

    /** Filtra transações por tenant — substitui findAll() nas queries públicas. */
    List<FinancialTransaction> findByOrganizationId(String organizationId);

    /** Filtra transações por psicólogo e tenant. */
    List<FinancialTransaction> findByPsychologistAccountIdAndOrganizationId(
            String psychologistAccountId, String organizationId);
}
