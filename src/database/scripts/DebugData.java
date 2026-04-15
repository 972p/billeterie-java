package database.scripts;

import database.MySQLConnection;
import java.sql.*;

public class DebugData {
    public static void main(String[] args) {
        try (Connection conn = MySQLConnection.connect()) {
            System.out.println("--- SALLES ---");
            ResultSet rsS = conn.createStatement().executeQuery("SELECT id_salle, nom, nb_rangees, nb_colonnes, nb_places FROM Salle");
            while (rsS.next()) {
                System.out.println("ID:" + rsS.getInt(1) + " | Nom:" + rsS.getString(2) + " | Size:" + rsS.getInt(3) + "x" + rsS.getInt(4) + " | Places:" + rsS.getInt(5));
            }

            System.out.println("\n--- SEANCES ---");
            ResultSet rsE = conn.createStatement().executeQuery("SELECT id_seance, id_evenement, id_salle FROM Seance");
            while (rsE.next()) {
                System.out.println("SeanceID:" + rsE.getInt(1) + " | EventID:" + rsE.getInt(2) + " | SalleID:" + rsE.getInt(3));
            }

            System.out.println("\n--- PLACES (Stats) ---");
            ResultSet rsP = conn.createStatement().executeQuery("SELECT id_salle, COUNT(*) FROM Place GROUP BY id_salle");
            while (rsP.next()) {
                System.out.println("SalleID:" + rsP.getInt(1) + " | Count:" + rsP.getInt(2));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
