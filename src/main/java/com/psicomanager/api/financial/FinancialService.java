package com.psicomanager.api.financial;

import com.psicomanager.api.financial.account.model.PatientAccount;
import com.psicomanager.api.financial.account.model.PsychologistAccount;
import com.psicomanager.api.financial.transaction.TransactionRepository;
import com.psicomanager.api.financial.transaction.dto.*;
import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import com.psicomanager.api.financial.transaction.enums.TransactionTypeEnum;
import com.psicomanager.api.financial.transaction.exception.*;
import com.psicomanager.api.financial.transaction.mapper.TransactionMapper;
import com.psicomanager.api.financial.transaction.model.FinancialTransaction;
import com.psicomanager.api.infra.tenant.TenantService;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.schedule.model.Schedule;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contém toda a regra de negócio financeira do sistema.
 *
 * <p>Gerencia geração de cobranças, registro de pagamentos, adiantamentos,
 * cancelamentos e consultas. Delega operações de saldo ao {@link AccountService}.</p>
 */
@Service
@Slf4j
public class FinancialService {

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TenantService tenantService;

    // region Geração de cobranças

    /**
     * Gera uma cobrança de sessão ({@code SESSION_CHARGE}) ao concluir uma sessão
     * avulsa ou de plano contínuo.
     *
     * <p>Aplica automaticamente crédito disponível do paciente, podendo liquidar
     * total ou parcialmente a cobrança gerada.</p>
     *
     * @param schedule     sessão concluída
     * @param psychAccount conta do psicólogo autenticado
     * @return transação criada
     */
    @Transactional
    public FinancialTransaction generateSessionCharge(Schedule schedule, PsychologistAccount psychAccount) {
        log.info("Gerando SESSION_CHARGE para a sessão de id " + schedule.getId());
        var patientAccount = accountService.getPatientAccount(schedule.getPatient().getId());

        var transaction = new FinancialTransaction();
        transaction.setType(TransactionTypeEnum.SESSION_CHARGE);
        transaction.setAmount(schedule.getSessionValue() != null ? schedule.getSessionValue() : BigDecimal.ZERO);
        transaction.setSession(schedule);
        transaction.setPatientAccount(patientAccount);
        transaction.setPsychologistAccount(psychAccount);
        transaction.setDueDate(schedule.getDateStart().toLocalDate().plusDays(7));
        transaction.setStatus(TransactionStatusEnum.PENDING);
        transaction.setOrganizationId(tenantService.optional());

        applyAutomaticCredit(transaction, patientAccount, psychAccount);
        transactionRepo.save(transaction);
        accountService.recalculatePatientBalance(patientAccount);
        accountService.recalculatePsychologistBalance(psychAccount);

        log.info("SESSION_CHARGE gerado com status " + transaction.getStatus() + " para a sessão de id " + schedule.getId());
        return transaction;
    }

    /**
     * Gera uma cobrança de plano ({@code PLAN_CHARGE}) ao criar um plano fechado
     * ({@code IS_CONTINUOUS = false}).
     *
     * <p>Aplica automaticamente crédito disponível do paciente.</p>
     *
     * @param plan         plano fechado recém-criado
     * @param psychAccount conta do psicólogo autenticado
     * @return transação criada
     */
    @Transactional
    public FinancialTransaction generatePlanCharge(Plan plan, PsychologistAccount psychAccount) {
        log.info("Gerando PLAN_CHARGE para o plano de id " + plan.getId());
        var patientAccount = accountService.getPatientAccount(plan.getPatient().getId());

        var transaction = new FinancialTransaction();
        transaction.setType(TransactionTypeEnum.PLAN_CHARGE);
        transaction.setAmount(plan.getTotalValue() != null ? plan.getTotalValue() : BigDecimal.ZERO);
        transaction.setPlan(plan);
        transaction.setPatientAccount(patientAccount);
        transaction.setPsychologistAccount(psychAccount);
        transaction.setDueDate(plan.getAdherenceDate().plusDays(7));
        transaction.setStatus(TransactionStatusEnum.PENDING);
        transaction.setOrganizationId(tenantService.optional());

        applyAutomaticCredit(transaction, patientAccount, psychAccount);
        transactionRepo.save(transaction);
        accountService.recalculatePatientBalance(patientAccount);
        accountService.recalculatePsychologistBalance(psychAccount);

        log.info("PLAN_CHARGE gerado com status " + transaction.getStatus() + " para o plano de id " + plan.getId());
        return transaction;
    }

