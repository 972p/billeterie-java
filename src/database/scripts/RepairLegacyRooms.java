package database.scripts;

import database.MySQLConnection;
import java.sql.*;

public class RepairLegacyRooms {
    public static void main(String[] args) {
        System.out.println("Starting legacy room repair script...");
        
        try (Connection conn = MySQLConnection.connect()) {
            if (conn == null) return;

            // 1. Identify rooms with 0 places in the Place table
            String findSql = "SELECT s.id_salle, s.nom, s.nb_rangees, s.nb_colonnes " +
                             "FROM Salle s " +
                             "LEFT JOIN Place p ON s.id_salle = p.id_salle " +
                             "WHERE p.id_place IS NULL";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(findSql);
            
            PreparedStatement psPlace = conn.prepareStatement(
                "INSERT INTO Place (id_salle, rangee, numero, statut) VALUES (?, ?, ?, 1)");
            PreparedStatement psUpdateCount = conn.prepareStatement(
                "UPDATE Salle SET nb_places = ? WHERE id_salle = ?");

            int repairedCount = 0;
            while (rs.next()) {
                int idSalle = rs.getInt("id_salle");
                String nom = rs.getString("nom");
                int rows = rs.getInt("nb_rangees");
                int cols = rs.getInt("nb_colonnes");
                
                System.out.println("Repairing room: " + nom + " (ID:" + idSalle + ") - Generating " + (rows*cols) + " seats...");
                
                for (int r = 1; r <= rows; r++) {
                    for (int c = 1; c <= cols; c++) {
                        psPlace.setInt(1, idSalle);
                        psPlace.setInt(2, r);
                        psPlace.setInt(3, c);
                        psPlace.addBatch();
                    }
                }
                psPlace.executeBatch();
                
                // Update nb_places column
                psUpdateCount.setInt(1, rows * cols);
                psUpdateCount.setInt(2, idSalle);
                psUpdateCount.executeUpdate();
                
                repairedCount++;
            }
            
            System.out.println("Repair complete. Rooms repaired: " + repairedCount);

        } catch (SQLException e) {
            System.err.println("Repair failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
