package DAO;
import java.sql.*;

import database.MySQLConnection;
import models.Billet;

public class BilletDAO {
    public void ajouter(Billet obj) throws SQLException {
        String sql = "INSERT INTO Billet (id_seance, id_client, id_tarif, numero, statut, date_achat) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_seance());
        ps.setObject(2, obj.getId_client());
        ps.setObject(3, obj.getId_tarif());
        ps.setObject(4, obj.getNumero());
        ps.setObject(5, obj.getStatut());
        ps.setObject(6, obj.getDate_achat());
        ps.executeUpdate();
    }

    public void modifier(Billet obj) throws SQLException {
        String sql = "UPDATE Billet SET id_seance=?, id_client=?, id_tarif=?, numero=?, statut=?, date_achat=? WHERE id_billet=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_seance());
        ps.setObject(2, obj.getId_client());
        ps.setObject(3, obj.getId_tarif());
        ps.setObject(4, obj.getNumero());
        ps.setObject(5, obj.getStatut());
        ps.setObject(6, obj.getDate_achat());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Billet WHERE id_billet = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
