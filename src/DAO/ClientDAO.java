package DAO;

import java.sql.*;

import database.DatabaseConfig;
import database.MySQLConnection;
import models.Client;

public class ClientDAO {
    public void ajouter(Client obj) throws SQLException {
        String sql = "INSERT INTO Client (nom, email, telephone, adresse, mot_de_passe, p_role, photo_profil) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, obj.getNom());
        ps.setString(2, obj.getEmail());
        ps.setString(3, obj.getTelephone());
        ps.setString(4, obj.getAdresse());

        // Hash the password before storing
        String hashed = org.mindrot.jbcrypt.BCrypt.hashpw(obj.getMotDePasse(), org.mindrot.jbcrypt.BCrypt.gensalt());
        ps.setString(5, hashed);

        ps.setString(6, obj.getRole());
        ps.setString(7, obj.getPhotoProfil());
        ps.executeUpdate();
    }

    public void modifier(Client obj) throws SQLException {
        String sql = "UPDATE Client SET nom=?, email=?, telephone=?, adresse=?, mot_de_passe=?, p_role=?, photo_profil=? WHERE id_client=?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, obj.getNom());
        ps.setString(2, obj.getEmail());
        ps.setString(3, obj.getTelephone());
        ps.setString(4, obj.getAdresse());
        ps.setString(5, obj.getMotDePasse());
        ps.setString(6, obj.getRole());
        ps.setString(7, obj.getPhotoProfil());
        ps.setInt(8, obj.getId());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Client WHERE id_client = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public Client authentifier(String email, String motDePasse) throws SQLException {
        // Only fetch by email. Password verification is done by BCrypt.
        String sql = "SELECT * FROM Client WHERE email = ?";
        Connection conn = MySQLConnection.connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String hashedDbPassword = rs.getString("mot_de_passe");

            // Fallback for pre-existing plaintext passwords during dev (Optional but good
            // for smooth transitions)
            boolean isValid = false;
            try {
                isValid = org.mindrot.jbcrypt.BCrypt.checkpw(motDePasse, hashedDbPassword);
            } catch (IllegalArgumentException e) {
                // Not a valid BCrypt hash format. Check if it's plaintext.
                if (motDePasse.equals(hashedDbPassword)) {
                    isValid = true;
                }
            }

            if (isValid) {
                return new Client(
                        rs.getInt("id_client"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("adresse"),
                        hashedDbPassword,
                        rs.getString("p_role"),
                        rs.getString("photo_profil"));
            }
        }
        return null;
    }

    // Récupérer tous les clients (avec pagination)
    public java.util.List<Client> findPaginated(int page, int limit) throws SQLException {
        java.util.List<Client> clients = new java.util.ArrayList<>();
        int offset = (page - 1) * limit;

        String query = "SELECT * FROM Client ORDER BY id_client ASC LIMIT ? OFFSET ?";
        try (Connection conn = MySQLConnection.connect();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Client client = new Client(
                            rs.getInt("id_client"),
                            rs.getString("nom"),
                            rs.getString("email"),
                            rs.getString("telephone"),
                            rs.getString("adresse"),
                            rs.getString("mot_de_passe"),
                            rs.getString("p_role"),
                            rs.getString("photo_profil"));
                    clients.add(client);
                }
            }
        }
        return clients;
    }
}
