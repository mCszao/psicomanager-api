-- Corrige o tipo da coluna TYPE na tabela documents:
-- a entidade Document.java espera VARCHAR(100) mas a migration V6 criou CHAR(100).
ALTER TABLE `documents`
    MODIFY COLUMN `TYPE` VARCHAR(100) NOT NULL;
