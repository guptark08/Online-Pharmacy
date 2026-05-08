INSERT INTO categories (
    id, name, description, image_url, is_active, created_at
) VALUES
    (101, 'Pain Relief', 'Tablets and syrups for headaches, fever, and body pain.', '/images/categories/pain-relief.png', true, '2026-03-20 10:00:00'),
    (102, 'Diabetes Care', 'Daily medicines and monitoring support for diabetes.', '/images/categories/diabetes-care.png', true, '2026-03-20 10:05:00'),
    (103, 'Vitamins & Supplements', 'Immunity, wellness, and nutritional support products.', '/images/categories/vitamins.png', true, '2026-03-20 10:10:00'),
    (104, 'Prescription Medicines', 'Medicines that require a valid doctor prescription.', '/images/categories/prescription.png', true, '2026-03-20 10:15:00')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    image_url = VALUES(image_url),
    is_active = VALUES(is_active),
    created_at = VALUES(created_at);

INSERT INTO medicines (
    id, name, description, category_id, manufacturer, price, stock, requires_prescription,
    image_url, dosage_info, side_effects, is_active, created_at, updated_at
) VALUES
    (1001, 'Paracetamol 650', 'Fast relief for fever, cold symptoms, and body pain.', 101, 'MediCare Labs', 35.00, 120, false, '/images/medicines/paracetamol-650.png', '1 tablet after meals, up to 3 times a day.', 'Rare nausea or rash.', true, '2026-03-20 11:00:00', '2026-03-20 11:00:00'),
    (1002, 'Vitamin C 500', 'Daily immunity support tablets.', 103, 'Wellness Pharma', 199.00, 45, false, '/images/medicines/vitamin-c-500.png', '1 tablet daily after breakfast.', 'Mild stomach upset in sensitive patients.', true, '2026-03-20 11:05:00', '2026-03-20 11:05:00'),
    (1003, 'Metformin 500', 'Prescription medicine used for blood sugar management.', 102, 'HealthBridge', 149.50, 28, true, '/images/medicines/metformin-500.png', '1 tablet twice daily with food.', 'Possible nausea, bloating, or diarrhea.', true, '2026-03-20 11:10:00', '2026-03-20 11:10:00'),
    (1004, 'Amoxicillin 500', 'Antibiotic capsules for bacterial infections.', 104, 'CarePlus Pharma', 249.00, 8, true, '/images/medicines/amoxicillin-500.png', 'As prescribed by the doctor.', 'Possible allergy, nausea, or loose stools.', true, '2026-03-20 11:15:00', '2026-03-20 11:15:00')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    category_id = VALUES(category_id),
    manufacturer = VALUES(manufacturer),
    price = VALUES(price),
    stock = VALUES(stock),
    requires_prescription = VALUES(requires_prescription),
    image_url = VALUES(image_url),
    dosage_info = VALUES(dosage_info),
    side_effects = VALUES(side_effects),
    is_active = VALUES(is_active),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO inventory_batches (
    id, medicine_id, batch_number, quantity, expiry_date, manufacture_date, added_at
) VALUES
    (1101, 1001, 'PCM650-2401', 120, '2027-12-31', '2025-12-15', '2026-03-20 11:30:00'),
    (1102, 1002, 'VITC-2402', 45, '2027-10-30', '2025-11-20', '2026-03-20 11:35:00'),
    (1103, 1003, 'MET500-2403', 28, '2027-08-31', '2025-10-10', '2026-03-20 11:40:00'),
    (1104, 1004, 'AMX500-2404', 8, '2026-11-30', '2025-09-12', '2026-03-20 11:45:00')
ON DUPLICATE KEY UPDATE
    medicine_id = VALUES(medicine_id),
    batch_number = VALUES(batch_number),
    quantity = VALUES(quantity),
    expiry_date = VALUES(expiry_date),
    manufacture_date = VALUES(manufacture_date),
    added_at = VALUES(added_at);

INSERT INTO prescriptions (
    id, user_id, order_id, file_url, file_name, file_type, status, uploaded_at, reviewed_at, review_note
) VALUES
    (9001, 554854428, 5003, '/uploads/prescriptions/rx-customer2-pending.pdf', 'rx-customer2-pending.pdf', 'application/pdf', 'PENDING', '2026-03-24 09:00:00', NULL, NULL),
    (9002, 554854428, 5004, '/uploads/prescriptions/rx-customer2-approved.pdf', 'rx-customer2-approved.pdf', 'application/pdf', 'APPROVED', '2026-03-23 10:15:00', '2026-03-23 12:00:00', 'Approved after pharmacist review.'),
    (9003, 1065388605, NULL, '/uploads/prescriptions/rx-customer1-rejected.pdf', 'rx-customer1-rejected.pdf', 'application/pdf', 'REJECTED', '2026-03-22 08:45:00', '2026-03-22 09:30:00', 'Prescription image was unclear.')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    order_id = VALUES(order_id),
    file_url = VALUES(file_url),
    file_name = VALUES(file_name),
    file_type = VALUES(file_type),
    status = VALUES(status),
    uploaded_at = VALUES(uploaded_at),
    reviewed_at = VALUES(reviewed_at),
    review_note = VALUES(review_note);