    /**
     * Aplica crédito disponível do paciente à transação recém-criada.
     *
     * <p>Regras:</p>
     * <ol>
     *   <li>Se creditBalance == 0 → mantém PENDING, incrementa totalReceivable</li>
     *   <li>Se creditBalance >= amount → status PAID, zera crédito usado, incrementa totalReceived</li>
     *   <li>Se 0 < creditBalance < amount → status PARTIALLY_PAID, zera creditBalance, split entre receivable e received</li>
     * </ol>
     *
     * @param transaction  transação com status PENDING a avaliar
     * @param patientAccount conta do paciente
     * @param psychAccount   conta do psicólogo
     */
    private void applyAutomaticCredit(
            FinancialTransaction transaction,
            PatientAccount patientAccount,
            PsychologistAccount psychAccount
    ) {
        BigDecimal credit = patientAccount.getCreditBalance();
        BigDecimal amount = transaction.getAmount();

        if (credit.compareTo(BigDecimal.ZERO) == 0) {
            psychAccount.setTotalReceivable(psychAccount.getTotalReceivable().add(amount));
            return;
        }

        if (credit.compareTo(amount) >= 0) {
            transaction.setStatus(TransactionStatusEnum.PAID);
            transaction.setPaidAt(LocalDateTime.now());
            transaction.setNotes("Liquidado automaticamente por crédito disponível");
            patientAccount.setCreditBalance(credit.subtract(amount));
            psychAccount.setTotalReceived(psychAccount.getTotalReceived().add(amount));
        } else {
            // 0 < credit < amount
            BigDecimal remaining = amount.subtract(credit);
            transaction.setStatus(TransactionStatusEnum.PARTIALLY_PAID);
            transaction.setNotes("Parcialmente liquidado por crédito disponível");
            patientAccount.setCreditBalance(BigDecimal.ZERO);
            psychAccount.setTotalReceivable(psychAccount.getTotalReceivable().add(remaining));
            psychAccount.setTotalReceived(psychAccount.getTotalReceived().add(credit));
        }
    }

    // endregion

    // region Pagamentos e adiantamentos

    /**
     * Registra o pagamento (total ou parcial) de uma transação existente.
     *
     * @param transactionId ID da transação a pagar
     * @param dto           dados do pagamento
     * @throws TransactionNotFoundException    se a transação não existir
     * @throws TransactionAlreadyPaidException se a transação já estiver paga
     * @throws TransactionCancelledException   se a transação estiver cancelada
     */
    @Transactional
    public void registerPayment(String transactionId, PaymentRegisterDTO dto) {
        log.info("Registrando pagamento para a transação de id " + transactionId);
        var transaction = transactionRepo.findById(transactionId)
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.getStatus() == TransactionStatusEnum.PAID) throw new TransactionAlreadyPaidException();
        if (transaction.getStatus() == TransactionStatusEnum.CANCELLED) throw new TransactionCancelledException();

        if (dto.amountPaid().compareTo(transaction.getAmount()) >= 0) {
            transaction.setStatus(TransactionStatusEnum.PAID);
            transaction.setPaidAt(LocalDateTime.now());
        } else {
            transaction.setStatus(TransactionStatusEnum.PARTIALLY_PAID);
        }

        transaction.setPaymentMethod(dto.paymentMethod());
        if (dto.notes() != null) transaction.setNotes(dto.notes());
        transactionRepo.save(transaction);

