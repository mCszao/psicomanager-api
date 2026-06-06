-- Adiciona organization_id em todas as tabelas de dados do sistema

ALTER TABLE `patients`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual este paciente pertence';
ALTER TABLE `patients`
  ADD CONSTRAINT `fk_patients_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `patients`
  ADD KEY `idx_patients_organization` (`organization_id`);

ALTER TABLE `sessions_schedule`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual esta sessão pertence';
ALTER TABLE `sessions_schedule`
  ADD CONSTRAINT `fk_sessions_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `sessions_schedule`
  ADD KEY `idx_sessions_organization` (`organization_id`);

ALTER TABLE `plans`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual este plano pertence';
ALTER TABLE `plans`
  ADD CONSTRAINT `fk_plans_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `plans`
  ADD KEY `idx_plans_organization` (`organization_id`);

ALTER TABLE `plan_templates`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual este template pertence';
ALTER TABLE `plan_templates`
  ADD CONSTRAINT `fk_plan_templates_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `plan_templates`
  ADD KEY `idx_plan_templates_organization` (`organization_id`);

ALTER TABLE `alerts`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual este aviso pertence';
ALTER TABLE `alerts`
  ADD CONSTRAINT `fk_alerts_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `alerts`
  ADD KEY `idx_alerts_organization` (`organization_id`);

ALTER TABLE `documents`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual este documento pertence';
ALTER TABLE `documents`
  ADD CONSTRAINT `fk_documents_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `documents`
  ADD KEY `idx_documents_organization` (`organization_id`);

ALTER TABLE `patient_accounts`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual esta conta de paciente pertence';
ALTER TABLE `patient_accounts`
  ADD CONSTRAINT `fk_patient_accounts_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `patient_accounts`
  ADD KEY `idx_patient_accounts_organization` (`organization_id`);

ALTER TABLE `psychologist_accounts`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual esta conta de psicólogo pertence';
ALTER TABLE `psychologist_accounts`
  ADD CONSTRAINT `fk_psychologist_accounts_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `psychologist_accounts`
  ADD KEY `idx_psychologist_accounts_organization` (`organization_id`);

ALTER TABLE `financial_transactions`
  ADD COLUMN `organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Tenant: organização à qual esta transação pertence';
ALTER TABLE `financial_transactions`
  ADD CONSTRAINT `fk_financial_transactions_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`);
ALTER TABLE `financial_transactions`
  ADD KEY `idx_financial_transactions_organization` (`organization_id`);
