CREATE DATABASE IF NOT EXISTS `api_fintech` DEFAULT CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE `api_fintech`;

CREATE TABLE IF NOT EXISTS `client` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(255) DEFAULT NULL,
  `last_name_or_company_name` VARCHAR(255) DEFAULT NULL,
  `document_number` VARCHAR(255) DEFAULT NULL,
  `address` VARCHAR(255) DEFAULT NULL,
  `phone_number` VARCHAR(50) DEFAULT NULL,
  `email` VARCHAR(255) DEFAULT NULL,
  `user_type` VARCHAR(50) DEFAULT NULL,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `outstanding_balance` NUMERIC(15, 2) DEFAULT 0,
  `registration_date` DATETIME NULL,
  `modification_date` DATETIME NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `account` (
  `account_id` BIGINT NOT NULL AUTO_INCREMENT,
  `client_id` BIGINT NOT NULL,
  `account_number` VARCHAR(100) DEFAULT NULL,
  `currency` VARCHAR(10) DEFAULT NULL,
  `balance` NUMERIC(15, 2) DEFAULT 0,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` DATETIME NULL,
  `updated_at` DATETIME NULL,
  PRIMARY KEY (`account_id`),
  INDEX `idx_account_client` (`client_id`),
  CONSTRAINT `fk_account_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar dos clientes
INSERT INTO `client` (`first_name`, `last_name_or_company_name`, `document_number`, `address`, `phone_number`, `email`, `user_type`, `active`, `outstanding_balance`, `registration_date`, `modification_date`)
VALUES
('Juan', 'Pérez', '12345678', 'Calle Principal 123', '1123456789', 'juan.perez@email.com', 'INDIVIDUAL', TRUE, 0, NOW(), NOW()),
('María', 'García López', '87654321', 'Avenida Secundaria 456', '1187654321', 'maria.garcia@email.com', 'INDIVIDUAL', TRUE, 0, NOW(), NOW());

-- Insertar cuatro cuentas (dos por cliente)
INSERT INTO `account` (`client_id`, `account_number`, `currency`, `balance`, `active`, `created_at`, `updated_at`)
VALUES
(1, '1001-001-ARS', 'ARS', 50000.00, TRUE, NOW(), NOW()),
(1, '1001-002-USD', 'USD', 1000.00, TRUE, NOW(), NOW()),
(2, '1002-001-ARS', 'ARS', 75000.00, TRUE, NOW(), NOW()),
(2, '1002-002-USD', 'USD', 2500.00, TRUE, NOW(), NOW());

