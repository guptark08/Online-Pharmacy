INSERT INTO users (
    id, name, email, mobile, password, address, role, is_active, created_at, updated_at
) VALUES
    (1, 'Admin User', 'admin@pharmacy.local', '9000000001', '$2a$10$k5zrwq7MHMc.k74mghCNpOhOzOb2MZLje8mVp4sxE7erREHPULEaC', 'Head Office, Kolkata', 'ADMIN', true, '2026-03-20 09:00:00', '2026-03-20 09:00:00'),
    (2, 'Aarav Sharma', 'customer1@pharmacy.local', '9000000002', '$2a$10$k5zrwq7MHMc.k74mghCNpOhOzOb2MZLje8mVp4sxE7erREHPULEaC', '221 Park Street, Kolkata', 'CUSTOMER', true, '2026-03-20 09:05:00', '2026-03-20 09:05:00'),
    (3, 'Diya Verma', 'customer2@pharmacy.local', '9000000003', '$2a$10$k5zrwq7MHMc.k74mghCNpOhOzOb2MZLje8mVp4sxE7erREHPULEaC', '14 MG Road, Bengaluru', 'CUSTOMER', true, '2026-03-20 09:10:00', '2026-03-20 09:10:00'),
    (4, 'Riya Singh', 'pharmacist@pharmacy.local', '9000000004', '$2a$10$k5zrwq7MHMc.k74mghCNpOhOzOb2MZLje8mVp4sxE7erREHPULEaC', 'Warehouse 2, Kolkata', 'PHARMACIST', true, '2026-03-20 09:15:00', '2026-03-20 09:15:00'),
    (5, 'Kabir Das', 'delivery@pharmacy.local', '9000000005', '$2a$10$k5zrwq7MHMc.k74mghCNpOhOzOb2MZLje8mVp4sxE7erREHPULEaC', 'Delivery Hub, Kolkata', 'DELIVERY_AGENT', true, '2026-03-20 09:20:00', '2026-03-20 09:20:00')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    email = VALUES(email),
    mobile = VALUES(mobile),
    password = VALUES(password),
    address = VALUES(address),
    role = VALUES(role),
    is_active = VALUES(is_active),
    updated_at = VALUES(updated_at);
