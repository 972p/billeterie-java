package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MySQLConnection {
    public static Connection connect() throws SQLException {

        try {
            var jdbcUrl = DatabaseConfig.getDbUrl();
            var user = DatabaseConfig.getDbUsername();
            var password = DatabaseConfig.getDbPassword();

            // Uncomment if code above doesn't work)
            // var jdbcUrl = "jdbc:mysql://localhost:DATABASE_PORT/DATABASE_NAME";
            // var user = "USERNAME";
            // var password = "PASSWORD";

            return DriverManager.getConnection(jdbcUrl, user, password);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
