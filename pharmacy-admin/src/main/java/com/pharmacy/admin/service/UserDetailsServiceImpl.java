package com.pharmacy.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Value("${auth.datasource.url}")
    private String authDbUrl;

    @Value("${auth.datasource.username}")
    private String authDbUsername;

    @Value("${auth.datasource.password}")
    private String authDbPassword;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String sql = "SELECT email, password, role FROM users WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String userEmail = rs.getString("email");
                String password = rs.getString("password");
                String role = rs.getString("role");

                return new User(
                    userEmail,
                    password,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
            } else {
                throw new UsernameNotFoundException("User not found: " + email);
            }
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error: " + e.getMessage());
        }
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            authDbUrl, authDbUsername, authDbPassword);
    }
}
