-- Rastreamento granular de liquidação por transação.
-- amount_paid    : parte do `amount` quitada com dinheiro (pagamentos via /financial/payments)
-- credit_applied : parte do `amount` quitada com crédito de adiantamento (liquidação automática)
-- O valor em aberto de uma cobrança é amount - amount_paid - credit_applied.
ALTER TABLE `financial_transactions`
  ADD COLUMN `amount_paid`    decimal(12,2) NOT NULL DEFAULT 0.00
    COMMENT 'Parte do amount quitada em dinheiro' AFTER `amount`,
  ADD COLUMN `credit_applied` decimal(12,2) NOT NULL DEFAULT 0.00
    COMMENT 'Parte do amount quitada com crédito de adiantamento' AFTER `amount_paid`;

-- Backfill: cobranças já PAID antes deste rastreamento são assumidas como
-- totalmente quitadas em dinheiro, exceto as liquidadas automaticamente por crédito
-- (identificáveis pela nota gerada em applyAutomaticCredit).
UPDATE `financial_transactions`
SET `amount_paid` = `amount`
WHERE `status` = 'PAID'
  AND (`notes` IS NULL OR `notes` NOT LIKE '%crédito%');

UPDATE `financial_transactions`
SET `credit_applied` = `amount`
WHERE `status` = 'PAID'
  AND `notes` LIKE '%crédito%';
