package libman;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import libman.config.DatabaseConfig8;
import libman.dao.DocumentDAO8;
import libman.dao.ImportReceiptDAO8;
import libman.dao.InvoiceDAO8;
import libman.dao.PaymentDAO8;
import libman.dao.SupplierDAO8;
import libman.model.Document8;
import libman.model.ImportDetail8;
import libman.model.ImportReceipt8;
import libman.model.Invoice8;
import libman.model.Payment8;
import libman.model.Supplier8;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kiểm thử ghi dữ liệu trên MySQL thật. Chỉ chạy khi DB có hậu tố _test và
 * LIBMAN_RUN_MUTATING_DB_TESTS=true để tuyệt đối không chạm database người dùng.
 */
class ImportDocumentIntegrationTest8 {
    private final Set<String> submissionTokens = ConcurrentHashMap.newKeySet();
    private final List<Long> supplierIds = new ArrayList<>();
    private final List<String> documentIds = new ArrayList<>();

    @BeforeEach
    void requireDedicatedTestDatabase() {
        Assumptions.assumeTrue(isMutatingTestEnabled(),
                "Bỏ qua test ghi dữ liệu: cần DB *_test và LIBMAN_RUN_MUTATING_DB_TESTS=true.");
    }

    @AfterEach
    void cleanOnlyRowsCreatedByThisTest() throws Exception {
        if (!isMutatingTestEnabled()) {
            return;
        }
        try (Connection connection = DatabaseConfig8.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (String token : submissionTokens) {
                    Long importId = nullableLong(connection,
                            "SELECT importId FROM tblImportReceipt8 WHERE submissionToken = ?", token);
                    if (importId != null) {
                        execute(connection, "DELETE p FROM tblPayment8 p JOIN tblInvoice8 i ON i.invoiceId = p.invoiceId WHERE i.importId = ?", importId);
                        execute(connection, "DELETE FROM tblInvoice8 WHERE importId = ?", importId);
                        execute(connection, "DELETE c FROM tblDocumentCopy8 c JOIN tblImportDetail8 d ON d.importDetailId = c.importDetailId WHERE d.importId = ?", importId);
                        execute(connection, "DELETE FROM tblImportDetail8 WHERE importId = ?", importId);
                        execute(connection, "DELETE FROM tblImportReceipt8 WHERE importId = ?", importId);
                    }
                }
                for (String documentId : documentIds) {
                    execute(connection, "DELETE FROM tblDocument8 WHERE documentId = ?", documentId);
                }
                for (Long supplierId : supplierIds) {
                    execute(connection, "DELETE FROM tblSupplier8 WHERE supplierId = ?", supplierId);
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    @Test
    void addsAndFindsSupplierAndDocument() throws Exception {
        String suffix = shortId();
        Supplier8 supplier = new Supplier8();
        supplier.setSupplierName("Nhà cung cấp Phase 5 " + suffix);
        supplier.setAddress("Hà Nội");
        supplier.setPhone("090" + suffix);
        supplier.setEmail("phase5-" + suffix + "@example.test");
        new SupplierDAO8().insert(supplier);
        supplierIds.add(supplier.getSupplierId());
        assertEquals(supplier.getSupplierId(), new SupplierDAO8().searchByName(suffix).get(0).getSupplierId());

        String documentId = "P5-" + suffix;
        documentIds.add(documentId);
        Document8 document = new Document8();
        document.setDocumentId(documentId);
        document.setTitle("Tài liệu kiểm thử " + suffix);
        document.setCategory("Giáo trình");
        document.setAuthor("QA LibMan");
        document.setPublisher("NXB Kiểm thử");
        document.setPublishYear(2026);
        new DocumentDAO8().insert(document);
        assertEquals(document.getTitle(), new DocumentDAO8().findByCode(documentId).orElseThrow().getTitle());
    }

    @Test
    void savesMultipleDetailsCopiesAndInvoiceAtomicallyAndIdempotently() throws Exception {
        String token = token();
        ImportReceiptDAO8 dao = new ImportReceiptDAO8();
        ImportReceipt8 first = dao.saveReceiptWithDetailsAndInvoice(receipt(token,
                line("S-001", 3, "12.34"), line("GT-016", 2, "100.00")));
        ImportReceipt8 duplicate = dao.saveReceiptWithDetailsAndInvoice(receipt(token,
                line("S-001", 3, "12.34"), line("GT-016", 2, "100.00")));

        assertEquals(first.getImportId(), duplicate.getImportId());
        assertEquals(1, count("SELECT COUNT(*) FROM tblImportReceipt8 WHERE submissionToken = ?", token));
        assertEquals(2, count("SELECT COUNT(*) FROM tblImportDetail8 WHERE importId = ?", first.getImportId()));
        assertEquals(5, count("SELECT COUNT(*) FROM tblDocumentCopy8 c JOIN tblImportDetail8 d ON d.importDetailId = c.importDetailId WHERE d.importId = ?", first.getImportId()));
        assertEquals(1, count("SELECT COUNT(*) FROM tblInvoice8 WHERE importId = ?", first.getImportId()));
        assertEquals(new BigDecimal("237.02"), new InvoiceDAO8().findByImportId(first.getImportId())
                .orElseThrow().getTotalAmount());
    }

    @Test
    void concurrentDuplicateSubmitCreatesOnlyOneAggregate() throws Exception {
        String token = token();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Long> first = pool.submit(() -> saveAfterSignal(start, token));
            Future<Long> second = pool.submit(() -> saveAfterSignal(start, token));
            start.countDown();
            assertEquals(first.get(), second.get());
        } finally {
            pool.shutdownNow();
        }
        Long importId = nullableLong("SELECT importId FROM tblImportReceipt8 WHERE submissionToken = ?", token);
        assertNotNull(importId);
        assertEquals(1, count("SELECT COUNT(*) FROM tblImportReceipt8 WHERE submissionToken = ?", token));
        assertEquals(2, count("SELECT COUNT(*) FROM tblDocumentCopy8 c JOIN tblImportDetail8 d ON d.importDetailId = c.importDetailId WHERE d.importId = ?", importId));
        assertEquals(1, count("SELECT COUNT(*) FROM tblInvoice8 WHERE importId = ?", importId));
    }

    @Test
    void rollsBackWholeAggregateAndRejectsInvalidMoneyOrEmptyReceipt() throws Exception {
        String rollbackToken = token();
        assertThrows(Exception.class, () -> new ImportReceiptDAO8().saveReceiptWithDetailsAndInvoice(
                receipt(rollbackToken, line("MISSING-P5", 1, "10.00"))));
        assertEquals(0, count("SELECT COUNT(*) FROM tblImportReceipt8 WHERE submissionToken = ?", rollbackToken));

        String zeroToken = token();
        assertThrows(IllegalArgumentException.class, () -> new ImportReceiptDAO8()
                .saveReceiptWithDetailsAndInvoice(receipt(zeroToken, line("S-001", 0, "10.00"))));
        String negativeToken = token();
        assertThrows(IllegalArgumentException.class, () -> new ImportReceiptDAO8()
                .saveReceiptWithDetailsAndInvoice(receipt(negativeToken, line("S-001", 1, "-0.01"))));
        String overflowToken = token();
        assertThrows(IllegalArgumentException.class, () -> new ImportReceiptDAO8()
                .saveReceiptWithDetailsAndInvoice(receipt(overflowToken, line("S-001", 1, "10000000000.00"))));
        String emptyToken = token();
        assertThrows(IllegalArgumentException.class, () -> new ImportReceiptDAO8()
                .saveReceiptWithDetailsAndInvoice(receipt(emptyToken)));
    }

    @Test
    void recordsFailedThenSuccessfulPaymentAndBlocksLaterAttempts() throws Exception {
        ImportReceipt8 saved = new ImportReceiptDAO8().saveReceiptWithDetailsAndInvoice(
                receipt(token(), line("S-001", 2, "25.50")));
        Invoice8 invoice = new InvoiceDAO8().findByImportId(saved.getImportId()).orElseThrow();
        PaymentDAO8 dao = new PaymentDAO8();
        dao.recordManualPayment(payment(invoice.getInvoiceId(), invoice.getTotalAmount(), Payment8.STATUS_FAILED));
        assertEquals("UNPAID", value("SELECT status FROM tblImportReceipt8 WHERE importId = ?", saved.getImportId()));
        dao.recordManualPayment(payment(invoice.getInvoiceId(), invoice.getTotalAmount(), Payment8.STATUS_SUCCESS));
        assertEquals("PAID", value("SELECT status FROM tblImportReceipt8 WHERE importId = ?", saved.getImportId()));
        assertThrows(Exception.class, () -> dao.recordManualPayment(
                payment(invoice.getInvoiceId(), invoice.getTotalAmount(), Payment8.STATUS_SUCCESS)));
        assertThrows(Exception.class, () -> dao.recordManualPayment(
                payment(invoice.getInvoiceId(), invoice.getTotalAmount(), Payment8.STATUS_FAILED)));
        assertEquals(1, count("SELECT COUNT(*) FROM tblPayment8 WHERE invoiceId = ? AND paymentStatus = 'SUCCESS'", invoice.getInvoiceId()));
    }

    @Test
    void concurrentSuccessfulPaymentAllowsExactlyOneRequest() throws Exception {
        ImportReceipt8 saved = new ImportReceiptDAO8().saveReceiptWithDetailsAndInvoice(
                receipt(token(), line("S-001", 1, "75.00")));
        Invoice8 invoice = new InvoiceDAO8().findByImportId(saved.getImportId()).orElseThrow();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        int successes = 0;
        try {
            List<Future<Boolean>> results = List.of(
                    pool.submit(() -> payAfterSignal(start, invoice)),
                    pool.submit(() -> payAfterSignal(start, invoice)));
            start.countDown();
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, successes);
        assertEquals(1, count("SELECT COUNT(*) FROM tblPayment8 WHERE invoiceId = ? AND paymentStatus = 'SUCCESS'", invoice.getInvoiceId()));
        assertEquals("PAID", value("SELECT status FROM tblImportReceipt8 WHERE importId = ?", saved.getImportId()));
    }

    private long saveAfterSignal(CountDownLatch start, String token) throws Exception {
        start.await();
        return new ImportReceiptDAO8().saveReceiptWithDetailsAndInvoice(
                receipt(token, line("S-001", 2, "20.00"))).getImportId();
    }

    private boolean payAfterSignal(CountDownLatch start, Invoice8 invoice) throws Exception {
        start.await();
        try {
            new PaymentDAO8().recordManualPayment(
                    payment(invoice.getInvoiceId(), invoice.getTotalAmount(), Payment8.STATUS_SUCCESS));
            return true;
        } catch (Exception expectedForOneRequest) {
            return false;
        }
    }

    private ImportReceipt8 receipt(String token, ImportDetail8... details) {
        submissionTokens.add(token);
        ImportReceipt8 receipt = new ImportReceipt8();
        receipt.setSupplierId(1L);
        receipt.setLibrarianId(1L);
        receipt.setImportDate(LocalDate.now());
        receipt.setSubmissionToken(token);
        receipt.setDetails(List.of(details));
        return receipt;
    }

    private ImportDetail8 line(String documentId, int quantity, String price) {
        ImportDetail8 detail = new ImportDetail8();
        detail.setDocumentId(documentId);
        detail.setQuantity(quantity);
        detail.setPrice(new BigDecimal(price));
        return detail;
    }

    private Payment8 payment(long invoiceId, BigDecimal amount, String status) {
        Payment8 payment = new Payment8();
        payment.setInvoiceId(invoiceId);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setPaymentMethod(Payment8.METHOD_CASH);
        payment.setPaymentStatus(status);
        return payment;
    }

    private String token() {
        String token = UUID.randomUUID().toString();
        submissionTokens.add(token);
        return token;
    }

    private String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private int count(String sql, Object parameter) throws Exception {
        try (Connection connection = DatabaseConfig8.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private String value(String sql, Object parameter) throws Exception {
        try (Connection connection = DatabaseConfig8.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getString(1);
            }
        }
    }

    private Long nullableLong(String sql, Object parameter) throws Exception {
        try (Connection connection = DatabaseConfig8.getConnection()) {
            return nullableLong(connection, sql, parameter);
        }
    }

    private Long nullableLong(Connection connection, String sql, Object parameter) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : null;
            }
        }
    }

    private void execute(Connection connection, String sql, Object parameter) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            statement.executeUpdate();
        }
    }

    private boolean isMutatingTestEnabled() {
        return "true".equalsIgnoreCase(System.getenv("LIBMAN_RUN_MUTATING_DB_TESTS"))
                && DatabaseConfig8.getDatabaseName().endsWith("_test")
                && System.getenv("LIBMAN_DB_USER") != null
                && System.getenv("LIBMAN_DB_PASSWORD") != null;
    }
}
