INSERT INTO addresses (
    id, user_email, full_name, mobile, address_line1, address_line2, city, state, pincode, is_default, created_at
) VALUES
    (2001, 'customer1@pharmacy.local', 'Aarav Sharma', '9000000002', '221 Park Street', 'Near Metro Gate 3', 'Kolkata', 'West Bengal', '700016', true, '2026-03-20 12:00:00'),
    (2002, 'customer2@pharmacy.local', 'Diya Verma', '9000000003', '14 MG Road', 'Opposite Trinity Circle', 'Bengaluru', 'Karnataka', '560001', true, '2026-03-20 12:05:00')
ON DUPLICATE KEY UPDATE
    user_email = VALUES(user_email),
    full_name = VALUES(full_name),
    mobile = VALUES(mobile),
    address_line1 = VALUES(address_line1),
    address_line2 = VALUES(address_line2),
    city = VALUES(city),
    state = VALUES(state),
    pincode = VALUES(pincode),
    is_default = VALUES(is_default),
    created_at = VALUES(created_at);

INSERT INTO carts (
    id, user_email, updated_at
) VALUES
    (3001, 'customer1@pharmacy.local', '2026-03-25 08:00:00'),
    (3002, 'customer2@pharmacy.local', '2026-03-25 08:10:00')
ON DUPLICATE KEY UPDATE
    user_email = VALUES(user_email),
    updated_at = VALUES(updated_at);

INSERT INTO cart_items (
    id, cart_id, medicine_id, medicine_name, price, quantity, requires_prescription
) VALUES
    (4001, 3001, 1001, 'Paracetamol 650', 35.00, 2, false),
    (4002, 3001, 1002, 'Vitamin C 500', 199.00, 1, false),
    (4003, 3002, 1003, 'Metformin 500', 149.50, 1, true)
ON DUPLICATE KEY UPDATE
    cart_id = VALUES(cart_id),
    medicine_id = VALUES(medicine_id),
    medicine_name = VALUES(medicine_name),
    price = VALUES(price),
    quantity = VALUES(quantity),
    requires_prescription = VALUES(requires_prescription);

INSERT INTO orders (
    id, user_email, address_id, prescription_id, status, total_amount, delivery_slot, created_at, updated_at
) VALUES
    (5001, 'customer1@pharmacy.local', 2001, NULL, 'DELIVERED', 70.00, '9AM-11AM', '2026-03-20 13:00:00', '2026-03-21 18:00:00'),
    (5002, 'customer1@pharmacy.local', 2001, NULL, 'PAID', 199.00, '6PM-8PM', '2026-03-21 14:00:00', '2026-03-21 14:20:00'),
    (5003, 'customer2@pharmacy.local', 2002, 9001, 'PRESCRIPTION_PENDING', 249.00, '10AM-12PM', '2026-03-24 10:00:00', '2026-03-24 10:00:00'),
    (5004, 'customer2@pharmacy.local', 2002, 9002, 'PAYMENT_PENDING', 149.50, '2PM-4PM', '2026-03-23 11:00:00', '2026-03-23 12:10:00'),
    (5005, 'customer1@pharmacy.local', 2001, NULL, 'PACKED', 269.00, '4PM-6PM', '2026-03-25 09:00:00', '2026-03-25 11:30:00')
ON DUPLICATE KEY UPDATE
    user_email = VALUES(user_email),
    address_id = VALUES(address_id),
    prescription_id = VALUES(prescription_id),
    status = VALUES(status),
    total_amount = VALUES(total_amount),
    delivery_slot = VALUES(delivery_slot),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO order_items (
    id, order_id, medicine_id, medicine_name, price, quantity, subtotal
) VALUES
    (6001, 5001, 1001, 'Paracetamol 650', 35.00, 2, 70.00),
    (6002, 5002, 1002, 'Vitamin C 500', 199.00, 1, 199.00),
    (6003, 5003, 1004, 'Amoxicillin 500', 249.00, 1, 249.00),
    (6004, 5004, 1003, 'Metformin 500', 149.50, 1, 149.50),
    (6005, 5005, 1001, 'Paracetamol 650', 35.00, 2, 70.00),
    (6006, 5005, 1002, 'Vitamin C 500', 199.00, 1, 199.00)
ON DUPLICATE KEY UPDATE
    order_id = VALUES(order_id),
    medicine_id = VALUES(medicine_id),
    medicine_name = VALUES(medicine_name),
    price = VALUES(price),
    quantity = VALUES(quantity),
    subtotal = VALUES(subtotal);

INSERT INTO payments (
    id, order_id, user_id, amount, status, transaction_id, payment_method, created_at
) VALUES
    (7001, 5002, 2, 199.00, 'PAID', 'TXN-5002-PAID', 'UPI', '2026-03-21 14:20:00'),
    (7002, 5001, 2, 70.00, 'PAID', 'TXN-5001-PAID', 'CARD', '2026-03-20 13:10:00')
ON DUPLICATE KEY UPDATE
    order_id = VALUES(order_id),
    user_id = VALUES(user_id),
    amount = VALUES(amount),
    status = VALUES(status),
    transaction_id = VALUES(transaction_id),
    payment_method = VALUES(payment_method),
    created_at = VALUES(created_at);
