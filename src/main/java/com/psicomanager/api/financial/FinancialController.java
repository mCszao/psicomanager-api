package com.psicomanager.api.financial;

import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.financial.transaction.dto.*;
import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import com.psicomanager.api.user.model.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe os endpoints do módulo financeiro.
 *
 * <p>O psicólogo autenticado é identificado via {@link SecurityContextHolder},
 * seguindo o mesmo padrão do filtro JWT ({@code FilterSecurity}).</p>
 */
@RestController
@RequestMapping("/financial")
@Slf4j
public class FinancialController {

    @Autowired
    private FinancialService financialService;

    @Autowired
    private AccountService accountService;

    // region Utilitários

    /**
     * Obtém o ID do usuário autenticado a partir do SecurityContextHolder.
     *
     * @return ID do psicólogo autenticado
     */
    private String getAuthenticatedUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    // endregion

    // region Transações

    /**
     * Lista todas as transações do sistema.
     *
     * @return lista de transações
     */
    @GetMapping("/transactions")
    public ResponseEntity<BaseResponse<List<TransactionResponseDTO>>> getAllTransactions() {
        log.info("GET: /financial/transactions");
        return ResponseEntity.ok(new BaseResponse<>(true, financialService.getAllTransactions()));
    }

    /**
     * Lista as transações de um paciente específico.
     *
     * @param id ID do paciente
     * @return lista de transações do paciente
     */
    @GetMapping("/transactions/patient")
    public ResponseEntity<BaseResponse<List<TransactionResponseDTO>>> getByPatient(@RequestParam String id) {
        log.info("GET: /financial/transactions/patient?id=" + id);
        return ResponseEntity.ok(new BaseResponse<>(true, financialService.getTransactionsByPatient(id)));
    }

    /**
     * Cancela uma transação pelo ID.
     *
     * @param id ID da transação
     * @return mensagem de confirmação
     */
    @PatchMapping("/transactions/{id}/cancel")
    public ResponseEntity<BaseResponse<String>> cancelTransaction(@PathVariable String id) {
        log.info("PATCH: /financial/transactions/" + id + "/cancel");
        financialService.cancelTransaction(id);
        return ResponseEntity.ok(new BaseResponse<>(true, "Transação cancelada com sucesso!"));
    }

    // endregion

    // region Resumo

    /**
     * Retorna o resumo financeiro consolidado do psicólogo autenticado.
     *
     * @return DTO com totais consolidados
     */
    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<FinancialSummaryDTO>> getSummary() {
        log.info("GET: /financial/summary");
        String userId = getAuthenticatedUserId();
        return ResponseEntity.ok(new BaseResponse<>(true, financialService.getSummary(userId)));
    }

    // endregion

    // region Pagamentos

    /**
     * Registra o pagamento (total ou parcial) de uma cobrança.
     *
     * @param body dados do pagamento
     * @return mensagem de confirmação
     */
    @PostMapping("/payments")
    public ResponseEntity<BaseResponse<String>> registerPayment(@RequestBody @Valid PaymentRegisterDTO body) {
        log.info("POST: /financial/payments — transactionId: " + body.transactionId());
        financialService.registerPayment(body.transactionId(), body);
        return ResponseEntity.ok(new BaseResponse<>(true, "Pagamento registrado com sucesso!"));
    }

    /**
     * Registra um adiantamento de um paciente.
     *
     * @param body dados do adiantamento
     * @return mensagem de confirmação
     */
    @PostMapping("/advance")
    public ResponseEntity<BaseResponse<String>> registerAdvance(@RequestBody @Valid AdvancePaymentRegisterDTO body) {
        log.info("POST: /financial/advance — patientId: " + body.patientId());
        String userId = getAuthenticatedUserId();
        var psychAccount = accountService.getPsychologistAccount(userId);
        financialService.registerAdvancePayment(body, psychAccount);
        return ResponseEntity.ok(new BaseResponse<>(true, "Adiantamento registrado com sucesso!"));
    }

    // endregion
}
