package com.skillgap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Enterprise-grade Database Connection Manager for MySQL.
 * 
 * Supports flexible configuration loading in priority order:
 * 1. JVM System properties (-Ddb.pass=..., -Ddb.user=..., -Ddb.config.file=...)
 * 2. Environment variables (DB_PASS / DB_PASSWORD, DB_USER, DB_NAME, DB_HOST, DB_PORT)
 * 3. External Tomcat configuration file ($CATALINA_BASE/conf/db.properties)
 * 4. External working directory configuration file (./db.properties)
 * 5. Classpath configuration file (WEB-INF/classes/db.properties)
 * 
 * Accurately supports blank passwords, multiple key aliases, and actionable diagnostics.
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;
    private static String configSource = "none";
    private static boolean driverLoaded = false;
    private static long lastLoadTime = 0;
    private static final long RELOAD_INTERVAL_MS = 10000; // allow re-checking external config if needed

    static {
        loadDriver();
        loadConfiguration();
    }

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
            System.out.println("[DBConnection] MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] ERROR: MySQL JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        configSource = "defaults";

        // 1. Try external file from JVM system property -Ddb.config.file=...
        String customConfigPath = System.getProperty("db.config.file");
        boolean loaded = false;

        if (customConfigPath != null && !customConfigPath.trim().isEmpty()) {
            File customFile = new File(customConfigPath.trim());
            if (customFile.exists() && customFile.canRead()) {
                try (InputStream fis = new FileInputStream(customFile)) {
                    props.load(fis);
                    configSource = "system property file (" + customFile.getAbsolutePath() + ")";
                    loaded = true;
                } catch (Exception e) {
                    System.err.println("[DBConnection] Could not read custom config file: " + e.getMessage());
                }
            }
        }

        // 2. Try external file in Tomcat's conf/ directory ($CATALINA_BASE/conf/db.properties or $CATALINA_HOME/conf/db.properties)
        if (!loaded) {
            String catalinaBase = System.getProperty("catalina.base", System.getProperty("catalina.home", System.getenv("CATALINA_BASE")));
            if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
                File tomcatConf = new File(new File(catalinaBase.trim(), "conf"), "db.properties");
                if (tomcatConf.exists() && tomcatConf.canRead()) {
                    try (InputStream fis = new FileInputStream(tomcatConf)) {
                        props.load(fis);
                        configSource = "Tomcat conf (" + tomcatConf.getAbsolutePath() + ")";
                        loaded = true;
                    } catch (Exception e) {
                        System.err.println("[DBConnection] Could not read Tomcat conf/db.properties: " + e.getMessage());
                    }
                }
            }
        }

        // 3. Try external file in current directory (./db.properties)
        if (!loaded) {
            File currentDirFile = new File("db.properties");
            if (currentDirFile.exists() && currentDirFile.canRead()) {
                try (InputStream fis = new FileInputStream(currentDirFile)) {
                    props.load(fis);
                    configSource = "current directory (" + currentDirFile.getAbsolutePath() + ")";
                    loaded = true;
                } catch (Exception e) {
                    System.err.println("[DBConnection] Could not read current dir db.properties: " + e.getMessage());
                }
            }
        }

        // 4. Try classpath resource (WEB-INF/classes/db.properties)
        if (!loaded) {
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    configSource = "classpath (WEB-INF/classes/db.properties)";
                    loaded = true;
                }
            } catch (Exception e) {
                System.err.println("[DBConnection] Notice: db.properties not found in classpath.");
            }
        }

        // Host resolution (System Property > Env Var > Properties File > Default)
        String host = resolveValue(
                new String[]{"db.host", "mysql.host"},
                new String[]{"DB_HOST", "MYSQL_HOST"},
                props,
                new String[]{"db.host", "mysql.host", "host"},
                "localhost"
        );

        // Port resolution
        String port = resolveValue(
                new String[]{"db.port", "mysql.port"},
                new String[]{"DB_PORT", "MYSQL_PORT"},
                props,
                new String[]{"db.port", "mysql.port", "port"},
                "3306"
        );

        // Database name resolution
        String dbName = resolveValue(
                new String[]{"db.name", "db.database", "mysql.database"},
                new String[]{"DB_NAME", "DB_DATABASE", "MYSQL_DATABASE"},
                props,
                new String[]{"db.name", "db.database", "database"},
                "skill_gap_db"
        );

        // Username resolution
        username = resolveValue(
                new String[]{"db.user", "db.username", "mysql.user"},
                new String[]{"DB_USER", "DB_USERNAME", "MYSQL_USER"},
                props,
                new String[]{"db.user", "db.username", "username", "user"},
                "root"
        );

        // Password resolution (Supports blank passwords accurately)
        password = resolvePassword(props);

        // Direct JDBC URL or URI resolution (Supports mysql://user:pass@host:port/db or jdbc:mysql://...)
        String directUrl = resolveValue(
                new String[]{"db.url", "jdbc.url", "DB_URL", "DATABASE_URL"},
                new String[]{"DB_URL", "JDBC_DATABASE_URL", "DATABASE_URL", "MYSQL_URL"},
                props,
                new String[]{"db.url", "jdbc.url", "url"},
                null
        );

        if (directUrl != null && !directUrl.trim().isEmpty()) {
            directUrl = directUrl.trim();
            // Handle standard cloud URI: mysql://user:pass@host:port/dbname
            if (directUrl.contains("@") && (directUrl.startsWith("mysql://") || directUrl.startsWith("jdbc:mysql://"))) {
                try {
                    String cleanUri = directUrl.startsWith("jdbc:") ? directUrl.substring(5) : directUrl;
                    java.net.URI uri = new java.net.URI(cleanUri);
                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":", 2);
                        if (userInfo.length > 0 && !userInfo[0].isEmpty()) {
                            username = java.net.URLDecoder.decode(userInfo[0], "UTF-8");
                        }
                        if (userInfo.length > 1) {
                            password = java.net.URLDecoder.decode(userInfo[1], "UTF-8");
                        }
                    }
                    if (uri.getHost() != null) {
                        host = uri.getHost();
                    }
                    if (uri.getPort() > 0) {
                        port = String.valueOf(uri.getPort());
                    }
                    if (uri.getPath() != null && uri.getPath().length() > 1) {
                        dbName = uri.getPath().substring(1);
                    }
                    url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                            + "?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectTimeout=10000";
                } catch (Exception e) {
                    System.err.println("[DBConnection] Could not parse URI connection string: " + e.getMessage() + ". Using as raw JDBC URL.");
                    url = directUrl.startsWith("jdbc:") ? directUrl : ("jdbc:" + directUrl);
                }
            } else {
                url = directUrl.startsWith("jdbc:") ? directUrl : ("jdbc:" + directUrl);
            }
        } else {
            String sslSetting = resolveValue(
                    new String[]{"db.ssl"},
                    new String[]{"DB_SSL", "MYSQL_SSL"},
                    props,
                    new String[]{"db.ssl", "ssl"},
                    "false"
            );
            boolean useSSL = "true".equalsIgnoreCase(sslSetting) || "required".equalsIgnoreCase(sslSetting);

            if (useSSL) {
                url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                        + "?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectTimeout=10000";
            } else {
                url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectTimeout=10000";
            }
        }

        lastLoadTime = System.currentTimeMillis();
        System.out.println("[DBConnection] Configuration loaded from " + configSource
                + " [Target: " + host + ":" + port + "/" + dbName + ", User: " + username + "]");
    }

    /**
     * Resolves configuration property with multiple key aliases across System properties, Environment variables, and Properties files.
     */
    private static String resolveValue(String[] sysKeys, String[] envKeys, Properties props, String[] propKeys, String fallback) {
        // 1. System Properties
        for (String k : sysKeys) {
            String val = System.getProperty(k);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }

        // 2. Environment Variables
        for (String k : envKeys) {
            String val = System.getenv(k);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }

        // 3. Properties File
        for (String k : propKeys) {
            if (props.containsKey(k)) {
                String val = props.getProperty(k);
                if (val != null) {
                    return val.trim();
                }
            }
        }

        return fallback;
    }

    /**
     * Resolves database password with precise support for blank/empty passwords.
     */
    private static String resolvePassword(Properties props) {
        // 1. System Property: -Ddb.pass=... or -Ddb.password=...
        String[] sysKeys = new String[]{"db.pass", "db.password", "mysql.password", "mysql.pass"};
        for (String k : sysKeys) {
            if (System.getProperties().containsKey(k)) {
                return System.getProperty(k); // can be empty string
            }
        }

        // 2. Environment variables: DB_PASS, DB_PASSWORD, MYSQL_PWD, MYSQL_PASSWORD
        String[] envKeys = new String[]{"DB_PASS", "DB_PASSWORD", "MYSQL_PWD", "MYSQL_PASSWORD"};
        for (String k : envKeys) {
            String val = System.getenv(k);
            if (val != null) {
                return val; // allow empty string if explicitly set in environment
            }
        }

        // 3. Properties file: db.pass, db.password, password, pass
        String[] propKeys = new String[]{"db.pass", "db.password", "password", "pass"};
        for (String k : propKeys) {
            if (props.containsKey(k)) {
                String val = props.getProperty(k);
                return val != null ? val.trim() : "";
            }
        }

        // 4. Default fallback if not defined anywhere
        return "root";
    }

    /**
     * Obtains an active connection to MySQL.
     * @return java.sql.Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            // If access denied or connection failed, allow reloading configuration in case credentials were updated
            if (System.currentTimeMillis() - lastLoadTime > RELOAD_INTERVAL_MS) {
                System.out.println("[DBConnection] Connection attempt failed. Re-evaluating configuration...");
                loadConfiguration();
            }
            // Enhance exception message for user diagnosis while protecting secret values
            if (ex.getErrorCode() == 1045 || ex.getMessage().contains("Access denied")) {
                System.err.println("[DBConnection] AUTHENTICATION FAILURE: Access denied for MySQL user '" + username + "'.");
                System.err.println("[DBConnection] Current configuration source: " + configSource);
                System.err.println("[DBConnection] Target URL: " + url);
                System.err.println("[DBConnection] HINT: You can provide your real MySQL password in any of these ways:");
                System.err.println("  1. Create/edit file: C:\\apache-tomcat-9\\conf\\db.properties (db.pass=YOUR_PASSWORD)");
                System.err.println("  2. Set environment variable: DB_PASS=YOUR_PASSWORD (in Tomcat bin\\setenv.bat or Windows system)");
                System.err.println("  3. Edit src\\db.properties and rebuild the WAR with 'ant war'");
            }
            throw ex;
        }
    }

    /**
     * Allows dynamic override of credentials for setup scripts or testing.
     */
    public static synchronized void configure(String customUrl, String customUser, String customPass) {
        url = customUrl;
        username = customUser;
        password = customPass;
        configSource = "programmatic override";
    }

    public static String getConfigSource() {
        return configSource;
    }

    public static String getUsername() {
        return username;
    }

    public static String getUrl() {
        return url;
    }
}
