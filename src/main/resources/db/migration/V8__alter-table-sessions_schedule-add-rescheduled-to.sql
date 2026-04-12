ALTER TABLE `sessions_schedule`
    ADD COLUMN `RESCHEDULED_TO` varchar(255) NULL AFTER `TYPE`,
    ADD CONSTRAINT `fk_sessions_schedule_rescheduled_to` FOREIGN KEY (`RESCHEDULED_TO`) REFERENCES `sessions_schedule` (`ID`);
