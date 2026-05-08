package com.pharmacy.order.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userEmail;

    @OneToMany(mappedBy = "cart",
        cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Cart() {}

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public List<CartItem> getItems() { return items; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setItems(List<CartItem> items) { this.items = items; }
}