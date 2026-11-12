package libman.dao;

import libman.model.Invoice8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class InvoiceDAO8 extends DAO8 {
    public Invoice8 insert(Connection connection, Invoice8 invoice) throws SQLException {
        String sql = "INSERT INTO tblInvoice8 (importId, invoiceDate) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, invoice.getImportId());
            statement.setObject(2, invoice.getInvoiceDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    invoice.setInvoiceId(keys.getLong(1));
                    return invoice;
                }
            }
        }
        throw new SQLException("Không lấy được mã hóa đơn sau khi lưu.");
    }

    public Optional<Invoice8> findByImportId(long importId) throws SQLException {
        String sql = "SELECT i.invoiceId, i.importId, i.invoiceDate, COALESCE(SUM(d.quantity * d.price), 0) AS totalAmount "
                + "FROM tblInvoice8 i LEFT JOIN tblImportDetail8 d ON d.importId = i.importId "
                + "WHERE i.importId = ? GROUP BY i.invoiceId, i.importId, i.invoiceDate";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, importId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public Optional<Invoice8> findById(long invoiceId) throws SQLException {
        String sql = "SELECT i.invoiceId, i.importId, i.invoiceDate, "
                + "COALESCE(SUM(d.quantity * d.price), 0) AS totalAmount "
                + "FROM tblInvoice8 i LEFT JOIN tblImportDetail8 d ON d.importId = i.importId "
                + "WHERE i.invoiceId = ? GROUP BY i.invoiceId, i.importId, i.invoiceDate";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, invoiceId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    private Invoice8 map(ResultSet result) throws SQLException {
        Invoice8 invoice = new Invoice8();
        invoice.setInvoiceId(result.getLong("invoiceId"));
        invoice.setImportId(result.getLong("importId"));
        invoice.setInvoiceDate(result.getObject("invoiceDate", java.time.LocalDate.class));
        invoice.setTotalAmount(result.getBigDecimal("totalAmount"));
        return invoice;
    }
}
