-- Migration de dados: cria patient_accounts para todos os pacientes que ainda não têm conta
INSERT INTO `patient_accounts` (`ID`, `patient_id`, `balance`, `credit_balance`, `created_at`)
SELECT
    UUID(),
    p.`ID`,
    0.00,
    0.00,
    NOW()
FROM `patients` p
WHERE NOT EXISTS (
    SELECT 1 FROM `patient_accounts` pa WHERE pa.`patient_id` = p.`ID`
);

-- Migration de dados: cria psychologist_accounts para todos os usuários que ainda não têm conta
INSERT INTO `psychologist_accounts` (`ID`, `user_id`, `total_receivable`, `total_received`, `created_at`)
SELECT
    UUID(),
    u.`ID`,
    0.00,
    0.00,
    NOW()
FROM `users` u
WHERE NOT EXISTS (
    SELECT 1 FROM `psychologist_accounts` pa WHERE pa.`user_id` = u.`ID`
);
