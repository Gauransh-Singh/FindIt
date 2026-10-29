CREATE DATABASE IF NOT EXISTS findit_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE findit_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    report_type ENUM('LOST', 'FOUND') NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    description TEXT,
    color VARCHAR(60),
    location VARCHAR(200) NOT NULL,
    item_date DATE NOT NULL,
    image_path VARCHAR(255),
    status ENUM('OPEN', 'MATCHED', 'RESOLVED') DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE IF NOT EXISTS claims (
    claim_id INT PRIMARY KEY AUTO_INCREMENT,
    item_id INT NOT NULL,
    claimant_id INT NOT NULL,
    verification_answer TEXT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (claimant_id) REFERENCES users(user_id)
);

INSERT IGNORE INTO categories (category_name) VALUES
('Electronics'), ('Wallets'), ('ID Cards'), ('Keys'), ('Bags'), ('Books'), ('Clothing'), ('Other');
