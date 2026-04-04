package database.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
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

            // Fix for corrupted empty tables (no email column)
            try {
                java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Prestataire");
                if (rs.next() && rs.getInt(1) == 0) {
                     stmt.executeUpdate("DROP TABLE IF EXISTS Evenement_Service");
                     stmt.executeUpdate("DROP TABLE IF EXISTS Service");
                     stmt.executeUpdate("DROP TABLE IF EXISTS Prestataire");
                     System.out.println("Cleaned up empty Prestataire tables to fix schema issues.");
                }
            } catch(Exception e) {}

            // Create Prestataire table
            try {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Prestataire (" +
                        "id_prestataire INT AUTO_INCREMENT PRIMARY KEY, " +
                        "nom VARCHAR(100) NOT NULL, " +
                        "specialite VARCHAR(100), " +
                        "contact VARCHAR(100), " +
                        "email VARCHAR(100) UNIQUE, " +
                        "mot_de_passe VARCHAR(255)" +
                        ")");
                System.out.println("Created/Verified 'Prestataire' table.");

                // Check and rename 'login' to 'email' if the column was already created as 'login'
                try {
                    stmt.executeUpdate("ALTER TABLE Prestataire CHANGE COLUMN login email VARCHAR(100) UNIQUE");
                    System.out.println("Renamed 'login' to 'email' in 'Prestataire' table.");
                } catch (Exception e) {
                    try {
                        stmt.executeUpdate("ALTER TABLE Prestataire ADD COLUMN email VARCHAR(100) UNIQUE");
                        System.out.println("Added 'email' column to 'Prestataire' table.");
                    } catch (Exception ex) {}
                }

                // Seed test prestataires
                try {
                    String hashedPw = org.mindrot.jbcrypt.BCrypt.hashpw("password123", org.mindrot.jbcrypt.BCrypt.gensalt());
                    stmt.executeUpdate("INSERT IGNORE INTO Prestataire (nom, specialite, contact, email, mot_de_passe) VALUES ('Les Délices de Paris', 'Traiteur', '0600000000', 'contact@delicesparis.fr', '" + hashedPw + "')");
                    stmt.executeUpdate("INSERT IGNORE INTO Prestataire (nom, specialite, contact, email, mot_de_passe) VALUES ('SecurGuard', 'Sécurité', '0600000001', 'secu@securguard.eu', '" + hashedPw + "')");
                    stmt.executeUpdate("INSERT IGNORE INTO Prestataire (nom, specialite, contact, email, mot_de_passe) VALUES ('DJ Sono Pro', 'Technique & Son', '0600000002', 'djsono@pro-events.com', '" + hashedPw + "')");
                    System.out.println("Inserted/Verified seed data for 'Prestataire'.");
                } catch (Exception e) {}
            } catch (Exception e) {
                System.out.println("Error creating 'Prestataire' table: " + e.getMessage());
            }

            // Create Service table
            try {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Service (" +
                        "id_service INT AUTO_INCREMENT PRIMARY KEY, " +
                        "id_prestataire INT NOT NULL, " +
                        "nom VARCHAR(100) NOT NULL, " +
                        "description TEXT, " +
                        "FOREIGN KEY (id_prestataire) REFERENCES Prestataire(id_prestataire) ON DELETE CASCADE" +
                        ")");
                System.out.println("Created/Verified 'Service' table.");

                // Seed test services based on the pre-filled prestataires
                try {
                    stmt.executeUpdate("INSERT IGNORE INTO Service (id_prestataire, nom, description) SELECT id_prestataire, 'Buffet Froid', 'Service de nuit' FROM Prestataire WHERE email='contact@delicesparis.fr'");
                    stmt.executeUpdate("INSERT IGNORE INTO Service (id_prestataire, nom, description) SELECT id_prestataire, 'Gardiennage', 'Gardiens d\\'entrée' FROM Prestataire WHERE email='secu@securguard.eu'");
                    stmt.executeUpdate("INSERT IGNORE INTO Service (id_prestataire, nom, description) SELECT id_prestataire, 'Sonorisation Standard', 'Pour les mariages' FROM Prestataire WHERE email='djsono@pro-events.com'");
                } catch (Exception e) {}
            } catch (Exception e) {
                System.out.println("Error creating 'Service' table: " + e.getMessage());
            }

            // Modify Evenement to include id_prestataire (and force it to be NULLable just in case it was created NOT NULL)
            try {
                stmt.executeUpdate("ALTER TABLE Evenement MODIFY COLUMN id_prestataire INT NULL");
                System.out.println("Modified 'id_prestataire' to be strictly NULLable.");
            } catch (Exception e) {}

            try {
                stmt.executeUpdate("ALTER TABLE Evenement ADD COLUMN id_prestataire INT NULL");
            } catch (Exception e) {}

            try {
                stmt.executeUpdate("ALTER TABLE Evenement ADD CONSTRAINT fk_evenement_prestataire FOREIGN KEY (id_prestataire) REFERENCES Prestataire(id_prestataire) ON DELETE SET NULL");
                System.out.println("Added 'id_prestataire' and foreign key to Evenement table.");
            } catch (Exception e) {
                System.out.println("Column 'id_prestataire' FK already exists or error: " + e.getMessage());
            }

            // Create Evenement_Service table
            try {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Evenement_Service (" +
                        "id_evenement INT NOT NULL, " +
                        "id_service INT NOT NULL, " +
                        "etat VARCHAR(20) DEFAULT 'EN_ATTENTE', " +
                        "PRIMARY KEY (id_evenement, id_service), " +
                        "FOREIGN KEY (id_evenement) REFERENCES Evenement(id_evenement) ON DELETE CASCADE, " +
                        "FOREIGN KEY (id_service) REFERENCES Service(id_service) ON DELETE CASCADE" +
                        ")");
                System.out.println("Created/Verified 'Evenement_Service' table.");

                // Add etat column if the table already existed without it
                try {
                    stmt.executeUpdate("ALTER TABLE Evenement_Service ADD COLUMN etat VARCHAR(20) DEFAULT 'EN_ATTENTE'");
                    System.out.println("Added 'etat' column to 'Evenement_Service' table.");
                } catch (Exception e) {}

                // Seed some mapping only if table is empty
                try {
                    ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM Evenement_Service");
                    if (countRs.next() && countRs.getInt(1) == 0) {
                         // Assign Traiteur (Les Délices de Paris) to all existing events
                         stmt.executeUpdate("INSERT IGNORE INTO Evenement_Service (id_evenement, id_service, etat) " +
                                            "SELECT e.id_evenement, s.id_service, 'EN_ATTENTE' FROM Evenement e " +
                                            "JOIN Service s ON s.nom = 'Buffet Froid'");
                         
                         // Assign Security (SecurGuard) to all existing events
                         stmt.executeUpdate("INSERT IGNORE INTO Evenement_Service (id_evenement, id_service, etat) " +
                                            "SELECT e.id_evenement, s.id_service, 'EN_ATTENTE' FROM Evenement e " +
                                            "JOIN Service s ON s.nom = 'Gardiennage'");
                         System.out.println("Seeded test associations between Events and Services.");
                    }
                } catch (Exception e) {}
            } catch (Exception e) {
                System.out.println("Error creating 'Evenement_Service' table: " + e.getMessage());
            }

            System.out.println("Schema updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
