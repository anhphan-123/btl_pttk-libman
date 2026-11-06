package libman.dao;

import libman.model.ImportDetail8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ImportDetailDAO8 extends DAO8 {
    public ImportDetail8 insert(Connection connection, ImportDetail8 detail) throws SQLException {
        String sql = "INSERT INTO tblImportDetail8 (importId, documentId, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, detail.getImportId());
            statement.setString(2, detail.getDocumentId());
            statement.setInt(3, detail.getQuantity());
            statement.setBigDecimal(4, detail.getPrice());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    detail.setImportDetailId(keys.getLong(1));
                    return detail;
                }
            }
        }
        throw new SQLException("Không lấy được mã chi tiết phiếu nhập sau khi lưu.");
    }

    public List<ImportDetail8> findByImportId(long importId) throws SQLException {
        String sql = "SELECT importDetailId, importId, documentId, quantity, price FROM tblImportDetail8 WHERE importId = ? ORDER BY importDetailId";
        List<ImportDetail8> details = new ArrayList<>();
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, importId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    details.add(map(result));
                }
            }
        }
        return details;
    }

    private ImportDetail8 map(ResultSet result) throws SQLException {
        ImportDetail8 detail = new ImportDetail8();
        detail.setImportDetailId(result.getLong("importDetailId"));
        detail.setImportId(result.getLong("importId"));
        detail.setDocumentId(result.getString("documentId"));
        detail.setQuantity(result.getInt("quantity"));
        detail.setPrice(result.getBigDecimal("price"));
        return detail;
    }
}
