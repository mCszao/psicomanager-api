CREATE TABLE `sessions_schedule` (
  `ID` varchar(255) NOT NULL,
  `PATIENT` varchar(255) NOT NULL,
  `DATE_START` datetime NOT NULL,
  `DATE_END` datetime NOT NULL,
  `ANNOTATIONS` text DEFAULT NULL,
  `STAGE` enum('CONCLUDED','OPENED','CANCELLED','RESCHEDULED') DEFAULT 'OPENED',
  PRIMARY KEY (`ID`),
  KEY `fk_sessions_schedule_id_patient` (`PATIENT`),
  CONSTRAINT `fk_sessions_schedule_id_patient` FOREIGN KEY (`PATIENT`) REFERENCES `patients` (`ID`)
);
