package libman.dao;

import libman.model.DocumentCopy8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentCopyDAO8 extends DAO8 {
    private static final int BATCH_SIZE = 500;

    public List<DocumentCopy8> findByDocumentId(String documentId) throws SQLException {
        if (documentId == null || !documentId.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,29}")) {
            throw new IllegalArgumentException("Mã tài liệu không hợp lệ.");
        }
        String sql = "SELECT copyId, documentId, importDetailId, status FROM tblDocumentCopy8 WHERE documentId = ? ORDER BY copyId";
        List<DocumentCopy8> copies = new ArrayList<>();
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, documentId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    copies.add(map(result));
                }
            }
        }
        return copies;
    }

    public void insert(Connection connection, DocumentCopy8 copy) throws SQLException {
        String sql = "INSERT INTO tblDocumentCopy8 (copyId, documentId, importDetailId, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, copy.getCopyId());
            statement.setString(2, copy.getDocumentId());
            statement.setLong(3, copy.getImportDetailId());
            statement.setString(4, copy.getStatus());
            statement.executeUpdate();
        }
    }

    public void createCopies(Connection connection, String documentId, int quantity, long importDetailId)
            throws SQLException {
        if (documentId == null || !documentId.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,29}")
                || quantity <= 0 || importDetailId <= 0) {
            throw new IllegalArgumentException("Thông tin tạo bản sao không hợp lệ.");
        }
        String sql = "INSERT INTO tblDocumentCopy8 (copyId, documentId, importDetailId, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int position = 1; position <= quantity; position++) {
                statement.setString(1, documentId + "-"
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
                statement.setString(2, documentId);
                statement.setLong(3, importDetailId);
                statement.setString(4, "AVAILABLE");
                statement.addBatch();
                if (position % BATCH_SIZE == 0) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
            }
            if (quantity % BATCH_SIZE != 0) {
                statement.executeBatch();
            }
        }
    }

    private DocumentCopy8 map(ResultSet result) throws SQLException {
        DocumentCopy8 copy = new DocumentCopy8();
        copy.setCopyId(result.getString("copyId"));
        copy.setDocumentId(result.getString("documentId"));
        copy.setImportDetailId(result.getLong("importDetailId"));
        copy.setStatus(result.getString("status"));
        return copy;
    }
}
