ALTER TABLE `sessions_schedule`
  ADD COLUMN `plan_id` varchar(255) DEFAULT NULL,
  ADD CONSTRAINT `fk_sessions_schedule_plan_id` FOREIGN KEY (`plan_id`) REFERENCES `plans` (`ID`);
