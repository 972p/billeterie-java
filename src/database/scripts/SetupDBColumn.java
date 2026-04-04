package database.scripts;

import database.MySQLConnection;
import java.sql.*;

public class SetupDBColumn {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE Client ADD COLUMN photo_profil VARCHAR(500) DEFAULT NULL");
                    System.out.println("✅ Colonne 'photo_profil' ajoutée avec succès à la table Client.");
                } catch (Exception e) {
                    System.out.println("⚠️ La colonne 'photo_profil' existe peut-être déjà : " + e.getMessage());
                }
            } else {
                System.out.println("❌ Erreur de connexion à la BDD.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
