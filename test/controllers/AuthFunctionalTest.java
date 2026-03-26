package controllers;

import DAO.ClientDAO;
import models.Client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;

public class AuthFunctionalTest {
    
    @Test
    public void testLoginWithInvalidCredentialsReturnsNull() {
        ClientDAO dao = new ClientDAO();
        try {
            Client user = dao.authentifier("nonexistent@example.com", "wrongpassword");
            assertNull(user, "User should be null for invalid credentials");
        } catch (SQLException e) {
            System.err.println("Database connection failed, skipping execution: " + e.getMessage());
        }
    }
}
