package com.psicomanager.api.financial;

import com.psicomanager.api.financial.transaction.TransactionRepository;
import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Job agendado que verifica diariamente as transações com vencimento expirado
 * e atualiza seus status para {@code OVERDUE}.
 *
 * <p>Executa todo dia às 01h00 (horário do servidor). Para cada transação
 * marcada como vencida, recalcula os saldos derivados das contas vinculadas.</p>
 */
@Component
@Slf4j
public class OverdueSchedulerJob {

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private AccountService accountService;

    /**
     * Marca como {@code OVERDUE} todas as transações com status {@code PENDING}
     * cujo {@code due_date} seja anterior à data atual.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void markOverdueTransactions() {
        log.info("[OverdueJob] Iniciando verificação de transações vencidas");

        var expired = transactionRepo.findByStatusAndDueDateBefore(
                TransactionStatusEnum.PENDING,
                LocalDate.now()
        );

        if (expired.isEmpty()) {
            log.info("[OverdueJob] Nenhuma transação vencida encontrada");
            return;
        }

        // Rastreia as contas afetadas para recalcular saldos apenas uma vez por conta
        Set<String> patientAccountsProcessed = new HashSet<>();
        Set<String> psychAccountsProcessed = new HashSet<>();

        for (var transaction : expired) {
            transaction.setStatus(TransactionStatusEnum.OVERDUE);
            transactionRepo.save(transaction);

            String patientAccId = transaction.getPatientAccount().getId();
            String psychAccId = transaction.getPsychologistAccount().getId();

            if (patientAccountsProcessed.add(patientAccId)) {
                accountService.recalculatePatientBalance(transaction.getPatientAccount());
            }
            if (psychAccountsProcessed.add(psychAccId)) {
                accountService.recalculatePsychologistBalance(transaction.getPsychologistAccount());
            }
        }

        log.info("[OverdueJob] {} transação(ões) marcada(s) como OVERDUE", expired.size());
    }
}
