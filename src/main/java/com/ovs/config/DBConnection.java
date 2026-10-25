package com.ovs.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Manager implementing the Singleton pattern.
 * Manages JDBC connections to MySQL database for the Online Voting System.
 */
public class DBConnection {

    private static final String HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String PORT = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "online_voting_db";
    private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "Raihaanarif1@";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static volatile DBConnection instance;
    private Connection connection;

    // Static block to register MySQL JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Fatal: MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found.");
            throw new ExceptionInInitializerError("MySQL JDBC Driver failed to load: " + e.getMessage());
        }
    }

    /**
     * Private constructor to prevent direct instantiation (Singleton pattern).
     */
    private DBConnection() {
        // Initialization handled in getConnection()
    }

    /**
     * Retrieves the Singleton instance of DBConnection.
     *
     * @return DBConnection instance
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns a valid, active MySQL Database Connection.
     * Re-establishes connection if null or previously closed.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static synchronized Connection getConnection() throws SQLException {
        DBConnection dbInstance = getInstance();
        if (dbInstance.connection == null || dbInstance.connection.isClosed()) {
            dbInstance.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return dbInstance.connection;
    }

    /**
     * Closes the underlying database connection if open.
     */
    public static synchronized void closeConnection() {
        DBConnection dbInstance = getInstance();
        if (dbInstance.connection != null) {
            try {
                if (!dbInstance.connection.isClosed()) {
                    dbInstance.connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                dbInstance.connection = null;
            }
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static String getHost() {
        return HOST;
    }

    public static String getPort() {
        return PORT;
    }

    public static String getDbName() {
        return DB_NAME;
    }

    public static String getUser() {
        return USER;
    }

    /**
     * Test utility main method to verify MySQL connectivity directly from IDE/CLI.
     *
     * @param args runtime arguments
     */
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Online Voting System - DB Connection Test");
        System.out.println("=================================================");
        System.out.println("DB Host : " + HOST);
        System.out.println("DB Port : " + PORT);
        System.out.println("DB Name : " + DB_NAME);
        System.out.println("DB User : " + USER);
        System.out.println("JDBC URL: " + URL);

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("\nDatabase connected successfully!");
                System.out.println("Connected to: " + conn.getMetaData().getDatabaseProductName() + " v" + conn.getMetaData().getDatabaseProductVersion());
            } else {
                System.err.println("\nFailed to obtain a valid database connection.");
            }
        } catch (SQLException e) {
            System.err.println("\nDatabase connection failed!");
            System.err.println("SQL State : " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message   : " + e.getMessage());
            System.err.println("\nTroubleshooting Tips:");
            System.err.println(" 1. Ensure MySQL Server is running on " + HOST + ":" + PORT + ".");
            System.err.println(" 2. Execute 'sql/schema.sql' to create the '" + DB_NAME + "' database.");
            System.err.println(" 3. Verify user credentials (configured user: '" + USER + "') or set DB_PASSWORD env var.");
        }
    }
}
