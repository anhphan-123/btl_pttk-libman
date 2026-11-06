package libman;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import libman.config.DatabaseConfig8;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Chỉ chạy khi LIBMAN_DB_USER và LIBMAN_DB_PASSWORD đã được khai báo. */
class DatabaseSchemaIntegrationTest8 {
    @Test
    void verifiesSchemaConstraintsAndSeedData() throws Exception {
        Assumptions.assumeTrue(isDatabaseConfigured(), "Bỏ qua kiểm thử MySQL: chưa cấu hình biến môi trường LIBMAN_DB_*." );

        try (Connection connection = DatabaseConfig8.getConnection()) {
            assertEquals(15, count(connection,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN "
                            + "('tblReader8','tblReaderCard8','tblLibrarian8','tblManager8','tblSupplier8','tblDocument8',"
                            + "'tblDocumentCopy8','tblLoanReceipt8','tblLoanDetail8','tblReturnReceipt8','tblReturnDetail8',"
                            + "'tblImportReceipt8','tblImportDetail8','tblInvoice8','tblPayment8')"));
            assertEquals(4, count(connection, "SELECT COUNT(*) FROM tblDocument8"));
            assertEquals(5, count(connection, "SELECT COUNT(*) FROM tblDocumentCopy8"));
            assertEquals(0, count(connection,
                    "SELECT COUNT(*) FROM (SELECT invoiceId FROM tblPayment8 WHERE paymentStatus = 'SUCCESS' GROUP BY invoiceId HAVING COUNT(*) > 1) AS duplicate_success"));
            assertEquals("UNPAID", value(connection, "SELECT status FROM tblImportReceipt8 WHERE importId = 2"));
            assertEquals(new BigDecimal("405000.00"), decimal(connection,
                    "SELECT SUM(quantity * price) FROM tblImportDetail8 WHERE importId = 1"));
        }
    }

    private boolean isDatabaseConfigured() {
        String user = System.getenv("LIBMAN_DB_USER");
        String password = System.getenv("LIBMAN_DB_PASSWORD");
        return user != null && !user.isBlank() && password != null;
    }

    private int count(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    private String value(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getString(1);
        }
    }

    private BigDecimal decimal(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getBigDecimal(1);
        }
    }
}
