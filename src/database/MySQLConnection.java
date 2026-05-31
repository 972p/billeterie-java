package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnection {
    public static Connection connect() throws SQLException {
        try {
            var jdbcUrl = DatabaseConfig.getDbUrl();
            var user = DatabaseConfig.getDbUsername();
            var password = DatabaseConfig.getDbPassword();

            // 1. Tenter de créer la base de données automatiquement si elle n'existe pas
            if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:mysql://")) {
                int questionMarkIdx = jdbcUrl.indexOf('?');
                String baseUrl = questionMarkIdx != -1 ? jdbcUrl.substring(0, questionMarkIdx) : jdbcUrl;
                String queryParams = questionMarkIdx != -1 ? jdbcUrl.substring(questionMarkIdx) : "";

                int lastSlashIdx = baseUrl.lastIndexOf('/');
                if (lastSlashIdx > 12) { // 12 correspond à la longueur de "jdbc:mysql://"
                    String serverUrl = baseUrl.substring(0, lastSlashIdx + 1) + queryParams;
                    String dbName = baseUrl.substring(lastSlashIdx + 1);

                    if (!dbName.isEmpty()) {
                        // Connexion au serveur MySQL sans spécifier de base pour pouvoir exécuter le CREATE DATABASE
                        try (Connection serverConn = DriverManager.getConnection(serverUrl, user, password);
                             Statement stmt = serverConn.createStatement()) {
                            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                        } catch (SQLException e) {
                            // On affiche un avertissement sans bloquer, au cas où l'utilisateur n'ait pas les droits CREATE
                            System.err.println("Warning: Impossible de vérifier/créer la base de données '" + dbName + "' : " + e.getMessage());
                        }
                    }
                }
            }

            return DriverManager.getConnection(jdbcUrl, user, password);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
