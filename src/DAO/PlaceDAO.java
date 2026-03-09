package DAO;

import database.MySQLConnection;
import models.Place;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlaceDAO {

    public void ajouter(Place obj) throws SQLException {
        String sql = "INSERT INTO Place (id_salle, rangee, numero) VALUES (?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setInt(1, obj.getId_salle());
        ps.setInt(2, obj.getRangee());
        ps.setInt(3, obj.getNumero());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            obj.setId_place(rs.getInt(1));
        }
    }

    public void modifier(Place obj) throws SQLException {
        String sql = "UPDATE Place SET id_salle=?, rangee=?, numero=? WHERE id_place=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, obj.getId_salle());
        ps.setInt(2, obj.getRangee());
        ps.setInt(3, obj.getNumero());
        ps.setInt(4, obj.getId_place());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Place WHERE id_place = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Place> trouverParSalle(int id_salle) throws SQLException {
        List<Place> liste = new ArrayList<>();
        String sql = "SELECT * FROM Place WHERE id_salle = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id_salle);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            liste.add(new Place(
                    rs.getInt("id_place"),
                    rs.getInt("id_salle"),
                    rs.getInt("rangee"),
                    rs.getInt("numero")));
        }
        return liste;
    }
}
