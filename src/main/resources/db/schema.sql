CREATE DATABASE IF NOT EXISTS sharenest_db;
USE sharenest_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BIT NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    location VARCHAR(180) NOT NULL,
    image_url VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    category_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS borrow_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    message VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_requests_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_requests_borrower FOREIGN KEY (borrower_id) REFERENCES users(id)
);
