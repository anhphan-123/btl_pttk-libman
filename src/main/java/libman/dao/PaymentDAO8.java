package libman.dao;

import libman.model.ImportReceipt8;
import libman.model.Payment8;
import libman.util.ValidationUtil8;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO8 extends DAO8 {
    public Payment8 recordManualPayment(Payment8 payment) throws SQLException {
        validatePayment(payment);
        try (Connection connection = open()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long importId = lockInvoiceAndGetImportId(connection, payment.getInvoiceId());
                BigDecimal expectedAmount = findInvoiceTotal(connection, importId);

                if (hasSuccessfulPayment(connection, payment.getInvoiceId())) {
                    throw new IllegalStateException("Hóa đơn đã thanh toán thành công; không thể ghi nhận thêm.");
                }
                if (Payment8.STATUS_SUCCESS.equals(payment.getPaymentStatus())) {
                    if (payment.getAmount().compareTo(expectedAmount) != 0) {
                        throw new IllegalArgumentException("Số tiền thanh toán phải bằng tổng tiền hóa đơn.");
                    }
                }

                insert(connection, payment);
                if (Payment8.STATUS_SUCCESS.equals(payment.getPaymentStatus())) {
                    updateReceiptStatus(connection, importId, ImportReceipt8.STATUS_PAID);
                }
                connection.commit();
                return payment;
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw new SQLException("Không thể ghi nhận thanh toán; transaction đã được rollback.", exception);
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public boolean hasSuccessfulPayment(long invoiceId) throws SQLException {
        try (Connection connection = open()) {
            return hasSuccessfulPayment(connection, invoiceId);
        }
    }

    public List<Payment8> findByInvoiceId(long invoiceId) throws SQLException {
        String sql = "SELECT paymentId, invoiceId, paymentDate, amount, paymentMethod, paymentStatus "
                + "FROM tblPayment8 WHERE invoiceId = ? ORDER BY paymentId DESC";
        List<Payment8> payments = new ArrayList<>();
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, invoiceId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Payment8 payment = new Payment8();
                    payment.setPaymentId(result.getLong("paymentId"));
                    payment.setInvoiceId(result.getLong("invoiceId"));
                    payment.setPaymentDate(result.getObject("paymentDate", LocalDate.class));
                    payment.setAmount(result.getBigDecimal("amount"));
                    payment.setPaymentMethod(result.getString("paymentMethod"));
                    payment.setPaymentStatus(result.getString("paymentStatus"));
                    payments.add(payment);
                }
            }
        }
        return payments;
    }

    private long lockInvoiceAndGetImportId(Connection connection, long invoiceId) throws SQLException {
        String sql = "SELECT importId FROM tblInvoice8 WHERE invoiceId = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, invoiceId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("Hóa đơn không tồn tại.");
                }
                return result.getLong("importId");
            }
        }
    }

    private BigDecimal findInvoiceTotal(Connection connection, long importId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * price), 0) AS totalAmount FROM tblImportDetail8 WHERE importId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, importId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBigDecimal("totalAmount");
            }
        }
    }

    private boolean hasSuccessfulPayment(Connection connection, long invoiceId) throws SQLException {
        String sql = "SELECT 1 FROM tblPayment8 WHERE invoiceId = ? AND paymentStatus = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, invoiceId);
            statement.setString(2, Payment8.STATUS_SUCCESS);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void insert(Connection connection, Payment8 payment) throws SQLException {
        String sql = "INSERT INTO tblPayment8 (invoiceId, paymentDate, amount, paymentMethod, paymentStatus) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, payment.getInvoiceId());
            statement.setObject(2, payment.getPaymentDate());
            statement.setBigDecimal(3, payment.getAmount());
            statement.setString(4, payment.getPaymentMethod());
            statement.setString(5, payment.getPaymentStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setPaymentId(keys.getLong(1));
                    return;
                }
            }
        }
        throw new SQLException("Không lấy được mã thanh toán sau khi lưu.");
    }

    private void updateReceiptStatus(Connection connection, long importId, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE tblImportReceipt8 SET status = ? WHERE importId = ?")) {
            statement.setString(1, status);
            statement.setLong(2, importId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Không thể cập nhật trạng thái phiếu nhập.");
            }
        }
    }

    private void validatePayment(Payment8 payment) {
        if (payment == null || payment.getInvoiceId() == null || payment.getPaymentDate() == null
                || !ValidationUtil8.isValidMoney(payment.getAmount())
                || !isAllowedMethod(payment.getPaymentMethod()) || !isAllowedStatus(payment.getPaymentStatus())) {
            throw new IllegalArgumentException("Dữ liệu thanh toán không hợp lệ.");
        }
    }

    private boolean isAllowedMethod(String value) {
        return Payment8.METHOD_CASH.equals(value) || Payment8.METHOD_BANK_TRANSFER.equals(value);
    }

    private boolean isAllowedStatus(String value) {
        return Payment8.STATUS_SUCCESS.equals(value) || Payment8.STATUS_FAILED.equals(value);
    }
}
