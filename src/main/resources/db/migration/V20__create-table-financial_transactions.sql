CREATE TABLE `financial_transactions` (
  `ID`                       varchar(255)   NOT NULL,
  `patient_account_id`       varchar(255)   NOT NULL,
  `psychologist_account_id`  varchar(255)   NOT NULL,
  `plan_id`                  varchar(255)   DEFAULT NULL
                             COMMENT 'Preenchido apenas para transações do tipo PLAN_CHARGE',
  `session_id`               varchar(255)   DEFAULT NULL
                             COMMENT 'Preenchido apenas para transações do tipo SESSION_CHARGE',
  `type`                     enum(
                               'SESSION_CHARGE',
                               'PLAN_CHARGE',
                               'ADVANCE_PAYMENT',
                               'PAYMENT',
                               'CREDIT_ADJUSTMENT',
                               'REFUND'
                             ) NOT NULL,
  `amount`                   decimal(12,2)  NOT NULL,
  `due_date`                 date           DEFAULT NULL
                             COMMENT 'Data de vencimento. Nulo para pagamentos e adiantamentos.',
  `paid_at`                  datetime       DEFAULT NULL
                             COMMENT 'Preenchido quando status muda para PAID',
  `payment_method`           enum(
                               'PIX',
                               'CARD',
                               'CASH',
                               'TRANSFER'
                             ) DEFAULT NULL
                             COMMENT 'Preenchido apenas quando há pagamento efetivo',
  `status`                   enum(
                               'PENDING',
                               'PAID',
                               'OVERDUE',
                               'PARTIALLY_PAID',
                               'CANCELLED',
                               'ADVANCE'
                             ) NOT NULL DEFAULT 'PENDING',
  `notes`                    text           DEFAULT NULL,
  `created_at`               datetime       NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `idx_ft_patient_account`      (`patient_account_id`),
  KEY `idx_ft_psychologist_account` (`psychologist_account_id`),
  KEY `idx_ft_plan`                 (`plan_id`),
  KEY `idx_ft_session`              (`session_id`),
  KEY `idx_ft_status`               (`status`),
  KEY `idx_ft_due_date`             (`due_date`),
  CONSTRAINT `fk_ft_patient_account`
    FOREIGN KEY (`patient_account_id`)      REFERENCES `patient_accounts` (`ID`),
  CONSTRAINT `fk_ft_psychologist_account`
    FOREIGN KEY (`psychologist_account_id`) REFERENCES `psychologist_accounts` (`ID`),
  CONSTRAINT `fk_ft_plan`
    FOREIGN KEY (`plan_id`)                REFERENCES `plans` (`ID`),
  CONSTRAINT `fk_ft_session`
    FOREIGN KEY (`session_id`)             REFERENCES `sessions_schedule` (`ID`)
);
