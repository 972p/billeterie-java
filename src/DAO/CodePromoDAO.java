package DAO;

import database.MySQLConnection;
import models.CodePromo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CodePromoDAO {

    public CodePromo trouverParCode(String code) throws SQLException {
        String sql = "SELECT * FROM CodePromo WHERE code = ? AND statut = 'ACTIF'";
        try (Connection conn = MySQLConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CodePromo(
                            rs.getInt("id_code_promo"),
                            rs.getString("code"),
                            rs.getString("type_reduction"),
                            rs.getDouble("valeur_reduction"),
                            rs.getString("statut")
                    );
                }
            }
        }
        return null;
    }
}
