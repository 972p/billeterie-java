package DAO;
import java.sql.*;

import database.DatabaseConfig;
import database.MySQLConnection;
import models.Tarif;

public class TarifDAO {
    public void ajouter(Tarif obj) throws SQLException {
        String sql = "INSERT INTO Tarif (id_evenement, libelle, prix) VALUES (?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_evenement());
        ps.setObject(2, obj.getLibelle());
        ps.setObject(3, obj.getPrix());
        ps.executeUpdate();
    }

    public void modifier(Tarif obj) throws SQLException {
        String sql = "UPDATE Tarif SET id_evenement=?, libelle=?, prix=? WHERE id_tarif=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_evenement());
        ps.setObject(2, obj.getLibelle());
        ps.setObject(3, obj.getPrix());
        ps.setInt(4, obj.getId_tarif());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Tarif WHERE id_tarif = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
