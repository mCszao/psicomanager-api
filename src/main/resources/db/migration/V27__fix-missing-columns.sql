-- V27: Adiciona colunas que existem nas entidades JPA mas faltam nas migrations originais.

-- patients: birthday_date (existe no Patient.java mas nunca foi adicionada por migration)
ALTER TABLE `patients`
    ADD COLUMN `birthday_date` date DEFAULT NULL;

-- patients: is_active (estava na V2 como bit mas alguns ambientes podem ter divergência)
-- Já existe — apenas garantindo que birthday_date foi o único gap.

-- alerts: organization_id já foi adicionado na V25, mas created_at precisa existir
-- (criado na V15, já deve estar correto — só garantindo birthday_date acima)
