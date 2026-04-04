package database.scripts;

import database.MySQLConnection;
import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            ResultSet rs = conn.createStatement().executeQuery("DESCRIBE Evenement");
            while (rs.next()) {
                System.out.println(rs.getString("Field") + " - " + rs.getString("Type"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
