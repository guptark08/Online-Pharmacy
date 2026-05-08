package com.pharmacy.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private Long medicineId;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    private boolean requiresPrescription;

    public CartItem() {}

    public Long getId() { return id; }
    public Cart getCart() { return cart; }
    public Long getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public boolean isRequiresPrescription() { return requiresPrescription; }

    public void setId(Long id) { this.id = id; }
    public void setCart(Cart cart) { this.cart = cart; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
}