package libman;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import libman.config.DatabaseConfig8;
import libman.dao.DocumentCopyDAO8;
import libman.dao.DocumentDAO8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm thử Search Document 8 với MySQL thật khi biến môi trường đã được khai báo. */
class SearchDocumentIntegrationTest8 {
    @Test
    void searchesDocumentsAndReadsCopiesWithoutMutation() throws Exception {
        Assumptions.assumeTrue(isDatabaseConfigured(), "Bỏ qua vì chưa cấu hình MySQL.");
        int documentsBefore = count("SELECT COUNT(*) FROM tblDocument8");
        int copiesBefore = count("SELECT COUNT(*) FROM tblDocumentCopy8");

        DocumentDAO8 documentDAO8 = new DocumentDAO8();
        assertEquals(1, documentDAO8.searchByTitle("Lập trình Java Web").size());
        assertEquals(1, documentDAO8.searchByTitle("Java").size());
        assertFalse(documentDAO8.searchByTitle("Web").isEmpty());
        assertTrue(documentDAO8.searchByTitle("không-tồn-tại-8").isEmpty());
        assertTrue(documentDAO8.searchByTitle("%").isEmpty(), "% phải được tìm như ký tự thường.");
        assertTrue(documentDAO8.searchByTitle("_").isEmpty(), "_ phải được tìm như ký tự thường.");
        assertEquals("GT-016", documentDAO8.findByCode("GT-016").orElseThrow().getDocumentId());
        assertTrue(documentDAO8.findByCode("NO-SUCH-8").isEmpty());
        List<?> copies = new DocumentCopyDAO8().findByDocumentId("GT-016");
        assertFalse(copies.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> documentDAO8.searchByTitle("  "));
        assertThrows(IllegalArgumentException.class, () -> documentDAO8.findById("bad code!"));

        assertEquals(documentsBefore, count("SELECT COUNT(*) FROM tblDocument8"));
        assertEquals(copiesBefore, count("SELECT COUNT(*) FROM tblDocumentCopy8"));
    }

    private int count(String sql) throws Exception {
        try (Connection connection = DatabaseConfig8.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    private boolean isDatabaseConfigured() {
        String user = System.getenv("LIBMAN_DB_USER");
        String password = System.getenv("LIBMAN_DB_PASSWORD");
        return user != null && !user.isBlank() && password != null;
    }
}
