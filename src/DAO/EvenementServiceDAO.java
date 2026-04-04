package DAO;

import database.MySQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EvenementServiceDAO {

    public void lierServiceAEvenement(int evenementId, int serviceId) throws SQLException {
        String sql = "INSERT IGNORE INTO Evenement_Service (id_evenement, id_service) VALUES (?, ?)";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            ps.setInt(2, serviceId);
            ps.executeUpdate();
        }
    }

    public void supprimerLiaisonsPourEvenement(int evenementId) throws SQLException {
        String sql = "DELETE FROM Evenement_Service WHERE id_evenement = ?";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            ps.executeUpdate();
        }
    }

    public List<Integer> recupererIdServicesPourEvenement(int evenementId) throws SQLException {
        List<Integer> servicesIds = new ArrayList<>();
        String sql = "SELECT id_service FROM Evenement_Service WHERE id_evenement = ?";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    servicesIds.add(rs.getInt("id_service"));
                }
            }
        }
        return servicesIds;
    }

    public void mettreAJourEtat(int evenementId, int serviceId, String etat) throws SQLException {
        String sql = "UPDATE Evenement_Service SET etat = ? WHERE id_evenement = ? AND id_service = ?";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, etat);
            ps.setInt(2, evenementId);
            ps.setInt(3, serviceId);
            ps.executeUpdate();
        }
    }
}
