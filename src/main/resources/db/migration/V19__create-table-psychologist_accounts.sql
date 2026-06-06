CREATE TABLE `psychologist_accounts` (
  `ID`               varchar(255)  NOT NULL,
  `user_id`          varchar(255)  NOT NULL,
  `total_receivable` decimal(12,2) NOT NULL DEFAULT 0.00
                     COMMENT 'Total a receber: soma de transações PENDING e OVERDUE',
  `total_received`   decimal(12,2) NOT NULL DEFAULT 0.00
                     COMMENT 'Total já recebido: soma de transações PAID',
  `created_at`       datetime      NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uq_psychologist_accounts_user_id` (`user_id`),
  CONSTRAINT `fk_psychologist_accounts_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`ID`)
);
