CREATE TABLE `organizations` (
  `ID`          varchar(255) NOT NULL,
  `NAME`        varchar(255) NOT NULL,
  `SLUG`        varchar(255) NOT NULL
                COMMENT 'Identificador amigável da organização, único no sistema',
  `CREATED_AT`  datetime     NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uq_organizations_slug` (`SLUG`)
);
