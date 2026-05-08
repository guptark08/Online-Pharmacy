package com.pharmacy.auth.dto;

import com.pharmacy.auth.enums.Role;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private String address;
    private Role role;

    public AuthResponse() {}

    // Getters
    public String getToken() { 
    	return token;
    	}
    public String getTokenType() {
    	return tokenType; 
    	}
    public Long getUserId() { 
    	return userId; 
    	}
    public String getName() { 
    	return name; 
    	}
    public String getEmail() { 
    	return email; 
    	}
    public String getAddress() {
		return address;
	}

    public Role getRole() {
    	return role; 
    	}

    // Setters
    public void setToken(String token) { 
    	this.token = token; 
    	}
    public void setTokenType(String tokenType) 
    { 
    	this.tokenType = tokenType; 
    	}
    public void setUserId(Long userId) {
    	this.userId = userId;
    	}
    public void setName(String name) { 
    	this.name = name; 
    	}
    public void setEmail(String email) {
    	this.email = email; 
    	}
    public void setRole(Role role) {
    	this.role = role;
    	}

	
	public void setAddress(String address) {
		this.address = address;
	}
}