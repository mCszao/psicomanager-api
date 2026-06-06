-- Adiciona active_organization_id em users para rastrear qual org está ativa na sessão
ALTER TABLE `users`
  ADD COLUMN `active_organization_id` varchar(255) DEFAULT NULL
  COMMENT 'Organização ativa no momento — usada como contexto tenant da sessão';

ALTER TABLE `users`
  ADD CONSTRAINT `fk_users_active_organization`
    FOREIGN KEY (`active_organization_id`) REFERENCES `organizations` (`ID`);
