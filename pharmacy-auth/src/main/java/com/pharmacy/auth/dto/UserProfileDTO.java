package com.pharmacy.auth.dto;

import com.pharmacy.auth.enums.Role;
import java.time.LocalDateTime;

public class UserProfileDTO {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String address;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfileDTO() {}

    // Getters
    public Long getId() { 
    	return id; 
    	}
    public String getName() {
    	return name; 
    	}
    public String getEmail() { 
    	return email; 
    	}
    public String getMobile() {
    	return mobile; 
    	}
    public String getAddrsss() {
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
    public LocalDateTime getUpdatedAt() {
    	return updatedAt; 
    	}
    

    // Setters
    public void setId(Long id) { 
    	this.id = id;
    	}
    public void setName(String name) {
    	this.name = name;
    	}
    public void setEmail(String email) {
    	this.email = email; 
    	}
    public void setMobile(String mobile) { 
    	this.mobile = mobile;
    	}
    public void setAddress(String addrsss) {
		this.address = addrsss;
	    }
    public void setRole(Role role) {
    	this.role = role;
    	}
    public void setActive(boolean isActive) { 
    	this.isActive = isActive;
    	}
    public void setCreatedAt(LocalDateTime createdAt) { 
    	this.createdAt = createdAt; 
    	}
    public void setUpdatedAt(LocalDateTime updatedAt) { 
    	this.updatedAt = updatedAt; 
    	}
}