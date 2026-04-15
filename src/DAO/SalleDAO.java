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
        // Default to all active
        List<Integer> statuses = new ArrayList<>();
        for (int i = 0; i < obj.getNb_rangees() * obj.getNb_colonnes(); i++) {
            statuses.add(1);
        }
        ajouter(obj, statuses);
    }

    public void ajouter(Salle obj, List<Integer> seatStatuses) throws SQLException {
        String sql = "INSERT INTO Salle (id_lieu, nom, nb_rangees, nb_colonnes, nb_places) VALUES (?, ?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setInt(1, obj.getId_lieu());
        ps.setString(2, obj.getNom());
        ps.setInt(3, obj.getNb_rangees());
        ps.setInt(4, obj.getNb_colonnes());
        ps.setInt(5, obj.getNb_places());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            obj.setId_salle(rs.getInt(1));
        }

        // Générer les places avec les statuts spécifiés
        PreparedStatement psPlace = conn.prepareStatement("INSERT INTO Place (id_salle, rangee, numero, statut) VALUES (?, ?, ?, ?)");
        int indexList = 0;
        for (int r = 1; r <= obj.getNb_rangees(); r++) {
            for (int c = 1; c <= obj.getNb_colonnes(); c++) {
                int status = (indexList < seatStatuses.size()) ? seatStatuses.get(indexList) : 1;
                psPlace.setInt(1, obj.getId_salle());
                psPlace.setInt(2, r);
                psPlace.setInt(3, c); // CORRECT: Store column number, not linear index
                psPlace.setInt(4, status);
                psPlace.addBatch();
                indexList++;
            }
        }
        psPlace.executeBatch();
        psPlace.close();
        ps.close();
        conn.close();
    }

    public void modifier(Salle obj) throws SQLException {
        String sql = "UPDATE Salle SET id_lieu=?, nom=?, nb_rangees=?, nb_colonnes=?, nb_places=? WHERE id_salle=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, obj.getId_lieu());
        ps.setString(2, obj.getNom());
        ps.setInt(3, obj.getNb_rangees());
        ps.setInt(4, obj.getNb_colonnes());
        ps.setInt(5, obj.getNb_places());
        ps.setInt(6, obj.getId_salle());
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Salle WHERE id_salle = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    public List<Salle> trouverParLieu(int id_lieu) throws SQLException {
        List<Salle> liste = new ArrayList<>();
        String sql = "SELECT * FROM Salle WHERE id_lieu = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id_lieu);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int rangees = rs.getInt("nb_rangees");
            int colonnes = rs.getInt("nb_colonnes");
            int nbPlaces = rs.getInt("nb_places");
            
            // Safety: if nb_places is 0 but it's a grid, assume it's row*col
            if (nbPlaces == 0 && rangees > 0 && colonnes > 0) {
                nbPlaces = rangees * colonnes;
            }

            liste.add(new Salle(
                    rs.getInt("id_salle"),
                    rs.getInt("id_lieu"),
                    rs.getString("nom"),
                    rangees,
                    colonnes,
                    nbPlaces));
        }
        rs.close();
        ps.close();
        conn.close();
        return liste;
    }
}
