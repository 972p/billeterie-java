package database.scripts;

import database.MySQLConnection;
import java.sql.*;

public class FixSeatNumbers {
    public static void main(String[] args) {
        System.out.println("Starting seat number correction script...");
        
        String sql = "UPDATE Place p " +
                     "JOIN Salle s ON p.id_salle = s.id_salle " +
                     "SET p.numero = CASE " +
                     "  WHEN p.numero > s.nb_colonnes THEN p.numero - (p.rangee - 1) * s.nb_colonnes " +
                     "  ELSE p.numero " +
                     "END " +
                     "WHERE p.rangee > 1";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            int affectedRows = ps.executeUpdate();
            System.out.println("Migration complete. Affected rows: " + affectedRows);
            
            // Safety check: ensure no numero is 0 or negative
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM Place WHERE numero <= 0");
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("WARNING: " + rs.getInt(1) + " seats have invalid numbers after migration!");
                }
            }

        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
