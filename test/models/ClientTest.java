package models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    @Test
    public void testClientConstructorWithId() {
        Client client = new Client(1, "John Doe", "john@example.com", "0123456789", "123 Rue de la Paix", "password123", "USER", "photo.png");
        assertEquals(1, client.getId());
        assertEquals("John Doe", client.getNom());
        assertEquals("john@example.com", client.getEmail());
        assertEquals("0123456789", client.getTelephone());
        assertEquals("123 Rue de la Paix", client.getAdresse());
        assertEquals("password123", client.getMotDePasse());
        assertEquals("USER", client.getRole());
        assertEquals("photo.png", client.getPhotoProfil());
    }

    @Test
    public void testClientSetters() {
        Client client = new Client("Jane Doe", "jane@example.com", "9876543210", "456 Avenue", "pass", "ADMIN");
        client.setNom("Jane Smith");
        client.setRole("USER");
        
        assertEquals("Jane Smith", client.getNom());
        assertEquals("USER", client.getRole());
        assertNull(client.getPhotoProfil());
    }
}
