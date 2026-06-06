CREATE TABLE `organization_members` (
  `ID`              varchar(255) NOT NULL,
  `organization_id` varchar(255) NOT NULL,
  `user_id`         varchar(255) NOT NULL,
  `role`            enum('OWNER','ADMIN','MEMBER') NOT NULL DEFAULT 'MEMBER',
  `joined_at`       datetime NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uq_org_members_org_user` (`organization_id`, `user_id`),
  KEY `idx_org_members_user` (`user_id`),
  CONSTRAINT `fk_org_members_organization`
    FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`ID`),
  CONSTRAINT `fk_org_members_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`ID`)
);
