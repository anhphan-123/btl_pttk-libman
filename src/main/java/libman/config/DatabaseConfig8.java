package libman.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Cấu hình JDBC chỉ đọc thông tin kết nối từ biến môi trường. */
public final class DatabaseConfig8 {
    private static final String HOST = valueOrDefault("LIBMAN_DB_HOST", "127.0.0.1");
    private static final String PORT = valueOrDefault("LIBMAN_DB_PORT", "3306");
    private static final String DATABASE = valueOrDefault("LIBMAN_DB_NAME", "libman");
    private static final String USER = System.getenv("LIBMAN_DB_USER");
    private static final String PASSWORD = System.getenv("LIBMAN_DB_PASSWORD");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new ExceptionInInitializerError("Không tìm thấy MySQL Connector/J: " + exception.getMessage());
        }
    }

    private DatabaseConfig8() {
    }

    public static Connection getConnection() throws SQLException {
        if (USER == null || USER.isBlank() || PASSWORD == null) {
            throw new SQLException("Thiếu LIBMAN_DB_USER hoặc LIBMAN_DB_PASSWORD trong biến môi trường.");
        }
        return DriverManager.getConnection(getJdbcUrl(), USER, PASSWORD);
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
                + "&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=10000";
    }

    public static String getDatabaseName() {
        return DATABASE;
    }

    private static String valueOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
