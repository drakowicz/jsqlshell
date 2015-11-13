package net.rakowicz.jsqlshell;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConfig {

    private static final String DB_PROPERTIES = "jsqlshell.properties";
    private static final String DB_DRIVER = "dbDriver";
    private static final String DB_URL = "dbUrl";
    private static final String DB_USER = "dbUser";
    private static final String DB_PASS = "dbPass";
    private static final String DB_AUTO_COMMIT = "dbAutoCommit";
    private static final String DB_READ_ONLY = "dbReadOnly";
    
    private Properties dbProperties;
    private String dbPrefix;
    
    public DbConfig(String dbName) throws IOException {
        dbProperties = new Properties();
        String userHomeProperties = System.getProperty("user.home")+"/."+DB_PROPERTIES;
        try {
            if (new File(userHomeProperties).exists()) {
                dbProperties.load(new FileInputStream(userHomeProperties));
            } else {
                dbProperties.load(getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES));
            }
        } catch (Exception e) {
            throw new RuntimeException("Reading property file '" + userHomeProperties +
            "' or property file '" + DB_PROPERTIES + "' not found in the classpath!");
        }

        dbPrefix = isEmpty(dbName) ? "" : dbName + ".";
        if (isEmpty(getProperty(DB_DRIVER))) {
            throw new RuntimeException("JDBC Driver property '" + dbPrefix + DB_DRIVER + "' not found");
        }
        if (isEmpty(getProperty(DB_URL))) {
            throw new RuntimeException("DB URL property '" + dbPrefix + DB_URL + "' not found!");
        }
        if (isEmpty(getProperty(DB_USER))) {
            dbProperties.put(dbPrefix + DB_USER, CommandReader.readPassword("username: "));
        }
        if (isEmpty(getProperty(DB_PASS))) {
            dbProperties.put(dbPrefix + DB_PASS, CommandReader.readPassword("password: "));
        }
        if (isEmpty(getProperty(DB_AUTO_COMMIT))) {
            dbProperties.put(dbPrefix + DB_AUTO_COMMIT, "true");
        }
        if (isEmpty(getProperty(DB_READ_ONLY))) {
            dbProperties.put(dbPrefix + DB_READ_ONLY, "false");
        }
    }
    
    public DbConfig loadDriver() throws ClassNotFoundException {
        try {
            Class.forName(getProperty(DB_DRIVER));
        } catch (Exception e) {
            throw new RuntimeException("Driver class not found: " + e.getMessage());
        }
        return this;
    }
    
    public Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(
                getProperty(DB_URL),
                getProperty(DB_USER),
                getProperty(DB_PASS));
        connection.setAutoCommit("true".equalsIgnoreCase(getProperty(DB_AUTO_COMMIT)));
        connection.setReadOnly("true".equalsIgnoreCase(getProperty(DB_READ_ONLY)));
        return connection;
    }
    
    private String getProperty(String key) {
        return dbProperties.getProperty(dbPrefix + key);
    }
    
    private boolean isEmpty(String value) {
        return value == null || value.length() == 0 || value.trim().length() == 0;
    }
}
