package DAO;

import java.sql.*;

import database.DatabaseConfig;
import database.MySQLConnection;
import models.Seance;

public class SeanceDAO {
    public void ajouter(Seance obj) throws SQLException {
        String sql = "INSERT INTO Seance (id_evenement, id_lieu, id_salle, date_heure) VALUES (?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_evenement());
        ps.setObject(2, obj.getId_lieu());
        ps.setObject(3, obj.getId_salle());
        ps.setObject(4, obj.getDate_heure());
        ps.executeUpdate();
    }

    public void modifier(Seance obj) throws SQLException {
        String sql = "UPDATE Seance SET id_evenement=?, id_lieu=?, id_salle=?, date_heure=? WHERE id_seance=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_evenement());
        ps.setObject(2, obj.getId_lieu());
        ps.setObject(3, obj.getId_salle());
        ps.setObject(4, obj.getDate_heure());
        ps.setObject(5, obj.getId_seance());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Seance WHERE id_seance = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
