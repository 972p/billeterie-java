package DAO;

import database.MySQLConnection;
import models.Prestataire;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestataireDAO {

    public List<Prestataire> trouverTous() throws SQLException {
        List<Prestataire> prestataires = new ArrayList<>();
        String sql = "SELECT * FROM Prestataire ORDER BY nom ASC";

        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestataires.add(new Prestataire(
                        rs.getInt("id_prestataire"),
                        rs.getString("nom"),
                        rs.getString("specialite"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe")
                ));
            }
        }
        return prestataires;
    }

    public Prestataire trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM Prestataire WHERE id_prestataire = ?";
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Prestataire(
                            rs.getInt("id_prestataire"),
                            rs.getString("nom"),
                            rs.getString("specialite"),
                            rs.getString("contact"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe")
                    );
                }
            }
        }
        return null;
    }

    public void ajouter(Prestataire p) throws SQLException {
        String sql = "INSERT INTO Prestataire (nom, specialite, contact, email, mot_de_passe) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getSpecialite());
            ps.setString(3, p.getContact());
            ps.setString(4, p.getEmail());
            // Hash the password if it's not already hashed (assuming it's a new password in plain text if called from UI)
            String hashed = p.getMotDePasse().startsWith("$2a$") ? p.getMotDePasse() : org.mindrot.jbcrypt.BCrypt.hashpw(p.getMotDePasse(), org.mindrot.jbcrypt.BCrypt.gensalt());
            ps.setString(5, hashed);
            ps.executeUpdate();
        }
    }

    public void modifier(Prestataire p) throws SQLException {
        boolean hasPassword = p.getMotDePasse() != null && !p.getMotDePasse().isEmpty();
        String sql = hasPassword ? 
            "UPDATE Prestataire SET nom=?, specialite=?, contact=?, email=?, mot_de_passe=? WHERE id_prestataire=?" :
            "UPDATE Prestataire SET nom=?, specialite=?, contact=?, email=? WHERE id_prestataire=?";
        
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getSpecialite());
            ps.setString(3, p.getContact());
            ps.setString(4, p.getEmail());
            if (hasPassword) {
                String hashed = p.getMotDePasse().startsWith("$2a$") ? p.getMotDePasse() : org.mindrot.jbcrypt.BCrypt.hashpw(p.getMotDePasse(), org.mindrot.jbcrypt.BCrypt.gensalt());
                ps.setString(5, hashed);
                ps.setInt(6, p.getId());
            } else {
                ps.setInt(5, p.getId());
            }
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Prestataire WHERE id_prestataire=?";
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Prestataire authentifier(String email, String password) throws SQLException {
        String sql = "SELECT * FROM Prestataire WHERE email = ?";
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPw = rs.getString("mot_de_passe");
                    if (org.mindrot.jbcrypt.BCrypt.checkpw(password, hashedPw)) {
                        return new Prestataire(
                                rs.getInt("id_prestataire"),
                                rs.getString("nom"),
                                rs.getString("specialite"),
                                rs.getString("contact"),
                                rs.getString("email"),
                                hashedPw
                        );
                    }
                }
            }
        }
        return null;
    }
}
