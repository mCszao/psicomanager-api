CREATE TABLE `patients` (
  `ID` varchar(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `EMAIL` varchar(100) DEFAULT NULL,
  `PHONE` varchar(100) NOT NULL,
  `CPF` varchar(11) NOT NULL,
  `IS_ACTIVE` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME` (`NAME`),
  UNIQUE KEY `PHONE` (`PHONE`),
  UNIQUE KEY `MAIL` (`EMAIL`),
  UNIQUE KEY `CPF` (`CPF`)
);
