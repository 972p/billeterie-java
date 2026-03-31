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
        String sql = "INSERT INTO Evenement " +
                "(titre, description_courte, description_longue, duree, langue, age_min, categorie, affiche, id_prestataire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, obj.getTitre());
        ps.setString(2, obj.getDescriptionCourte());
        ps.setString(3, obj.getDescriptionLongue());
        ps.setInt(4, obj.getDuree());
        ps.setString(5, obj.getLangue());
        ps.setInt(6, obj.getAgeMin());
        ps.setString(7, obj.getCategorie());
        ps.setString(8, obj.getAffiche());

        if (obj.getPrestataireId() != null && obj.getPrestataireId() > 0) {
            ps.setInt(9, obj.getPrestataireId());
        } else {
            ps.setNull(9, java.sql.Types.INTEGER);
        }

        ps.executeUpdate();
        
        // Retrieve generated ID to ensure the insert worked
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            // Since we can't easily mutate the ID in the Evenement object if it's final,
            // we will let EvenementController handle it by querying DESC LIMIT 1 like before
        }
        
        ps.close();
        conn.close();
    }

    public void modifier(Evenement obj) throws SQLException {
        String sql = "UPDATE Evenement SET " +
                "titre=?, description_courte=?, description_longue=?, " +
                "duree=?, langue=?, age_min=?, categorie=?, affiche=?, id_prestataire=? " +
                "WHERE id_evenement=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, obj.getTitre());
        ps.setString(2, obj.getDescriptionCourte());
        ps.setString(3, obj.getDescriptionLongue());
        ps.setInt(4, obj.getDuree());
        ps.setString(5, obj.getLangue());
        ps.setInt(6, obj.getAgeMin());
        ps.setString(7, obj.getCategorie());
        ps.setString(8, obj.getAffiche());

        if (obj.getPrestataireId() != null && obj.getPrestataireId() > 0) {
            ps.setInt(9, obj.getPrestataireId());
        } else {
            ps.setNull(9, java.sql.Types.INTEGER);
        }

        ps.setInt(10, obj.getId());
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

    public Evenement trouver(int id) throws SQLException {
        String sql = "SELECT * FROM Evenement WHERE id_evenement = ?";
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Evenement mapRow(ResultSet rs) throws SQLException {
        String cat = null;
        try {
            cat = rs.getString("categorie");
        } catch (SQLException e) {
            // ignore if column doesn't exist yet
        }
        Evenement ev = new Evenement(
                rs.getInt("id_evenement"),
                rs.getString("titre"),
                rs.getString("description_courte"),
                rs.getString("description_longue"),
                rs.getInt("duree"),
                rs.getString("langue"),
                rs.getInt("age_min"),
                cat != null ? cat : "Autre",
                rs.getString("affiche")
        );

        int pId = rs.getInt("id_prestataire");
        if (!rs.wasNull()) {
            ev.setPrestataireId(pId);
        }

        return ev;
    }
}
