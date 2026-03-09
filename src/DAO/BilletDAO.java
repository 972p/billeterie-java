package DAO;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

import database.MySQLConnection;
import models.Billet;
import models.BilletDisplay;

public class BilletDAO {
    public void ajouter(Billet obj) throws SQLException {
        String sql = "INSERT INTO Billet (id_seance, id_client, id_tarif, id_place, statut, date_achat) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_seance());
        ps.setObject(2, obj.getId_client());
        ps.setObject(3, obj.getId_tarif());
        ps.setObject(4, obj.getId_place());
        ps.setObject(5, obj.getStatut());
        ps.setObject(6, obj.getDate_achat());
        ps.executeUpdate();
    }

    public void modifier(Billet obj) throws SQLException {
        String sql = "UPDATE Billet SET id_seance=?, id_client=?, id_tarif=?, id_place=?, statut=?, date_achat=? WHERE id_billet=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, obj.getId_seance());
        ps.setObject(2, obj.getId_client());
        ps.setObject(3, obj.getId_tarif());
        ps.setObject(4, obj.getId_place());
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

    public List<BilletDisplay> getBilletsDetailedByClient(int clientId) throws SQLException {
        List<BilletDisplay> billets = new ArrayList<>();
        String sql = "SELECT b.id_billet, e.titre AS nom_evenement, " +
                "DATE(s.date_heure) AS date_seance, TIME(s.date_heure) AS heure_seance, " +
                "sa.nom AS nom_salle, l.nom AS nom_lieu, " +
                "p.rangee, p.numero, t.prix, b.statut, b.date_achat " +
                "FROM Billet b " +
                "JOIN Seance s ON b.id_seance = s.id_seance " +
                "JOIN Evenement e ON s.id_evenement = e.id_evenement " +
                "LEFT JOIN Salle sa ON s.id_salle = sa.id_salle " +
                "LEFT JOIN Lieu l ON s.id_lieu = l.id_lieu " +
                "LEFT JOIN Place p ON b.id_place = p.id_place " +
                "JOIN Tarif t ON b.id_tarif = t.id_tarif " +
                "WHERE b.id_client = ?";

        try (Connection conn = MySQLConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rangee = rs.getString("rangee");
                    String numero = rs.getString("numero");

                    BilletDisplay bd = new BilletDisplay(
                            rs.getInt("id_billet"),
                            rs.getString("nom_evenement"),
                            rs.getString("date_seance"),
                            rs.getString("heure_seance"),
                            rs.getString("nom_salle"),
                            rs.getString("nom_lieu"),
                            rangee != null ? rangee : "-",
                            numero != null ? numero : "-",
                            rs.getDouble("prix"),
                            rs.getString("statut"),
                            rs.getString("date_achat"));
                    billets.add(bd);
                }
            }
        }
        return billets;
    }
}
