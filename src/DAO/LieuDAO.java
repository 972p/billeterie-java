package DAO;

import java.sql.*;

import database.DatabaseConfig;
import database.MySQLConnection;
import models.Lieu;

public class LieuDAO {
    public void ajouter(Lieu obj) throws SQLException {
        String sql = "INSERT INTO Lieu (nom, adresse, ville, capacite) VALUES (?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getNom());
        ps.setObject(2, obj.getAdresse());
        ps.setObject(3, obj.getVille());
        ps.setObject(4, obj.getCapacite());
        ps.executeUpdate();
    }

    public void modifier(Lieu obj) throws SQLException {
        String sql = "UPDATE Lieu SET nom=?, adresse=?, ville=?, capacite=? WHERE id_lieu=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getNom());
        ps.setObject(2, obj.getAdresse());
        ps.setObject(3, obj.getVille());
        ps.setObject(4, obj.getCapacite());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Lieu WHERE id_lieu = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public java.util.List<Lieu> trouverTous() throws SQLException {
        java.util.List<Lieu> liste = new java.util.ArrayList<>();
        String sql = "SELECT * FROM Lieu";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new Lieu(
                    rs.getInt("id_lieu"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getInt("capacite")));
        }
        return liste;
    }
}
