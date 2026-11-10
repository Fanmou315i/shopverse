-- ShopVerse MySQL 8 schema. H2 is used automatically for the local demo.
CREATE DATABASE IF NOT EXISTS shopverse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE shopverse;

CREATE TABLE IF NOT EXISTS consumed_events (
  event_id VARCHAR(200) PRIMARY KEY,
  consumed_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS account_data (
  id BIGINT PRIMARY KEY,
  content LONGTEXT
);

CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  category VARCHAR(40),
  description VARCHAR(255),
  emoji VARCHAR(16),
  rating INT,
  sold_count INT
);

CREATE TABLE cart_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(120) NOT NULL,
  emoji VARCHAR(16),
  unit_price DECIMAL(10,2) NOT NULL,
  variant VARCHAR(255),
  quantity INT NOT NULL,
  selected BIT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_cart_product (product_id, variant)
);

CREATE TABLE chat_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender VARCHAR(40) NOT NULL,
  conversation_id VARCHAR(255),
  content VARCHAR(1000) NOT NULL,
  sent_at DATETIME NOT NULL
);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL UNIQUE,
  items_json LONGTEXT,
  total DECIMAL(10,2) NOT NULL,
  item_count INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  purchase_channel VARCHAR(20),
  coupon_code VARCHAR(60),
  payment_method VARCHAR(40),
  receiver_name VARCHAR(80),
  receiver_phone VARCHAR(40),
  receiver_address VARCHAR(300),
  created_at DATETIME NOT NULL
);

CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);

CREATE TABLE outbox_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id VARCHAR(36) NOT NULL UNIQUE,
  event_type VARCHAR(60) NOT NULL,
  payload LONGTEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  next_attempt_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  published_at DATETIME NULL,
  INDEX idx_outbox_ready (status, next_attempt_at),
  INDEX idx_outbox_type (event_type, created_at)
);
