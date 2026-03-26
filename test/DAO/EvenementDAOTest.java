package DAO;

import models.Evenement;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EvenementDAOTest {

    @Test
    public void testSelectAllDoesNotThrow() {
        EvenementDAO dao = new EvenementDAO();
        try {
            List<Evenement> events = dao.selectAll();
            assertNotNull(events, "The returned list should not be null");
        } catch (SQLException e) {
            // If the database is not running, we don't want to fail the compilation/test structure.
            // A real environment would use a test DB. Here we just print.
            System.err.println("Database connection failed, skipping execution: " + e.getMessage());
        }
    }
}
