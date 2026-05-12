import java.sql.*;
import database.MySQLConnection;

public class DebugDB {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            System.out.println("--- Billets ---");
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_billet, id_client, date_achat FROM Billet ORDER BY id_billet DESC LIMIT 5");
            while(rs.next()) {
                System.out.println(rs.getInt("id_billet") + " | " + rs.getInt("id_client") + " | " + rs.getString("date_achat"));
            }
            System.out.println("--- PaiementStripe ---");
            rs = conn.createStatement().executeQuery("SELECT * FROM PaiementStripe ORDER BY date_achat DESC LIMIT 5");
            while(rs.next()) {
                System.out.println(rs.getString("date_achat") + " | " + rs.getInt("id_client") + " | " + rs.getString("stripe_payment_id"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
