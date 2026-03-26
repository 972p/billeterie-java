package database;

import java.sql.Connection;
import java.sql.Statement;

public class SetupDatabase {

    public static void main(String[] args) {
        System.out.println("Début de la mise à jour de la base de données...");

        try (Connection conn = MySQLConnection.connect()) {
            if (conn == null) {
                System.out.println("❌ Erreur : Impossible de se connecter à la base de données.");
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                // 1. Ajouter la colonne mot_de_passe si elle n'existe pas
                try {
                    stmt.executeUpdate("ALTER TABLE Client ADD COLUMN mot_de_passe VARCHAR(255) DEFAULT 'password'");
                    System.out.println("✅ Colonne 'mot_de_passe' ajoutée.");
                } catch (Exception e) {
                    System.out.println("⚠️ La colonne 'mot_de_passe' existe peut-être déjà.");
                }

                // 2. Ajouter la colonne p_role si elle n'existe pas
                try {
                    stmt.executeUpdate("ALTER TABLE Client ADD COLUMN p_role ENUM('USER', 'ADMIN') DEFAULT 'USER'");
                    System.out.println("✅ Colonne 'p_role' ajoutée.");
                } catch (Exception e) {
                    System.out.println("⚠️ La colonne 'p_role' existe peut-être déjà.");
                }

                // 3. Créer un administrateur par défaut (admin@favelas.eu / admin)
                try {
                    String hashedAdmin = org.mindrot.jbcrypt.BCrypt.hashpw("admin",
                            org.mindrot.jbcrypt.BCrypt.gensalt());
                    String sqlAdmin = "INSERT INTO Client (nom, email, telephone, adresse, mot_de_passe, p_role) " +
                            "VALUES ('Admin', 'admin@favelas.eu', '0000000000', 'Admin HQ', '" + hashedAdmin
                            + "', 'ADMIN')";
                    stmt.executeUpdate(sqlAdmin);
                    System.out
                            .println("✅ Compte administrateur créé avec succès (email: admin@favelas.eu, mdp: admin).");
                } catch (Exception e) {
                    System.out.println("⚠️ Administrateur déjà existant ou erreur lors de la création.");
                }

                // 4. Créer la table Salle
                try {
                    String sqlSalle = "CREATE TABLE IF NOT EXISTS Salle (" +
                            "id_salle INT AUTO_INCREMENT PRIMARY KEY, " +
                            "id_lieu INT NOT NULL, " +
                            "nom VARCHAR(100) NOT NULL, " +
                            "nb_rangees INT NOT NULL, " +
                            "nb_colonnes INT NOT NULL" +
                            ")";
                    stmt.executeUpdate(sqlSalle);
                    System.out.println("✅ Table 'Salle' créée ou déjà existante.");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 5. Créer la table Place
                try {
                    String sqlPlace = "CREATE TABLE IF NOT EXISTS Place (" +
                            "id_place INT AUTO_INCREMENT PRIMARY KEY, " +
                            "id_salle INT NOT NULL, " +
                            "rangee INT NOT NULL, " +
                            "numero INT NOT NULL" +
                            ")";
                    stmt.executeUpdate(sqlPlace);
                    System.out.println("✅ Table 'Place' créée ou déjà existante.");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 6. Ajouter colonne id_place à Billet
                try {
                    stmt.executeUpdate("ALTER TABLE Billet ADD COLUMN id_place INT");
                    System.out.println("✅ Colonne 'id_place' ajoutée à Billet.");
                } catch (Exception e) {
                    System.out.println("⚠️ La colonne 'id_place' existe peut-être déjà dans Billet.");
                }

                // 6.5. Ajouter colonne categorie à Evenement
                try {
                    stmt.executeUpdate("ALTER TABLE Evenement ADD COLUMN categorie VARCHAR(50) DEFAULT 'Autre'");
                    System.out.println("✅ Colonne 'categorie' ajoutée à Evenement.");
                } catch (Exception e) {
                    System.out.println("⚠️ La colonne 'categorie' existe peut-être déjà dans Evenement.");
                }

                // 7. Générer des Lieux, Salles et Places avec différentes configurations
                try {
                    java.sql.ResultSet check = stmt
                            .executeQuery("SELECT COUNT(*) FROM Salle WHERE nom = 'Salle Panoramique'");
                    check.next();
                    if (check.getInt(1) == 0) {
                        System.out.println("Génération des salles avec différentes formes...");

                        // 1. Lieux
                        stmt.executeUpdate(
                                "INSERT INTO Lieu (nom, adresse, ville, capacite) VALUES ('Le Grand Rex', '1 Bd Poissonnière', 'Paris', 1000)");
                        stmt.executeUpdate(
                                "INSERT INTO Lieu (nom, adresse, ville, capacite) VALUES ('Théâtre Antique', '1 Rue Romaine', 'Lyon', 500)");

                        java.sql.ResultSet rsL1 = stmt.executeQuery(
                                "SELECT id_lieu FROM Lieu WHERE nom = 'Le Grand Rex' ORDER BY id_lieu DESC LIMIT 1");
                        rsL1.next();
                        int grandRexId = rsL1.getInt(1);

                        java.sql.ResultSet rsL2 = stmt.executeQuery(
                                "SELECT id_lieu FROM Lieu WHERE nom = 'Théâtre Antique' ORDER BY id_lieu DESC LIMIT 1");
                        rsL2.next();
                        int theatreId = rsL2.getInt(1);

                        // 2. Salles de différentes formes
                        java.sql.PreparedStatement psSalle = conn.prepareStatement(
                                "INSERT INTO Salle (id_lieu, nom, nb_rangees, nb_colonnes) VALUES (?, ?, ?, ?) ",
                                Statement.RETURN_GENERATED_KEYS);

                        Object[][] salles = {
                                { grandRexId, "Salle Carrée Max", 15, 15 },
                                { grandRexId, "Salle Panoramique", 5, 25 }, // Très large, peu profonde
                                { theatreId, "Salle Profonde", 25, 6 }, // Très profonde, peu large
                                { theatreId, "Petite Salle VIP", 4, 4 }, // Toute petite
                                { grandRexId, "Salle Standard", 8, 12 }
                        };

                        java.sql.PreparedStatement psPlace = conn
                                .prepareStatement("INSERT INTO Place (id_salle, rangee, numero) VALUES (?, ?, ?)");

                        int totalPlaces = 0;
                        for (Object[] config : salles) {
                            psSalle.setInt(1, (Integer) config[0]);
                            psSalle.setString(2, (String) config[1]);
                            int rangees = (Integer) config[2];
                            int colonnes = (Integer) config[3];
                            psSalle.setInt(3, rangees);
                            psSalle.setInt(4, colonnes);
                            psSalle.executeUpdate();

                            java.sql.ResultSet rsSalleId = psSalle.getGeneratedKeys();
                            rsSalleId.next();
                            int newSalleId = rsSalleId.getInt(1);

                            for (int r = 1; r <= rangees; r++) {
                                for (int c = 1; c <= colonnes; c++) {
                                    psPlace.setInt(1, newSalleId);
                                    psPlace.setInt(2, r);
                                    psPlace.setInt(3, (r - 1) * colonnes + c);
                                    psPlace.addBatch();
                                    totalPlaces++;
                                }
                            }
                        }
                        psPlace.executeBatch();
                        System.out.println("✅ " + salles.length
                                + " salles de différentes formes générées avec succès ! (" + totalPlaces + " places)");
                    } else {
                        System.out.println("ℹ️ Les salles de démonstration existent déjà.");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Erreur lors de la génération des lieux/salles.");
                    e.printStackTrace();
                }

                System.out.println("🎉 Mise à jour terminée ! Vous pouvez maintenant lancer l'application.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
