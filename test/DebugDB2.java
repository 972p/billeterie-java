import java.sql.*;
import database.MySQLConnection;

public class DebugDB2 {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            ResultSet rs = conn.createStatement().executeQuery("DESCRIBE Billet");
            while(rs.next()) {
                System.out.println(rs.getString("Field") + " | " + rs.getString("Type"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
