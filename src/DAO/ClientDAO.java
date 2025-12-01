package DAO;
import java.sql.*;

import database.DatabaseConfig;
import database.MySQLConnection;
import models.Client;

public class ClientDAO {
    public void ajouter(Client obj) throws SQLException {
        String sql = "INSERT INTO Client (nom, email, telephone, adresse) VALUES (?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getNom());
        ps.setObject(2, obj.getEmail());
        ps.setObject(3, obj.getTelephone());
        ps.setObject(4, obj.getAdresse());
        ps.executeUpdate();
    }

    public void modifier(Client obj) throws SQLException {
        String sql = "UPDATE Client SET nom=?, email=?, telephone=?, adresse=? WHERE id_client=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getNom());
        ps.setObject(2, obj.getEmail());
        ps.setObject(3, obj.getTelephone());
        ps.setObject(4, obj.getAdresse());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Client WHERE id_client = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
