-- Create databases for each service
CREATE DATABASE IF NOT EXISTS pharmacy_auth_db;
CREATE DATABASE IF NOT EXISTS pharmacy_catalog_db;
CREATE DATABASE IF NOT EXISTS pharmacy_order_db;
CREATE DATABASE IF NOT EXISTS pharmacy_admin_db;
CREATE DATABASE IF NOT EXISTS zipkin;

-- Create user for each service
CREATE USER IF NOT EXISTS 'pharmacy'@'%' IDENTIFIED BY 'pharmacy';
CREATE USER IF NOT EXISTS 'zipkin'@'%' IDENTIFIED BY 'zipkin';
GRANT ALL PRIVILEGES ON pharmacy_auth_db.* TO 'pharmacy'@'%';
GRANT ALL PRIVILEGES ON pharmacy_catalog_db.* TO 'pharmacy'@'%';
GRANT ALL PRIVILEGES ON pharmacy_order_db.* TO 'pharmacy'@'%';
GRANT ALL PRIVILEGES ON pharmacy_admin_db.* TO 'pharmacy'@'%';
GRANT ALL PRIVILEGES ON zipkin.* TO 'zipkin'@'%';
FLUSH PRIVILEGES;
