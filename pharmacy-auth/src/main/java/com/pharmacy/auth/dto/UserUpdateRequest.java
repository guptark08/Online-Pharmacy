package com.pharmacy.auth.dto;

import com.pharmacy.auth.enums.Role;

public class UserUpdateRequest {
    private Role role;
    private Boolean active;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
