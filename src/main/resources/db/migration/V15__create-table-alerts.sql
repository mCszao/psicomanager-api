CREATE TABLE `alerts` (
  `ID` varchar(255) NOT NULL,
  `patient_id` varchar(255) NOT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `type` enum('MANUAL','SYSTEM') NOT NULL DEFAULT 'MANUAL',
  `scope` enum('PATIENT','SESSION') NOT NULL,
  `message` text NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_alerts_patient_id` (`patient_id`),
  KEY `fk_alerts_session_id` (`session_id`),
  CONSTRAINT `fk_alerts_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`ID`),
  CONSTRAINT `fk_alerts_session_id` FOREIGN KEY (`session_id`) REFERENCES `sessions_schedule` (`ID`)
);