        accountService.recalculatePatientBalance(transaction.getPatientAccount());
        accountService.recalculatePsychologistBalance(transaction.getPsychologistAccount());
        log.info("Pagamento registrado com sucesso para a transação de id " + transactionId);
    }

    /**
     * Registra um adiantamento do paciente.
     *
     * <p>Cria uma transação com status {@code ADVANCE} e incrementa o
     * {@code creditBalance} da conta do paciente.</p>
     *
     * @param dto dados do adiantamento
     */
    @Transactional
    public void registerAdvancePayment(AdvancePaymentRegisterDTO dto) {
        log.info("Registrando adiantamento para o paciente de id " + dto.patientId());
        var patientAccount = accountService.getPatientAccount(dto.patientId());
        var psychAccount = accountService.getPsychologistAccount(
                patientAccount.getPatient().getId() // será sobrescrito via controller — aqui é placeholder
        );

        var transaction = new FinancialTransaction();
        transaction.setType(TransactionTypeEnum.ADVANCE_PAYMENT);
        transaction.setAmount(dto.amount());
        transaction.setStatus(TransactionStatusEnum.ADVANCE);
        transaction.setPaidAt(LocalDateTime.now());
        transaction.setPaymentMethod(dto.paymentMethod());
        transaction.setNotes(dto.notes());
        transaction.setPatientAccount(patientAccount);
        transaction.setPsychologistAccount(psychAccount);
        transaction.setOrganizationId(tenantService.optional());
        transactionRepo.save(transaction);

        patientAccount.setCreditBalance(patientAccount.getCreditBalance().add(dto.amount()));
        psychAccount.setTotalReceived(psychAccount.getTotalReceived().add(dto.amount()));
        accountService.recalculatePatientBalance(patientAccount);
        accountService.recalculatePsychologistBalance(psychAccount);
        log.info("Adiantamento de R$ " + dto.amount() + " registrado para o paciente de id " + dto.patientId());
    }

    /**
     * Registra um adiantamento do paciente com a conta do psicólogo explicitamente informada.
     *
     * @param dto          dados do adiantamento
     * @param psychAccount conta do psicólogo autenticado
     */
    @Transactional
    public void registerAdvancePayment(AdvancePaymentRegisterDTO dto, PsychologistAccount psychAccount) {
        log.info("Registrando adiantamento para o paciente de id " + dto.patientId());
        var patientAccount = accountService.getPatientAccount(dto.patientId());

        var transaction = new FinancialTransaction();
        transaction.setType(TransactionTypeEnum.ADVANCE_PAYMENT);
        transaction.setAmount(dto.amount());
        transaction.setStatus(TransactionStatusEnum.ADVANCE);
        transaction.setPaidAt(LocalDateTime.now());
        transaction.setPaymentMethod(dto.paymentMethod());
        transaction.setNotes(dto.notes());
        transaction.setPatientAccount(patientAccount);
        transaction.setPsychologistAccount(psychAccount);
        transactionRepo.save(transaction);

        patientAccount.setCreditBalance(patientAccount.getCreditBalance().add(dto.amount()));
        psychAccount.setTotalReceived(psychAccount.getTotalReceived().add(dto.amount()));
        accountService.recalculatePatientBalance(patientAccount);
        accountService.recalculatePsychologistBalance(psychAccount);
        log.info("Adiantamento de R$ " + dto.amount() + " registrado para o paciente de id " + dto.patientId());
    }

    // endregion

    // region Cancelamentos

    /**
     * Cancela uma transação. Se já estiver paga, gera um REFUND como nova linha no ledger.
     *
     * @param transactionId ID da transação a cancelar
     * @throws TransactionNotFoundException se a transação não existir
     */
    @Transactional
    public void cancelTransaction(String transactionId) {
        log.info("Cancelando transação de id " + transactionId);
        var transaction = transactionRepo.findById(transactionId)
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.getStatus() == TransactionStatusEnum.PAID) {
            log.info("Transação já paga — gerando REFUND para a transação de id " + transactionId);
            var refund = new FinancialTransaction();
            refund.setType(TransactionTypeEnum.REFUND);
            refund.setAmount(transaction.getAmount());
            refund.setStatus(TransactionStatusEnum.CANCELLED);
            refund.setPatientAccount(transaction.getPatientAccount());
            refund.setPsychologistAccount(transaction.getPsychologistAccount());
            refund.setNotes("Estorno da transação de id " + transactionId);
            transactionRepo.save(refund);
        } else {
            transaction.setStatus(TransactionStatusEnum.CANCELLED);
            transactionRepo.save(transaction);
        }

        accountService.recalculatePatientBalance(transaction.getPatientAccount());
        accountService.recalculatePsychologistBalance(transaction.getPsychologistAccount());
        log.info("Transação de id " + transactionId + " cancelada com sucesso");
    }

    /**
     * Cancela a transação pendente vinculada à sessão informada, se existir.
     * Método idempotente — não lança exceção se nenhuma transação for encontrada.
     *
     * @param sessionId ID da sessão cujas transações pendentes devem ser canceladas
     */
    @Transactional
    public void cancelTransactionBySessionIfPending(String sessionId) {
        log.info("Verificando transação pendente para a sessão de id " + sessionId);
        var result = transactionRepo.findBySessionIdAndStatusIn(
                sessionId,
                List.of(TransactionStatusEnum.PENDING, TransactionStatusEnum.OVERDUE)
        );
        result.ifPresent(t -> {
            t.setStatus(TransactionStatusEnum.CANCELLED);
            transactionRepo.save(t);
            accountService.recalculatePatientBalance(t.getPatientAccount());
            accountService.recalculatePsychologistBalance(t.getPsychologistAccount());
            log.info("Transação de id " + t.getId() + " cancelada por cancelamento da sessão de id " + sessionId);
        });
    }

    // endregion

    // region Consultas

    /**
     * Retorna todas as transações de um paciente, ordenadas da mais recente para a mais antiga.
     *
     * @param patientId ID do paciente
     * @return lista de DTOs de transação
     */
    public List<TransactionResponseDTO> getTransactionsByPatient(String patientId) {
        var account = accountService.getPatientAccount(patientId);
        return transactionRepo.findByPatientAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    /**
     * Retorna todas as transações com o status informado.
     *
     * @param status status a filtrar
     * @return lista de DTOs de transação
     */
    public List<TransactionResponseDTO> getTransactionsByStatus(TransactionStatusEnum status) {
        return transactionRepo.findByStatus(status)
                .stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    /**
     * Retorna todas as transações do sistema.
     *
     * @return lista de DTOs de transação
     */
    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionRepo.findByOrganizationId(tenantService.required())
                .stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    /**
     * Retorna o resumo financeiro consolidado do psicólogo.
     *
     * @param userId ID do usuário (psicólogo)
     * @return DTO com totais consolidados
     */
    public FinancialSummaryDTO getSummary(String userId) {
        var psychAccount = accountService.getPsychologistAccount(userId);
        var orgId = tenantService.required();
        var allTransactions = transactionRepo
                .findByPsychologistAccountIdAndOrganizationId(psychAccount.getId(), orgId);

        BigDecimal totalOverdue = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.OVERDUE)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPendingCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.PENDING)
                .count();

        return new FinancialSummaryDTO(
                psychAccount.getTotalReceivable(),
                psychAccount.getTotalReceived(),
                totalOverdue,
                totalPendingCount
        );
    }

    // endregion
}
