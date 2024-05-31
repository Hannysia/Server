package annaKnysh.serverside.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;
    private static DatabaseConnection instance;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    private DatabaseConnection() { }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        if (connection == null || !isValid(connection)) {
            connection = createNewConnection();
        }
        return connection;
    }

    public static boolean initialize(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
        connection = createNewConnection();
        return connection != null;
    }

    private static Connection createNewConnection() {
        try {
            Properties props = new Properties();
            props.setProperty("user", dbUser);
            props.setProperty("password", dbPassword);
            return DriverManager.getConnection(dbUrl, props);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }

    private static boolean isValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
