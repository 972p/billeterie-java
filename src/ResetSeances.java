import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.MySQLConnection;

public class ResetSeances {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            if (conn == null) {
                System.out.println("Connection failed.");
                return;
            }
            
            // 1. Delete all Billets and Seances
            conn.createStatement().executeUpdate("DELETE FROM Billet");
            conn.createStatement().executeUpdate("DELETE FROM Seance");
            System.out.println("Billets and Seances deleted.");

            // 2. Fetch first Salle and its Lieu
            int idSalle = -1;
            int idLieu = -1;
            ResultSet rsSalle = conn.createStatement().executeQuery("SELECT id_salle, id_lieu FROM Salle LIMIT 1");
            if (rsSalle.next()) {
                idSalle = rsSalle.getInt("id_salle");
                idLieu = rsSalle.getInt("id_lieu");
            } else {
                System.out.println("No Salle found in DB. Cannot create Seances.");
                return;
            }

            // 3. Fetch all Evenements
            ResultSet rsEv = conn.createStatement().executeQuery("SELECT id_evenement FROM Evenement");

            // 4. Create a new Seance for each Evenement
            PreparedStatement psInsert = conn.prepareStatement(
                "INSERT INTO Seance (id_evenement, id_lieu, id_salle, date_heure) VALUES (?, ?, ?, ?)"
            );

            int count = 0;
            int day = 10;
            int month = 4;
            while (rsEv.next()) {
                if (day > 30) {
                    day = 1;
                    month++;
                }
                int idEv = rsEv.getInt("id_evenement");
                psInsert.setInt(1, idEv);
                psInsert.setInt(2, idLieu);
                psInsert.setInt(3, idSalle);
                psInsert.setString(4, "2026-0" + month + "-" + (day < 10 ? "0"+day : day) + " 20:30:00");
                psInsert.executeUpdate();
                count++;
                day++;
            }
            System.out.println("Successfully created " + count + " new Seances starting from April 10th, 2026!");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
