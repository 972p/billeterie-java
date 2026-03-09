import database.MySQLConnection;
import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Client ORDER BY id_client DESC LIMIT 5");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id_client") +
                        ", Email: " + rs.getString("email") +
                        ", Pass: " + rs.getString("mot_de_passe") +
                        ", Role: " + rs.getString("p_role"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
