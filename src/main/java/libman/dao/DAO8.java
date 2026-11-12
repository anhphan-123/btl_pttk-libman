package libman.dao;

import libman.config.DatabaseConfig8;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DAO8 {
    protected Connection open() throws SQLException {
        return DatabaseConfig8.getConnection();
    }
}
