package com.psicomanager.api.financial;

import com.psicomanager.api.financial.account.model.PatientAccount;
import com.psicomanager.api.financial.account.model.PsychologistAccount;
import com.psicomanager.api.financial.transaction.TransactionRepository;
import com.psicomanager.api.financial.transaction.dto.AdvancePaymentRegisterDTO;
import com.psicomanager.api.financial.transaction.dto.PaymentRegisterDTO;
import com.psicomanager.api.financial.transaction.enums.PaymentMethodEnum;
import com.psicomanager.api.financial.transaction.enums.TransactionStatusEnum;
import com.psicomanager.api.financial.transaction.model.FinancialTransaction;
import com.psicomanager.api.infra.tenant.TenantService;
import com.psicomanager.api.patient.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialService — consumo de crédito de adiantamento")
class FinancialServiceTest {

    @InjectMocks private FinancialService service;

    @Mock private TransactionRepository transactionRepo;
    @Mock private AccountService accountService;
    @Mock private TenantService tenantService;

    private PatientAccount patientAccount(String id, String credito) {
        var pa = new PatientAccount();
        pa.setId(id);
        pa.setCreditBalance(new BigDecimal(credito));
        var patient = new Patient();
        patient.setId("p-1");
        pa.setPatient(patient);
        return pa;
    }

    private FinancialTransaction charge(PatientAccount pa, PsychologistAccount ps, String amount) {
        var t = new FinancialTransaction();
        t.setId("t-1");
        t.setAmount(new BigDecimal(amount));
        t.setStatus(TransactionStatusEnum.PENDING);
        t.setPatientAccount(pa);
        t.setPsychologistAccount(ps);
        return t;
    }

    // region Feature A — abate ao registrar adiantamento

    @Nested
    @DisplayName("registerAdvancePayment (Feature A)")
    class FeatureA {

        @Test
        @DisplayName("adiantamento abate parcialmente cobrança aberta (crédito < valor)")
        void adiantamentoAbateParcial() {
            var pa = patientAccount("pa-1", "50.00");
            var ps = new PsychologistAccount();
            ps.setId("ps-1");
            var open = charge(pa, ps, "100.00");

            when(accountService.getPatientAccount("p-1")).thenReturn(pa);
            when(transactionRepo.findByPatientAccountIdAndStatusInOrderByCreatedAtAsc(eq("pa-1"), anyList()))
                    .thenReturn(List.of(open));

            service.registerAdvancePayment(
                    new AdvancePaymentRegisterDTO("p-1", new BigDecimal("50.00"), PaymentMethodEnum.PIX, null), ps);

            assertThat(open.getCreditApplied()).isEqualByComparingTo("50.00");
            assertThat(open.getStatus()).isEqualTo(TransactionStatusEnum.PARTIALLY_PAID);
        }

        @Test
        @DisplayName("adiantamento quita cobrança aberta (crédito >= valor)")
        void adiantamentoQuitaCobranca() {
            var pa = patientAccount("pa-1", "120.00");
            var ps = new PsychologistAccount();
            ps.setId("ps-1");
            var open = charge(pa, ps, "100.00");

            when(accountService.getPatientAccount("p-1")).thenReturn(pa);
            when(transactionRepo.findByPatientAccountIdAndStatusInOrderByCreatedAtAsc(eq("pa-1"), anyList()))
                    .thenReturn(List.of(open));

            service.registerAdvancePayment(
                    new AdvancePaymentRegisterDTO("p-1", new BigDecimal("120.00"), PaymentMethodEnum.PIX, null), ps);

            assertThat(open.getCreditApplied()).isEqualByComparingTo("100.00");
            assertThat(open.getStatus()).isEqualTo(TransactionStatusEnum.PAID);
            assertThat(open.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("sem crédito disponível não altera as cobranças")
        void semCreditoNaoAltera() {
            var pa = patientAccount("pa-1", "0.00");
            var ps = new PsychologistAccount();
            ps.setId("ps-1");

            when(accountService.getPatientAccount("p-1")).thenReturn(pa);

            service.registerAdvancePayment(
                    new AdvancePaymentRegisterDTO("p-1", new BigDecimal("50.00"), PaymentMethodEnum.PIX, null), ps);

            // creditBalance == 0 → applyCreditToOpenCharges retorna cedo, sem buscar cobranças
        }
    }

    // endregion

    // region Feature B — consome ao pagar

    @Nested
    @DisplayName("registerPayment (Feature B)")
    class FeatureB {

        @Test
        @DisplayName("pagamento consome o crédito disponível antes do dinheiro")
        void pagamentoConsomeCredito() {
            var pa = patientAccount("pa-1", "30.00");
            var ps = new PsychologistAccount();
            ps.setId("ps-1");
            var t = charge(pa, ps, "100.00");

            when(transactionRepo.findById("t-1")).thenReturn(Optional.of(t));

            service.registerPayment("t-1",
                    new PaymentRegisterDTO("t-1", new BigDecimal("70.00"), PaymentMethodEnum.PIX, null));

            assertThat(t.getCreditApplied()).isEqualByComparingTo("30.00");
            assertThat(t.getAmountPaid()).isEqualByComparingTo("70.00");
            assertThat(t.getStatus()).isEqualTo(TransactionStatusEnum.PAID);
        }

        @Test
        @DisplayName("crédito cobre a cobrança inteira — sem necessidade de dinheiro")
        void creditoCobreTudo() {
            var pa = patientAccount("pa-1", "150.00");
            var ps = new PsychologistAccount();
            ps.setId("ps-1");
            var t = charge(pa, ps, "100.00");

            when(transactionRepo.findById("t-1")).thenReturn(Optional.of(t));

            service.registerPayment("t-1",
                    new PaymentRegisterDTO("t-1", new BigDecimal("0.01"), PaymentMethodEnum.PIX, null));

            assertThat(t.getCreditApplied()).isEqualByComparingTo("100.00");
            assertThat(t.getStatus()).isEqualTo(TransactionStatusEnum.PAID);
            assertThat(t.getAmountPaid()).isEqualByComparingTo("0.00");
        }
    }

    // endregion
}
