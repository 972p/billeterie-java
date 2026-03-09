package DAO;

import database.MySQLConnection;
import models.Salle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {

    public void ajouter(Salle obj) throws SQLException {
        String sql = "INSERT INTO Salle (id_lieu, nom, nb_rangees, nb_colonnes) VALUES (?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setInt(1, obj.getId_lieu());
        ps.setString(2, obj.getNom());
        ps.setInt(3, obj.getNb_rangees());
        ps.setInt(4, obj.getNb_colonnes());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            obj.setId_salle(rs.getInt(1));
        }
    }

    public void modifier(Salle obj) throws SQLException {
        String sql = "UPDATE Salle SET id_lieu=?, nom=?, nb_rangees=?, nb_colonnes=? WHERE id_salle=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, obj.getId_lieu());
        ps.setString(2, obj.getNom());
        ps.setInt(3, obj.getNb_rangees());
        ps.setInt(4, obj.getNb_colonnes());
        ps.setInt(5, obj.getId_salle());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Salle WHERE id_salle = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Salle> trouverParLieu(int id_lieu) throws SQLException {
        List<Salle> liste = new ArrayList<>();
        String sql = "SELECT * FROM Salle WHERE id_lieu = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id_lieu);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            liste.add(new Salle(
                    rs.getInt("id_salle"),
                    rs.getInt("id_lieu"),
                    rs.getString("nom"),
                    rs.getInt("nb_rangees"),
                    rs.getInt("nb_colonnes")));
        }
        return liste;
    }
}
