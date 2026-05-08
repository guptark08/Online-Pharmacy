package com.pharmacy.auth.entity;

import com.pharmacy.auth.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String mobile;

    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
	private String address;

	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
	
    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Empty constructor
    public User() {}

    // Getters
    public Long getId() 
    { 
    	return id;
    	}
    public String getName()
    { 
    	return name;
    	}
    public String getEmail()
    { 
    	return email;
    	}
    public String getMobile()
    {
    	return mobile;
    	}
    public String getPassword()
    {
    	return password; 
    	}
    public String getAddress() {
		return address;
	}
    public Role getRole() {
    	return role;
    	}
    public boolean isActive() { 
    	return isActive;
    	}
    public LocalDateTime getCreatedAt() { 
    	return createdAt; 
    	}
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public void setPassword(String password) { this.password = password; }
    public void setAddress(String address) {
		this.address = address;
    }
   
    public void setRole(Role role) { this.role = role; }
    
    public void setActive(boolean isActive) { this.isActive = isActive; }
}