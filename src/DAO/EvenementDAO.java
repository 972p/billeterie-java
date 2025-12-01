package DAO;

import database.MySQLConnection;
import models.Evenement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementDAO {

    public List<Evenement> selectAll() throws SQLException {
        String sql = "SELECT * FROM Evenement";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Evenement> evenements = new ArrayList<>();

        while (rs.next()) {
            evenements.add(mapRow(rs));
        }

        rs.close();
        ps.close();
        conn.close();
        return evenements;
    }

    // utilisé par ton controller
    public List<Evenement> findPaginated(int page, int limit) throws SQLException {
        int offset = (page - 1) * limit;
        String sql = "SELECT * FROM Evenement LIMIT ? OFFSET ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, limit);
        ps.setInt(2, offset);
        ResultSet rs = ps.executeQuery();

        List<Evenement> evenements = new ArrayList<>();
        while (rs.next()) {
            evenements.add(mapRow(rs));
        }

        rs.close();
        ps.close();
        conn.close();
        return evenements;
    }

    public void ajouter(Evenement obj) throws SQLException {
        String sql = "INSERT INTO Evenement (titre, description_courte, description_longue, duree, langue, age_min) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, obj.getTitre());
        ps.setString(2, obj.getDescriptionCourte());
        ps.setString(3, obj.getDescriptionLongue());
        ps.setInt(4, obj.getDuree());
        ps.setString(5, obj.getLangue());
        ps.setInt(6, obj.getAgeMin());
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    public void modifier(int id, Evenement obj) throws SQLException {
        String sql = "UPDATE Evenement SET titre=?, description_courte=?, description_longue=?, duree=?, langue=?, age_min=? " +
                "WHERE id_evenement=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, obj.getTitre());
        ps.setString(2, obj.getDescriptionCourte());
        ps.setString(3, obj.getDescriptionLongue());
        ps.setInt(4, obj.getDuree());
        ps.setString(5, obj.getLangue());
        ps.setInt(6, obj.getAgeMin());
        ps.setInt(7, id);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Evenement WHERE id_evenement = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    // méthode privée pour construire l'objet Evenement
    private Evenement mapRow(ResultSet rs) throws SQLException {
        return new Evenement(
                rs.getString("titre"),
                rs.getString("description_courte"),
                rs.getString("description_longue"),
                rs.getInt("duree"),
                rs.getString("langue"),
                rs.getInt("age_min")
        );
    }
}
