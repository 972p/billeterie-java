import java.sql.Connection;
import java.sql.Statement;
import database.MySQLConnection;

public class UpdateSchema {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Updating schema...");

            // Add solde to Client
            try {
                stmt.executeUpdate("ALTER TABLE Client ADD COLUMN solde DOUBLE DEFAULT 0.0");
                System.out.println("Added 'solde' to Client table.");
            } catch (Exception e) {
                System.out.println("Column 'solde' already exists or error: " + e.getMessage());
            }

            // Add affiche to Evenement
            try {
                stmt.executeUpdate("ALTER TABLE Evenement ADD COLUMN affiche VARCHAR(255)");
                System.out.println("Added 'affiche' to Evenement table.");
            } catch (Exception e) {
                System.out.println("Column 'affiche' already exists or error: " + e.getMessage());
            }

            // Add categorie to Evenement
            try {
                stmt.executeUpdate("ALTER TABLE Evenement ADD COLUMN categorie VARCHAR(50) DEFAULT 'Autre'");
                System.out.println("Added 'categorie' to Evenement table.");
            } catch (Exception e) {
                System.out.println("Column 'categorie' already exists or error: " + e.getMessage());
            }

            // Ensure statut exists in Billet (it seems to exist already based on models/Billet.java)
            try {
                stmt.executeUpdate("ALTER TABLE Billet ADD COLUMN statut VARCHAR(50) DEFAULT 'VALIDE'");
                System.out.println("Added 'statut' to Billet table.");
            } catch (Exception e) {
                System.out.println("Column 'statut' already exists or error: " + e.getMessage());
            }

            // Create CodePromo table and insert seed data
            try {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CodePromo (" +
                        "id_code_promo INT AUTO_INCREMENT PRIMARY KEY," +
                        "code VARCHAR(50) UNIQUE NOT NULL," +
                        "type_reduction VARCHAR(20) NOT NULL," +
                        "valeur_reduction DOUBLE NOT NULL," +
                        "statut VARCHAR(20) DEFAULT 'ACTIF')");
                System.out.println("Created/Verified 'CodePromo' table.");
                
                // Seed data (Ignore error if duplicates exist)
                try {
                    stmt.executeUpdate("INSERT INTO CodePromo (code, type_reduction, valeur_reduction) VALUES ('WELCOME10', 'POURCENTAGE', 10.0)");
                    stmt.executeUpdate("INSERT INTO CodePromo (code, type_reduction, valeur_reduction) VALUES ('MINUS5', 'FIXE', 5.0)");
                    System.out.println("Inserted seed data for 'CodePromo'.");
                } catch (Exception e) {}
            } catch (Exception e) {
                System.out.println("Error creating 'CodePromo' table: " + e.getMessage());
            }

            System.out.println("Schema updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
