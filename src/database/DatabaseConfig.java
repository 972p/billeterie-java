package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    static {
        boolean loaded = false;

        // 1. Try to load config.properties from the directory where the JAR is located
        try {
            java.net.URI uri = DatabaseConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarPath = new File(uri);
            if (jarPath.isFile()) { // If running from a jar
                File externalFile = new File(jarPath.getParentFile(), "config.properties");
                if (externalFile.exists()) {
                    System.out.println("Loading configuration from JAR directory: " + externalFile.getAbsolutePath());
                    try (InputStream input = new FileInputStream(externalFile)) {
                        properties.load(input);
                        loaded = true;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore and fall back
        }

        // 2. Try to load config.properties from the current working directory
        if (!loaded) {
            File externalFile = new File("config.properties");
            if (externalFile.exists()) {
                System.out.println("Loading configuration from working directory: " + externalFile.getAbsolutePath());
                try (InputStream input = new FileInputStream(externalFile)) {
                    properties.load(input);
                    loaded = true;
                } catch (IOException e) {
                    System.err.println("Error loading external config.properties: " + e.getMessage());
                }
            }
        }

        // 3. Fallback to the classpath (embedded resource)
        if (!loaded) {
            try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    System.err.println("Error: unable to find config.properties in classpath");
                    System.exit(1);
                }
                properties.load(input);
                System.out.println("Loaded embedded configuration from classpath.");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }



    
}