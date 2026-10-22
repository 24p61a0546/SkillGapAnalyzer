package com.skillgap.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe Database Connection Manager for MySQL.
 * Reads configuration from db.properties or system environment variables with fallback defaults.
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;
    private static boolean driverLoaded = false;

    static {
        loadConfiguration();
    }

    private static synchronized void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Notice: db.properties not found in classpath, using environment or default values.");
        }

        // Priority: System Env > db.properties > Default
        String host = getEnvOrProp("DB_HOST", props.getProperty("db.host", "localhost"));
        String port = getEnvOrProp("DB_PORT", props.getProperty("db.port", "3306"));
        String name = getEnvOrProp("DB_NAME", props.getProperty("db.name", "skill_gap_db"));
        username = getEnvOrProp("DB_USER", props.getProperty("db.user", "root"));
        password = getEnvOrProp("DB_PASS", props.getProperty("db.pass", "root"));

        url = "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
            System.out.println("[DBConnection] MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] Error: MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found!");
            e.printStackTrace();
        }
    }

    private static String getEnvOrProp(String envKey, String fallback) {
        String val = System.getenv(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = System.getProperty(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        return fallback;
    }

    /**
     * Obtains an active connection to MySQL.
     * @return java.sql.Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadConfiguration();
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Allows dynamic override of credentials for setup scripts or testing.
     */
    public static synchronized void configure(String customUrl, String customUser, String customPass) {
        url = customUrl;
        username = customUser;
        password = customPass;
    }
}
