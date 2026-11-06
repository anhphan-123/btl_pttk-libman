package libman.dao;

import libman.model.ImportDetail8;
import libman.model.ImportReceipt8;
import libman.model.Invoice8;
import libman.util.ValidationUtil8;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Optional;

public class ImportReceiptDAO8 extends DAO8 {
    private final ImportDetailDAO8 importDetailDAO8 = new ImportDetailDAO8();
    private final DocumentCopyDAO8 documentCopyDAO8 = new DocumentCopyDAO8();
    private final InvoiceDAO8 invoiceDAO8 = new InvoiceDAO8();

    /**
     * Lưu phiếu, các dòng chi tiết, toàn bộ bản sao và hóa đơn trong một transaction JDBC.
     * submissionToken có unique key để lần Submit lặp lại không sinh thêm phiếu.
     */
    public ImportReceipt8 saveReceiptWithDetailsAndInvoice(ImportReceipt8 receipt) throws SQLException {
        validateReceipt(receipt);
        try (Connection connection = open()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Optional<ImportReceipt8> existing = findBySubmissionToken(connection, receipt.getSubmissionToken());
                if (existing.isPresent()) {
                    connection.commit();
                    return existing.get();
                }

                insert(connection, receipt);
                for (ImportDetail8 detail : receipt.getDetails()) {
                    detail.setImportId(receipt.getImportId());
                    importDetailDAO8.insert(connection, detail);
                    documentCopyDAO8.createCopies(connection, detail.getDocumentId(), detail.getQuantity(),
                            detail.getImportDetailId());
                }

                Invoice8 invoice = new Invoice8();
                invoice.setImportId(receipt.getImportId());
                invoice.setInvoiceDate(receipt.getImportDate());
                invoiceDAO8.insert(connection, invoice);
                connection.commit();
                return receipt;
            } catch (SQLIntegrityConstraintViolationException duplicate) {
                connection.rollback();
                Optional<ImportReceipt8> existing = findBySubmissionToken(connection, receipt.getSubmissionToken());
                if (existing.isPresent()) {
                    return existing.get();
                }
                throw duplicate;
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw new SQLException("Không thể lưu phiếu nhập; toàn bộ transaction đã được rollback.", exception);
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public Optional<ImportReceipt8> findBySubmissionToken(String submissionToken) throws SQLException {
        try (Connection connection = open()) {
            return findBySubmissionToken(connection, submissionToken);
        }
    }

    private void insert(Connection connection, ImportReceipt8 receipt) throws SQLException {
        String sql = "INSERT INTO tblImportReceipt8 (supplierId, librarianId, importDate, status, submissionToken) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, receipt.getSupplierId());
            statement.setLong(2, receipt.getLibrarianId());
            statement.setObject(3, receipt.getImportDate());
            statement.setString(4, ImportReceipt8.STATUS_UNPAID);
            statement.setString(5, receipt.getSubmissionToken());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    receipt.setImportId(keys.getLong(1));
                    receipt.setStatus(ImportReceipt8.STATUS_UNPAID);
                    return;
                }
            }
        }
        throw new SQLException("Không lấy được mã phiếu nhập sau khi lưu.");
    }

    private Optional<ImportReceipt8> findBySubmissionToken(Connection connection, String submissionToken) throws SQLException {
        String sql = "SELECT importId, supplierId, librarianId, importDate, status, submissionToken FROM tblImportReceipt8 WHERE submissionToken = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, submissionToken);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                ImportReceipt8 receipt = new ImportReceipt8();
                receipt.setImportId(result.getLong("importId"));
                receipt.setSupplierId(result.getLong("supplierId"));
                receipt.setLibrarianId(result.getLong("librarianId"));
                receipt.setImportDate(result.getObject("importDate", java.time.LocalDate.class));
                receipt.setStatus(result.getString("status"));
                receipt.setSubmissionToken(result.getString("submissionToken"));
                return Optional.of(receipt);
            }
        }
    }

    private void validateReceipt(ImportReceipt8 receipt) {
        if (receipt == null || receipt.getSupplierId() == null || receipt.getLibrarianId() == null
                || receipt.getImportDate() == null || receipt.getSubmissionToken() == null
                || receipt.getSubmissionToken().isBlank()) {
            throw new IllegalArgumentException("Phiếu nhập thiếu thông tin bắt buộc.");
        }
        if (receipt.getDetails() == null || receipt.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một dòng chi tiết hợp lệ.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ImportDetail8 detail : receipt.getDetails()) {
            if (detail == null || detail.getDocumentId() == null || detail.getDocumentId().isBlank()
                    || detail.getQuantity() == null || detail.getQuantity() <= 0
                    || !ValidationUtil8.isValidMoney(detail.getPrice())) {
                throw new IllegalArgumentException("Dòng chi tiết nhập không hợp lệ.");
            }
            total = total.add(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        }
        if (!ValidationUtil8.isValidMoney(total)) {
            throw new IllegalArgumentException("Tổng tiền phiếu nhập vượt giới hạn DECIMAL(12,2).");
        }
    }
}
