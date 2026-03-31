package DAO;

import database.MySQLConnection;
import models.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> trouverParPrestataire(int prestataireId) throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM Service WHERE id_prestataire = ? ORDER BY nom ASC";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, prestataireId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    services.add(new Service(
                            rs.getInt("id_service"),
                            rs.getInt("id_prestataire"),
                            rs.getString("nom"),
                            rs.getString("description")
                    ));
                }
            }
        }
        return services;
    }
}
