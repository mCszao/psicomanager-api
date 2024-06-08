CREATE TABLE `addresses` (
  `ID` varchar(255) NOT NULL,
  `STREET` varchar(100) DEFAULT NULL,
  `DISTRICT` varchar(100)DEFAULT NULL,
  `ZIPCODE` varchar(9) NOT NULL,
  `COMPLEMENT` varchar(100) DEFAULT NULL,
  `NUMBER` varchar(20) NULL,
  `STATE` varchar(100) NOT NULL DEFAULT 'Mato-Grosso',
  `ABBREVIATION` varchar(5) DEFAULT 'MT',
  `CITY` varchar(100) NOT NULL DEFAULT 'CUIABÁ',
  `PATIENT` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_address_id_patient` (`PATIENT`),
  CONSTRAINT `fk_address_id_patient` FOREIGN KEY (`PATIENT`) REFERENCES `patients` (`ID`)
);
