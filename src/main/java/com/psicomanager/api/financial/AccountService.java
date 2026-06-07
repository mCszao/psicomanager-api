package com.psicomanager.api.financial;

import com.psicomanager.api.financial.account.PatientAccountRepository;
import com.psicomanager.api.financial.account.PsychologistAccountRepository;
import com.psicomanager.api.financial.account.model.PatientAccount;
import com.psicomanager.api.financial.account.model.PsychologistAccount;
import com.psicomanager.api.financial.transaction.TransactionRepository;
import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import com.psicomanager.api.financial.transaction.model.FinancialTransaction;
import com.psicomanager.api.financial.transaction.exception.PatientAccountNotFoundException;
import com.psicomanager.api.infra.tenant.TenantService;
import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.user.UserRepository;
import com.psicomanager.api.user.exception.UserNotFoundException;
import com.psicomanager.api.user.model.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gerencia as contas financeiras de pacientes e psicólogos.
 *
 * <p>Responsável exclusivamente por criar contas e recalcular saldos derivados
 * a partir das transações do ledger. Não contém regras de negócio financeiro.</p>
 *
 * <p>Os métodos de busca ({@link #getPatientAccount} e {@link #getPsychologistAccount})
 * seguem o padrão <strong>get-or-create</strong>: se a conta não existir, ela é criada
 * automaticamente com saldos zerados. Isso garante que registros criados antes do módulo
 * financeiro nunca causem erros.</p>
 */
@Service
@Slf4j
public class AccountService {

    @Autowired
    private PatientAccountRepository patientAccountRepo;

    @Autowired
    private PsychologistAccountRepository psychologistAccountRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TenantService tenantService;

    // region Criação explícita de contas

    /**
     * Cria uma conta financeira zerada para o paciente informado.
     * Chamado automaticamente ao cadastrar um novo paciente.
     *
     * @param patient paciente já persistido
     * @return conta criada
     */
    @Transactional
    public PatientAccount createPatientAccount(Patient patient) {
        // Idempotente: se já existir conta, retorna a existente sem criar duplicata
        return patientAccountRepo.findByPatientId(patient.getId()).orElseGet(() -> {
            log.info("Criando patient_account para o paciente de id " + patient.getId());
            var account = new PatientAccount();
            account.setPatient(patient);
            account.setBalance(BigDecimal.ZERO);
            account.setCreditBalance(BigDecimal.ZERO);
            account.setOrganizationId(patient.getOrganizationId());
            return patientAccountRepo.save(account);
        });
    }

    /**
     * Cria uma conta financeira zerada para o psicólogo informado.
     * Chamado automaticamente ao cadastrar um novo usuário.
     *
     * @param user usuário (psicólogo) já persistido
     * @return conta criada
     */
    @Transactional
    public PsychologistAccount createPsychologistAccount(User user) {
        // Idempotente: se já existir conta, retorna a existente sem criar duplicata
        return psychologistAccountRepo.findByUserId(user.getId()).orElseGet(() -> {
            log.info("Criando psychologist_account para o usuário de id " + user.getId());
            var account = new PsychologistAccount();
            account.setUser(user);
            account.setTotalReceivable(BigDecimal.ZERO);
            account.setTotalReceived(BigDecimal.ZERO);
            account.setOrganizationId(tenantService.optional());
            return psychologistAccountRepo.save(account);
        });
    }

    // endregion

    // region Busca de contas (get-or-create)

    /**
     * Retorna a conta financeira do paciente.
     *
     * <p>Se a conta não existir (ex: paciente criado antes do módulo financeiro),
     * ela é criada automaticamente com saldos zerados antes de ser retornada.</p>
     *
     * @param patientId ID do paciente
     * @return conta do paciente (existente ou recém-criada)
     * @throws PatientAccountNotFoundException se o próprio paciente não for encontrado
     */
    @Transactional
    public PatientAccount getPatientAccount(String patientId) {
        return patientAccountRepo.findByPatientId(patientId).orElseGet(() -> {
            log.warn("patient_account não encontrada para patientId {}. Criando automaticamente.", patientId);
            var patient = patientRepo.findById(patientId)
                    .orElseThrow(PatientAccountNotFoundException::new);
            return createPatientAccount(patient);
        });
    }

    /**
     * Retorna a conta financeira do psicólogo.
     *
     * <p>Se a conta não existir (ex: usuário criado antes do módulo financeiro),
     * ela é criada automaticamente com saldos zerados antes de ser retornada.</p>
     *
     * @param userId ID do usuário (psicólogo)
     * @return conta do psicólogo (existente ou recém-criada)
     * @throws UserNotFoundException se o próprio usuário não for encontrado
     */
    @Transactional
    public PsychologistAccount getPsychologistAccount(String userId) {
        return psychologistAccountRepo.findByUserId(userId).orElseGet(() -> {
            log.warn("psychologist_account não encontrada para userId {}. Criando automaticamente.", userId);
            var user = userRepo.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Psicólogo não encontrado para o id: " + userId));
            return createPsychologistAccount(user);
        });
    }

    // endregion

    // region Recálculo de saldos

    /**
     * Recalcula e persiste os saldos derivados da conta do paciente com base nas transações do ledger.
     *
     * <ul>
     *   <li>{@code balance} = soma do valor em aberto ({@code amount - amountPaid - creditApplied})
     *       de transações PENDING, OVERDUE e PARTIALLY_PAID</li>
     *   <li>{@code creditBalance} = soma dos adiantamentos (ADVANCE) menos o crédito já consumido
     *       em cobranças ({@code creditApplied})</li>
     * </ul>
     *
     * @param account conta do paciente a recalcular
     */
    @Transactional
    public void recalculatePatientBalance(PatientAccount account) {
        var transactions = transactionRepo.findByPatientAccountIdOrderByCreatedAtDesc(account.getId());

        BigDecimal balance = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.PENDING
                        || t.getStatus() == TransactionStatusEnum.OVERDUE
                        || t.getStatus() == TransactionStatusEnum.PARTIALLY_PAID)
                .map(FinancialTransaction::getOutstanding)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAdvance = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.ADVANCE)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditConsumed = transactions.stream()
                .map(t -> t.getCreditApplied() != null ? t.getCreditApplied() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        account.setBalance(balance);
        account.setCreditBalance(totalAdvance.subtract(creditConsumed));
        patientAccountRepo.save(account);
    }

    /**
     * Recalcula e persiste os saldos derivados da conta do psicólogo com base nas transações do ledger.
     *
     * <ul>
     *   <li>{@code totalReceivable} = soma do valor em aberto ({@code amount - amountPaid - creditApplied})
     *       de transações PENDING, OVERDUE e PARTIALLY_PAID</li>
     *   <li>{@code totalReceived} = caixa efetivamente recebido: adiantamentos (ADVANCE) +
     *       parte paga em dinheiro de qualquer cobrança ({@code amountPaid}). O crédito aplicado
     *       não entra aqui pois já foi contabilizado quando o adiantamento foi recebido.</li>
     * </ul>
     *
     * <p>Resolve o tenant via {@code tenantService.optional()} (e não {@code required()}) porque
     * pode ser invocado pelo {@code OverdueSchedulerJob}, fora de um contexto de requisição;
     * nesse caso recai sobre um filtro em memória por conta do psicólogo.</p>
     *
     * @param account conta do psicólogo a recalcular
     */
    @Transactional
    public void recalculatePsychologistBalance(PsychologistAccount account) {
        String orgId = tenantService.optional();
        var allTransactions = orgId != null
                ? transactionRepo.findByPsychologistAccountIdAndOrganizationId(account.getId(), orgId)
                : transactionRepo.findAll().stream()
                        .filter(t -> t.getPsychologistAccount().getId().equals(account.getId()))
                        .toList();

        BigDecimal receivable = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.PENDING
                        || t.getStatus() == TransactionStatusEnum.OVERDUE
                        || t.getStatus() == TransactionStatusEnum.PARTIALLY_PAID)
                .map(FinancialTransaction::getOutstanding)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedAdvance = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatusEnum.ADVANCE)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedCash = allTransactions.stream()
                .map(t -> t.getAmountPaid() != null ? t.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        account.setTotalReceivable(receivable);
        account.setTotalReceived(receivedAdvance.add(receivedCash));
        psychologistAccountRepo.save(account);
    }

    // endregion
}
