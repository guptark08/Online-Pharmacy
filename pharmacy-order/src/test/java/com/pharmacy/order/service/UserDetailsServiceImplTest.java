package com.pharmacy.order.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    @Spy
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameReturnsUser() throws Exception {
        doReturn(connection).when(userDetailsService).getConnection();
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("email")).thenReturn("user@example.com");
        when(resultSet.getString("password")).thenReturn("secret");
        when(resultSet.getString("role")).thenReturn("CUSTOMER");

        UserDetails details = userDetailsService.loadUserByUsername("user@example.com");

        assertEquals("user@example.com", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() throws Exception {
        doReturn(connection).when(userDetailsService).getConnection();
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("missing@example.com"));
    }

    @Test
    void loadUserByUsernameWrapsConnectionFailure() throws Exception {
        doThrow(new SQLException("down")).when(userDetailsService).getConnection();

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("user@example.com"));

        assertTrue(ex.getMessage().contains("Error loading user"));
    }
}
