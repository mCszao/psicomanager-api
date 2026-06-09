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
     * <p>O vencimento é definido em 30 dias a partir da data de conclusão (momento da geração
     * da cobrança), e não a partir da data agendada da sessão.</p>
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
        transaction.setDueDate(LocalDate.now().plusDays(30));
        transaction.setStatus(TransactionStatusEnum.PENDING);
        transaction.setOrganizationId(tenantService.optional());

        applyAutomaticCredit(transaction, patientAccount);
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
     * <p>O vencimento é definido em 30 dias a partir da data de adesão do plano.</p>
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
        transaction.setDueDate(plan.getAdherenceDate().plusDays(30));
        transaction.setStatus(TransactionStatusEnum.PENDING);
        transaction.setOrganizationId(tenantService.optional());

        applyAutomaticCredit(transaction, patientAccount);
        transactionRepo.save(transaction);
        accountService.recalculatePatientBalance(patientAccount);
        accountService.recalculatePsychologistBalance(psychAccount);

        log.info("PLAN_CHARGE gerado com status " + transaction.getStatus() + " para o plano de id " + plan.getId());
        return transaction;
    }

    /**
     * Aplica crédito disponível do paciente à transação recém-criada, registrando o
     * valor consumido em {@code creditApplied}.
     *
     * <p>Regras, dado {@code credit = patientAccount.creditBalance}:</p>
     * <ol>
     *   <li>{@code credit <= 0} → mantém PENDING, nada é aplicado;</li>
     *   <li>{@code credit >= amount} → status PAID, {@code creditApplied = amount};</li>
     *   <li>{@code 0 < credit < amount} → status PARTIALLY_PAID, {@code creditApplied = credit}.</li>
     * </ol>
     *
     * <p>Não altera saldos das contas: {@code creditBalance} do paciente, {@code totalReceivable}
     * e {@code totalReceived} do psicólogo são derivados pelos chamadores via
     * {@code recalculate*Balance} a partir dos campos persistidos.</p>
     *
     * @param transaction    transação com status PENDING a avaliar
     * @param patientAccount conta do paciente, fonte do crédito disponível
     */
    private void applyAutomaticCredit(
            FinancialTransaction transaction,
            PatientAccount patientAccount
    ) {
        BigDecimal credit = patientAccount.getCreditBalance();
        BigDecimal amount = transaction.getAmount();

        if (credit.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (credit.compareTo(amount) >= 0) {
            transaction.setStatus(TransactionStatusEnum.PAID);
            transaction.setPaidAt(LocalDateTime.now());
            transaction.setCreditApplied(amount);
            transaction.setNotes("Liquidado automaticamente por crédito disponível");
        } else {
            transaction.setStatus(TransactionStatusEnum.PARTIALLY_PAID);
            transaction.setCreditApplied(credit);
            transaction.setNotes("Parcialmente liquidado por crédito disponível");
        }
    }

    /**
     * Aplica crédito disponível a uma cobrança em aberto, incrementando {@code creditApplied}
     * e atualizando o status (PAID quando totalmente coberta, senão PARTIALLY_PAID). Não altera
     * saldos (derivados via {@code recalculate*Balance}). Retorna o crédito efetivamente consumido.
     *
     * @param t               cobrança a abater
     * @param availableCredit crédito disponível do paciente
     * @return valor de crédito consumido (≥ 0)
     */
    private BigDecimal applyCreditToTransaction(FinancialTransaction t, BigDecimal availableCredit) {
        if (availableCredit == null || availableCredit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal amount = t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO;
        BigDecimal paid = t.getAmountPaid() != null ? t.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal credit = t.getCreditApplied() != null ? t.getCreditApplied() : BigDecimal.ZERO;
        BigDecimal outstanding = amount.subtract(paid).subtract(credit);
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal toApply = availableCredit.min(outstanding);
        BigDecimal newCredit = credit.add(toApply);
        t.setCreditApplied(newCredit);
        if (paid.add(newCredit).compareTo(amount) >= 0) {
            t.setStatus(TransactionStatusEnum.PAID);
            t.setPaidAt(LocalDateTime.now());
        } else {
            t.setStatus(TransactionStatusEnum.PARTIALLY_PAID);
        }
        return toApply;
    }

    /**
     * Consome o crédito de adiantamento disponível do paciente abatendo as cobranças em aberto
     * (PENDING, OVERDUE, PARTIALLY_PAID), da mais antiga para a mais nova. Idempotente: nada faz
     * quando não há crédito ou cobranças em aberto. Recalcula os saldos afetados.
     *
     * @param patientAccount conta do paciente cujo crédito deve ser consumido
     */
    @Transactional
    public void applyCreditToOpenCharges(PatientAccount patientAccount) {
        BigDecimal credit = patientAccount.getCreditBalance();
        if (credit == null || credit.compareTo(BigDecimal.ZERO) <= 0) return;

        var openCharges = transactionRepo.findByPatientAccountIdAndStatusInOrderByCreatedAtAsc(
                patientAccount.getId(),
                List.of(TransactionStatusEnum.PENDING, TransactionStatusEnum.OVERDUE, TransactionStatusEnum.PARTIALLY_PAID)
        );

        var affectedPsych = new java.util.LinkedHashMap<String, PsychologistAccount>();
        for (var charge : openCharges) {
            if (credit.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal consumed = applyCreditToTransaction(charge, credit);
            if (consumed.compareTo(BigDecimal.ZERO) > 0) {
                transactionRepo.save(charge);
                credit = credit.subtract(consumed);
                affectedPsych.putIfAbsent(charge.getPsychologistAccount().getId(), charge.getPsychologistAccount());
            }
        }

        if (!affectedPsych.isEmpty()) {
            accountService.recalculatePatientBalance(patientAccount);
            affectedPsych.values().forEach(accountService::recalculatePsychologistBalance);
        }
    }

    // endregion

    // region Pagamentos e adiantamentos

    /**
     * Registra o pagamento (total ou parcial) de uma transação existente.
     *
     * <p>O pagamento em dinheiro é acumulado em {@code amountPaid} sobre o valor já pago,
     * somado ao {@code creditApplied} eventualmente existente. Quando
     * {@code amountPaid + creditApplied >= amount} a transação vira PAID e {@code amountPaid}
     * é limitado ao restante em aberto (evita inflar o total recebido); caso contrário vira
     * PARTIALLY_PAID.</p>
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

        // Feature B: consome o crédito disponível do paciente antes de aplicar o dinheiro
        BigDecimal availableCredit = transaction.getPatientAccount().getCreditBalance() != null
                ? transaction.getPatientAccount().getCreditBalance() : BigDecimal.ZERO;
        applyCreditToTransaction(transaction, availableCredit);

        BigDecimal amount = transaction.getAmount();
        BigDecimal creditApplied = transaction.getCreditApplied() != null
                ? transaction.getCreditApplied() : BigDecimal.ZERO;
        BigDecimal alreadyPaid = transaction.getAmountPaid() != null
                ? transaction.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newCashPaid = alreadyPaid.add(dto.amountPaid());

        if (newCashPaid.add(creditApplied).compareTo(amount) >= 0) {
            transaction.setStatus(TransactionStatusEnum.PAID);
            transaction.setPaidAt(LocalDateTime.now());
            transaction.setAmountPaid(amount.subtract(creditApplied).max(BigDecimal.ZERO));
        } else {
            transaction.setStatus(TransactionStatusEnum.PARTIALLY_PAID);
            transaction.setAmountPaid(newCashPaid);
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

        accountService.recalculatePatientBalance(patientAccount);
        accountService.recalculatePsychologistBalance(psychAccount);
        // Feature A: consome o crédito recém-creditado nas cobranças em aberto
        applyCreditToOpenCharges(patientAccount);
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
        transaction.setOrganizationId(tenantService.optional());
        transactionRepo.save(transaction);

        accountService.recalculatePatientBalance(patientAccount);
        accountService.recalculatePsychologistBalance(psychAccount);
        // Feature A: consome o crédito recém-creditado nas cobranças em aberto
        applyCreditToOpenCharges(patientAccount);
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
     * <p>Calculado diretamente do ledger (não dos saldos persistidos) para refletir sempre
     * o estado atual:</p>
     * <ul>
     *   <li>{@code totalReceivable} — valor em aberto ({@code amount - amountPaid - creditApplied})
     *       das cobranças PENDING, OVERDUE e PARTIALLY_PAID;</li>
     *   <li>{@code totalReceived} — caixa efetivo: adiantamentos (ADVANCE) mais a parte paga em
     *       dinheiro ({@code amountPaid}) de qualquer cobrança;</li>
     *   <li>{@code totalOverdue} — parte em aberto das cobranças OVERDUE;</li>
     *   <li>{@code totalPendingCount} — contagem das cobranças PENDING (parcialmente pagas não
     *       são pendência).</li>
     * </ul>
     *
     * @param userId ID do usuário (psicólogo)
     * @return DTO com totais consolidados
     */
    public FinancialSummaryDTO getSummary(String userId) {
        var psychAccount = accountService.getPsychologistAccount(userId);
        var orgId = tenantService.required();
        var allTransactions = transactionRepo
                .findByPsychologistAccountIdAndOrganizationId(psychAccount.getId(), orgId);

        BigDecimal totalReceivable = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.PENDING
                        || t.getStatus() == TransactionStatusEnum.OVERDUE
                        || t.getStatus() == TransactionStatusEnum.PARTIALLY_PAID)
                .map(FinancialTransaction::getOutstanding)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = allTransactions.stream()
                .map(t -> {
                    if (t.getStatus() == TransactionStatusEnum.ADVANCE) return t.getAmount();
                    return t.getAmountPaid() != null ? t.getAmountPaid() : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOverdue = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.OVERDUE)
                .map(FinancialTransaction::getOutstanding)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPendingCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.PENDING)
                .count();

        return new FinancialSummaryDTO(
                totalReceivable,
                totalReceived,
                totalOverdue,
                totalPendingCount
        );
    }

    // endregion
}
