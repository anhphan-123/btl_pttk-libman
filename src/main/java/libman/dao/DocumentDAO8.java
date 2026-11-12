package libman.dao;

import libman.model.Document8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class DocumentDAO8 extends DAO8 {
    private static final String BASE_SELECT = "SELECT documentId, title, category, author, publisher, publishYear FROM tblDocument8";

    public List<Document8> searchByTitle(String keyword) throws SQLException {
        validateKeyword(keyword);
        String sql = BASE_SELECT + " WHERE title LIKE ? ESCAPE '=' ORDER BY title, documentId";
        List<Document8> documents = new ArrayList<>();
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + escapeLike(keyword.trim()) + "%");
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    documents.add(map(result));
                }
            }
        }
        return documents;
    }

    public Optional<Document8> findById(String documentId) throws SQLException {
        try (Connection connection = open()) {
            return findById(connection, documentId);
        }
    }

    public Optional<Document8> findByCode(String documentId) throws SQLException {
        return findById(documentId);
    }

    public List<Document8> findByIds(Collection<String> documentIds) throws SQLException {
        if (documentIds == null || documentIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> uniqueIds = new LinkedHashSet<>(documentIds);
        uniqueIds.forEach(this::validateDocumentId);
        String placeholders = String.join(", ", java.util.Collections.nCopies(uniqueIds.size(), "?"));
        String sql = BASE_SELECT + " WHERE documentId IN (" + placeholders + ")";
        List<Document8> documents = new ArrayList<>();
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (String documentId : uniqueIds) {
                statement.setString(index++, documentId);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    documents.add(map(result));
                }
            }
        }
        return documents;
    }

    public Optional<Document8> findById(Connection connection, String documentId) throws SQLException {
        validateDocumentId(documentId);
        try (PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE documentId = ?")) {
            statement.setString(1, documentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public Document8 insert(Document8 document) throws SQLException {
        try (Connection connection = open()) {
            return insert(connection, document);
        }
    }

    public Document8 insert(Connection connection, Document8 document) throws SQLException {
        String sql = "INSERT INTO tblDocument8 (documentId, title, category, author, publisher, publishYear) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, document.getDocumentId());
            statement.setString(2, document.getTitle());
            statement.setString(3, document.getCategory());
            statement.setString(4, document.getAuthor());
            statement.setString(5, document.getPublisher());
            statement.setInt(6, document.getPublishYear());
            statement.executeUpdate();
        }
        return document;
    }

    private Document8 map(ResultSet result) throws SQLException {
        Document8 document = new Document8();
        document.setDocumentId(result.getString("documentId"));
        document.setTitle(result.getString("title"));
        document.setCategory(result.getString("category"));
        document.setAuthor(result.getString("author"));
        document.setPublisher(result.getString("publisher"));
        document.setPublishYear(result.getInt("publishYear"));
        return document;
    }

    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm không được để trống.");
        }
        if (keyword.length() > 255) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm không được vượt quá 255 ký tự.");
        }
    }

    private void validateDocumentId(String documentId) {
        if (documentId == null || !documentId.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,29}")) {
            throw new IllegalArgumentException("Mã tài liệu không hợp lệ.");
        }
    }

    private String escapeLike(String value) {
        return value.replace("=", "==").replace("%", "=%").replace("_", "=_");
    }
}
