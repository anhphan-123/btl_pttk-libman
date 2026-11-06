package libman.dao;

import libman.config.DatabaseConfig8;

import java.sql.Connection;
import java.sql.SQLException;

/** Lớp DAO cơ sở theo sơ đồ thiết kế; không giữ Connection dùng chung giữa các request. */
public abstract class DAO8 {
    protected Connection open() throws SQLException {
        return DatabaseConfig8.getConnection();
    }
}
