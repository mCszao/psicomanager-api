CREATE TABLE `patient_accounts` (
  `ID`             varchar(255)   NOT NULL,
  `patient_id`     varchar(255)   NOT NULL,
  `balance`        decimal(12,2)  NOT NULL DEFAULT 0.00
                   COMMENT 'Saldo devedor atual do paciente (soma das transações pendentes)',
  `credit_balance` decimal(12,2)  NOT NULL DEFAULT 0.00
                   COMMENT 'Crédito disponível para abater futuras cobranças (adiantamentos)',
  `created_at`     datetime       NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uq_patient_accounts_patient_id` (`patient_id`),
  CONSTRAINT `fk_patient_accounts_patient_id`
    FOREIGN KEY (`patient_id`) REFERENCES `patients` (`ID`)
);
