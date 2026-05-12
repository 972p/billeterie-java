import java.sql.*;
import database.MySQLConnection;

public class FixDB {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            conn.createStatement().executeUpdate("ALTER TABLE Billet MODIFY COLUMN date_achat DATETIME");
            System.out.println("Billet.date_achat changed to DATETIME");
            
            conn.createStatement().executeUpdate("ALTER TABLE PaiementStripe MODIFY COLUMN date_achat DATETIME");
            System.out.println("PaiementStripe.date_achat changed to DATETIME");
            
            // Fix existing mismatched data for today
            conn.createStatement().executeUpdate("UPDATE Billet SET date_achat = '2026-04-29 11:05:57' WHERE date_achat = '2026-04-29 00:00:00' AND id_client = 180");
            System.out.println("Data fixed.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
