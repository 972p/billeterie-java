import java.sql.*;
import database.MySQLConnection;

public class DebugDB3 {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            ResultSet rs = conn.createStatement().executeQuery("DESCRIBE Client");
            while(rs.next()) {
                System.out.println(rs.getString("Field") + " | " + rs.getString("Type"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
