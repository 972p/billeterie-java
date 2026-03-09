package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import database.MySQLConnection;
import org.mindrot.jbcrypt.BCrypt;

public class UpdateAdminPassword {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            if (conn != null) {
                String hashedAdmin = BCrypt.hashpw("admin", BCrypt.gensalt());
                String sql = "UPDATE Client SET mot_de_passe = ? WHERE email = 'admin@favelas.eu'";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, hashedAdmin);
                    int rows = ps.executeUpdate();
                    System.out.println("Admin password updated successfully. Rows affected: " + rows);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
