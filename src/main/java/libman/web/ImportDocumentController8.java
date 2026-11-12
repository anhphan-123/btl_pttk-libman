package libman.web;

import libman.dao.DocumentDAO8;
import libman.dao.ImportReceiptDAO8;
import libman.dao.ImportDetailDAO8;
import libman.dao.InvoiceDAO8;
import libman.dao.LibrarianDAO8;
import libman.dao.PaymentDAO8;
import libman.dao.SupplierDAO8;
import libman.model.Document8;
import libman.model.ImportDetail8;
import libman.model.ImportReceipt8;
import libman.model.Invoice8;
import libman.model.Librarian8;
import libman.model.Payment8;
import libman.model.Supplier8;
import libman.util.SubmissionTokenUtil8;
import libman.util.ValidationUtil8;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/import-document-8")
public class ImportDocumentController8 extends HttpServlet {
    private static final String SUPPLIER_VIEW = "/WEB-INF/jsp/FindSupplierUI8.jsp";
    private static final String ADD_SUPPLIER_VIEW = "/WEB-INF/jsp/AddSupplierUI8.jsp";
    private static final String RECEIPT_VIEW = "/WEB-INF/jsp/ImportReceiptUI8.jsp";
    private static final String ADD_DOCUMENT_VIEW = "/WEB-INF/jsp/AddDocumentUI8.jsp";
    private static final String INVOICE_VIEW = "/WEB-INF/jsp/InvoiceUI8.jsp";
    private static final String PAYMENT_VIEW = "/WEB-INF/jsp/PaymentUI8.jsp";

    private static final String SESSION_SUPPLIER_ID = "libman.import.supplierId8";
    private static final String SESSION_LIBRARIAN_ID = "libman.import.librarianId8";
    private static final String SESSION_DETAILS = "libman.import.details8";
    private static final String SESSION_TOKEN = "libman.import.submissionToken8";
    private static final String SESSION_CSRF_TOKEN = "libman.csrfToken8";
    private static final long DEMO_LIBRARIAN_ID = 1L;

    private final SupplierDAO8 supplierDAO8 = new SupplierDAO8();
    private final DocumentDAO8 documentDAO8 = new DocumentDAO8();
    private final LibrarianDAO8 librarianDAO8 = new LibrarianDAO8();
    private final ImportReceiptDAO8 importReceiptDAO8 = new ImportReceiptDAO8();
    private final ImportDetailDAO8 importDetailDAO8 = new ImportDetailDAO8();
    private final InvoiceDAO8 invoiceDAO8 = new InvoiceDAO8();
    private final PaymentDAO8 paymentDAO8 = new PaymentDAO8();

    public List<Supplier8> searchSupplier(String name) throws SQLException {
        if (!ValidationUtil8.hasText(name) || name.trim().length() > 200) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống và tối đa 200 ký tự.");
        }
        return supplierDAO8.searchByName(name.trim());
    }

    public Supplier8 addSupplier(Supplier8 supplier) throws SQLException {
        validateSupplier(supplier);
        return supplierDAO8.insert(supplier);
    }

    public Optional<Document8> findDocumentByCode(String code) throws SQLException {
        if (code == null || !code.trim().matches("[A-Za-z0-9][A-Za-z0-9_-]{0,29}")) {
            throw new IllegalArgumentException("Mã tài liệu không hợp lệ.");
        }
        return documentDAO8.findByCode(code.trim());
    }

    public Document8 addDocument(Document8 document) throws SQLException {
        validateDocument(document);
        if (documentDAO8.findByCode(document.getDocumentId()).isPresent()) {
            throw new IllegalArgumentException("Mã tài liệu đã tồn tại.");
        }
        return documentDAO8.insert(document);
    }

    public ImportDetail8 addItem(HttpSession session, String code, int quantity, BigDecimal price)
            throws SQLException {
        Optional<Document8> document = findDocumentByCode(code);
        if (document.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy tài liệu với mã đã nhập.");
        }
        validateLine(quantity, price);
        ImportDetail8 detail = new ImportDetail8();
        detail.setDocumentId(document.get().getDocumentId());
        detail.setQuantity(quantity);
        detail.setPrice(price);
        synchronized (session) {
            details(session).add(detail);
        }
        return detail;
    }

    public ImportReceipt8 saveImport(long supplierId, long librarianId, List<ImportDetail8> items)
            throws SQLException {
        return saveImport(supplierId, librarianId, items, SubmissionTokenUtil8.create());
    }

    private ImportReceipt8 saveImport(long supplierId, long librarianId, List<ImportDetail8> items,
                                      String submissionToken) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một dòng chi tiết.");
        }
        if (supplierId <= 0 || librarianId <= 0) {
            throw new IllegalArgumentException("Nhà cung cấp hoặc thủ thư không hợp lệ.");
        }
        if (librarianDAO8.findById(librarianId).isEmpty()) {
            throw new IllegalArgumentException("Thủ thư không tồn tại.");
        }
        if (supplierDAO8.findById(supplierId).isEmpty()) {
            throw new IllegalArgumentException("Nhà cung cấp không tồn tại.");
        }
        List<ImportDetail8> copy = new ArrayList<>();
        for (ImportDetail8 item : items) {
            if (item == null || item.getDocumentId() == null || item.getDocumentId().isBlank()) {
                throw new IllegalArgumentException("Dòng chi tiết thiếu mã tài liệu.");
            }
            validateLine(item.getQuantity(), item.getPrice());
            if (findDocumentByCode(item.getDocumentId()).isEmpty()) {
                throw new IllegalArgumentException("Tài liệu " + item.getDocumentId() + " không tồn tại.");
            }
            ImportDetail8 copiedItem = new ImportDetail8();
            copiedItem.setDocumentId(item.getDocumentId());
            copiedItem.setQuantity(item.getQuantity());
            copiedItem.setPrice(item.getPrice());
            copy.add(copiedItem);
        }
        ImportReceipt8 receipt = new ImportReceipt8();
        receipt.setSupplierId(supplierId);
        receipt.setLibrarianId(librarianId);
        receipt.setImportDate(LocalDate.now());
        receipt.setSubmissionToken(submissionToken);
        receipt.setDetails(copy);
        return importReceiptDAO8.saveReceiptWithDetailsAndInvoice(receipt);
    }

    public Payment8 recordPayment(long invoiceId, String method, BigDecimal amount, String status)
            throws SQLException {
        if (invoiceId <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }
        Payment8 payment = new Payment8();
        payment.setInvoiceId(invoiceId);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(method);
        payment.setAmount(amount);
        payment.setPaymentStatus(status == null || status.isBlank() ? Payment8.STATUS_SUCCESS : status);
        return paymentDAO8.recordManualPayment(payment);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = trim(request.getParameter("action"));
        try {
            switch (action == null ? "supplier" : action) {
                case "supplier", "search-supplier" -> showSupplier(request, response, null, null);
                case "add-supplier" -> showAddSupplier(request, response);
                case "receipt" -> showReceipt(request, response);
                case "add-document" -> showAddDocument(request, response);
                case "invoice" -> showInvoice(request, response);
                case "payment" -> showPayment(request, response);
                default -> {
                    request.setAttribute("error", "Thao tác không hợp lệ.");
                    showSupplier(request, response, null, null);
                }
            }
        } catch (SQLException exception) {
            log("Lỗi truy vấn module Import Document 8.", exception);
            request.setAttribute("error", "Không thể kết nối hoặc truy vấn cơ sở dữ liệu. Vui lòng thử lại.");
            request.setAttribute("keyword", "");
            request.setAttribute("suppliers", Collections.emptyList());
            forward(request, response, SUPPLIER_VIEW);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            request.setAttribute("keyword", "");
            request.setAttribute("suppliers", Collections.emptyList());
            forward(request, response, SUPPLIER_VIEW);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isValidCsrfToken(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Yêu cầu không hợp lệ hoặc phiên làm việc đã hết hạn.");
            return;
        }
        String action = trim(request.getParameter("action"));
        try {
            switch (action == null ? "" : action) {
                case "search-supplier" -> handleSearchSupplier(request, response);
                case "add-supplier" -> handleAddSupplier(request, response);
                case "open-receipt" -> handleOpenReceipt(request, response);
                case "find-document" -> handleFindDocument(request, response);
                case "add-document" -> handleAddDocument(request, response);
                case "add-item" -> handleAddItem(request, response);
                case "edit-item" -> handleEditItem(request, response);
                case "remove-item" -> handleRemoveItem(request, response);
                case "submit-import" -> handleSubmitImport(request, response);
                case "record-payment" -> handleRecordPayment(request, response);
                default -> {
                    request.setAttribute("error", "Thao tác không hợp lệ.");
                    showSupplier(request, response, null, null);
                }
            }
        } catch (SQLIntegrityConstraintViolationException exception) {
            request.setAttribute("error", "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc cơ sở dữ liệu.");
            routeAfterError(request, response, action);
        } catch (SQLException exception) {
            log("Lỗi ghi dữ liệu module Import Document 8.", exception);
            request.setAttribute("error", userMessage(exception));
            routeAfterError(request, response, action);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            routeAfterError(request, response, action);
        }
    }

    private void handleSearchSupplier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String keyword = trim(request.getParameter("keyword"));
        if (!ValidationUtil8.hasText(keyword)) {
            request.setAttribute("error", "Vui lòng nhập tên nhà cung cấp.");
            request.setAttribute("searchedSupplier", true);
            showSupplier(request, response, keyword, null);
            return;
        }
        request.setAttribute("searchedSupplier", true);
        showSupplier(request, response, keyword, searchSupplier(keyword));
    }

    private void handleAddSupplier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        Supplier8 supplier = new Supplier8();
        supplier.setSupplierName(trim(request.getParameter("supplierName")));
        supplier.setAddress(trim(request.getParameter("address")));
        supplier.setPhone(trim(request.getParameter("phone")));
        supplier.setEmail(trim(request.getParameter("email")));
        Supplier8 saved = addSupplier(supplier);
        response.sendRedirect(request.getContextPath() + "/import-document-8?action=supplier&selectedSupplierId="
                + saved.getSupplierId());
    }

    private void handleOpenReceipt(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        long supplierId = requiredPositiveLong(request.getParameter("supplierId"), "Nhà cung cấp");
        long librarianId = resolveLibrarianId(request);
        if (supplierDAO8.findById(supplierId).isEmpty()) {
            throw new IllegalArgumentException("Nhà cung cấp không tồn tại.");
        }
        if (librarianDAO8.findById(librarianId).isEmpty()) {
            throw new IllegalArgumentException("Thủ thư demo không tồn tại.");
        }
        HttpSession session = request.getSession();
        synchronized (session) {
            session.setAttribute(SESSION_SUPPLIER_ID, supplierId);
            session.setAttribute(SESSION_LIBRARIAN_ID, librarianId);
            session.setAttribute(SESSION_DETAILS, new ArrayList<ImportDetail8>());
            session.setAttribute(SESSION_TOKEN, SubmissionTokenUtil8.create());
        }
        response.sendRedirect(request.getContextPath() + "/import-document-8?action=receipt");
    }

    private void handleFindDocument(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String code = trim(request.getParameter("documentCode"));
        Optional<Document8> result = findDocumentByCode(code);
        request.setAttribute("searchedDocumentCode", code == null ? "" : code);
        if (result.isPresent()) {
            request.setAttribute("foundDocument", result.get());
        } else {
            request.setAttribute("error", "Không tìm thấy tài liệu. Bạn có thể thêm tài liệu mới.");
        }
        showReceipt(request, response);
    }

    private void handleAddDocument(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Document8 document = new Document8();
        document.setDocumentId(trim(request.getParameter("documentId")));
        document.setTitle(trim(request.getParameter("title")));
        document.setCategory(trim(request.getParameter("category")));
        document.setAuthor(trim(request.getParameter("author")));
        document.setPublisher(trim(request.getParameter("publisher")));
        document.setPublishYear(parseYear(request.getParameter("publishYear")));
        Document8 saved = addDocument(document);
        response.sendRedirect(request.getContextPath() + "/import-document-8?action=receipt&documentId="
                + saved.getDocumentId());
    }

    private void handleAddItem(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        String code = trim(request.getParameter("documentCode"));
        int quantity = parseQuantity(request.getParameter("quantity"));
        BigDecimal price = parsePrice(request.getParameter("price"));
        addItem(request.getSession(), code, quantity, price);
        response.sendRedirect(request.getContextPath() + "/import-document-8?action=receipt&documentId=" + code);
    }

    private void handleEditItem(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int index = parseIndex(request.getParameter("index"));
        int quantity = parseQuantity(request.getParameter("quantity"));
        BigDecimal price = parsePrice(request.getParameter("price"));
        validateLine(quantity, price);
        HttpSession session = request.getSession();
        synchronized (session) {
            List<ImportDetail8> details = details(session);
            if (index < 0 || index >= details.size()) {
                throw new IllegalArgumentException("Dòng chi tiết không tồn tại.");
            }
            ImportDetail8 detail = details.get(index);
            detail.setQuantity(quantity);
            detail.setPrice(price);
        }
        response.sendRedirect(request.getContextPath() + "/import-document-8?action=receipt");
    }

    private void handleRemoveItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int index = parseIndex(request.getParameter("index"));
        HttpSession session = request.getSession();
        synchronized (session) {
            List<ImportDetail8> details = details(session);
            if (index < 0 || index >= details.size()) {
                throw new IllegalArgumentException("Dòng chi tiết không tồn tại.");
            }
            details.remove(index);
        }
        response.sendRedirect(request.getContextPath() + "/import-document-8?action=receipt");
    }

    private void handleSubmitImport(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        HttpSession session = request.getSession();
        Invoice8 invoice;
        synchronized (session) {
            Long supplierId = sessionLong(session, SESSION_SUPPLIER_ID);
            Long librarianId = sessionLong(session, SESSION_LIBRARIAN_ID);
            List<ImportDetail8> currentDetails = new ArrayList<>(details(session));
            if (supplierId == null || librarianId == null) {
                throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp và thủ thư trước khi Submit.");
            }
            String token = (String) session.getAttribute(SESSION_TOKEN);
            if (!ValidationUtil8.hasText(token)) {
                token = SubmissionTokenUtil8.create();
                session.setAttribute(SESSION_TOKEN, token);
            }
            ImportReceipt8 saved = saveImport(supplierId, librarianId, currentDetails, token);
            invoice = invoiceDAO8.findByImportId(saved.getImportId())
                    .orElseThrow(() -> new SQLException("Đã lưu phiếu nhưng không tìm thấy hóa đơn tương ứng."));
            session.removeAttribute(SESSION_DETAILS);
            session.removeAttribute(SESSION_TOKEN);
        }
        response.sendRedirect(request.getContextPath() + "/import-document-8?action=invoice&invoiceId="
                + invoice.getInvoiceId());
    }

    private void handleRecordPayment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        long invoiceId = requiredPositiveLong(request.getParameter("invoiceId"), "Hóa đơn");
        String method = trim(request.getParameter("paymentMethod"));
        String status = trim(request.getParameter("paymentStatus"));
        BigDecimal amount = parsePrice(request.getParameter("amount"));
        try {
            recordPayment(invoiceId, method, amount, status);
            String effectiveStatus = status == null || status.isBlank() ? Payment8.STATUS_SUCCESS : status;
            request.setAttribute("success", Payment8.STATUS_SUCCESS.equals(effectiveStatus)
                    ? "Ghi nhận thanh toán thành công." : "Đã lưu lần thanh toán thất bại để theo dõi.");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
        } catch (SQLException exception) {
            request.setAttribute("error", userMessage(exception));
        }
        showPayment(request, response, invoiceId);
    }

    private void showSupplier(HttpServletRequest request, HttpServletResponse response,
                              String keyword, List<Supplier8> result)
            throws ServletException, IOException, SQLException {
        String selected = trim(request.getParameter("selectedSupplierId"));
        if (selected != null && selected.matches("\\d+")) {
            supplierDAO8.findById(Long.parseLong(selected)).ifPresent(s -> request.setAttribute("selectedSupplier", s));
        }
        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("suppliers", result == null ? Collections.emptyList() : result);
        if (request.getAttribute("searchedSupplier") == null) {
            request.setAttribute("searchedSupplier", false);
        }
        forward(request, response, SUPPLIER_VIEW);
    }

    private void showAddSupplier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, ADD_SUPPLIER_VIEW);
    }

    private void showReceipt(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        HttpSession session = request.getSession();
        long supplierId = requiredSessionLong(session, SESSION_SUPPLIER_ID, "Nhà cung cấp");
        long librarianId = resolveLibrarianId(request);
        Supplier8 supplier = supplierDAO8.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp không tồn tại."));
        Librarian8 librarian = librarianDAO8.findById(librarianId)
                .orElseThrow(() -> new IllegalArgumentException("Thủ thư không tồn tại."));
        request.setAttribute("supplier", supplier);
        request.setAttribute("librarian", librarian);
        List<ImportDetail8> current;
        synchronized (session) {
            session.setAttribute(SESSION_LIBRARIAN_ID, librarianId);
            current = new ArrayList<>(details(session));
        }
        request.setAttribute("details", current);
        request.setAttribute("totalAmount", current.stream().map(ImportDetail8::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Map<String, Document8> documents = new LinkedHashMap<>();
        List<String> documentIds = current.stream().map(ImportDetail8::getDocumentId).toList();
        for (Document8 document : documentDAO8.findByIds(documentIds)) {
            documents.put(document.getDocumentId(), document);
        }
        request.setAttribute("documentsByCode", documents);
        String documentId = trim(request.getParameter("documentId"));
        if (documentId != null) {
            findDocumentByCode(documentId).ifPresent(document -> request.setAttribute("foundDocument", document));
        }
        forward(request, response, RECEIPT_VIEW);
    }

    private void showAddDocument(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("documentId", trim(request.getParameter("documentId")));
        forward(request, response, ADD_DOCUMENT_VIEW);
    }

    private void showInvoice(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        long invoiceId = requiredPositiveLong(request.getParameter("invoiceId"), "Hóa đơn");
        Invoice8 invoice = invoiceDAO8.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại."));
        request.setAttribute("invoice", invoice);
        request.setAttribute("details", importDetailDAO8.findByImportId(invoice.getImportId()));
        request.setAttribute("payments", paymentDAO8.findByInvoiceId(invoiceId));
        request.setAttribute("paid", paymentDAO8.hasSuccessfulPayment(invoiceId));
        forward(request, response, INVOICE_VIEW);
    }

    private void showPayment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        showPayment(request, response, requiredPositiveLong(request.getParameter("invoiceId"), "Hóa đơn"));
    }

    private void showPayment(HttpServletRequest request, HttpServletResponse response, long invoiceId)
            throws ServletException, IOException, SQLException {
        Invoice8 invoice = invoiceDAO8.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại."));
        request.setAttribute("invoice", invoice);
        request.setAttribute("payments", paymentDAO8.findByInvoiceId(invoiceId));
        request.setAttribute("paid", paymentDAO8.hasSuccessfulPayment(invoiceId));
        forward(request, response, PAYMENT_VIEW);
    }

    private void routeAfterError(HttpServletRequest request, HttpServletResponse response, String action)
            throws ServletException, IOException {
        try {
            if ("add-supplier".equals(action)) {
                showAddSupplier(request, response);
            } else if ("add-document".equals(action)) {
                showAddDocument(request, response);
            } else if ("record-payment".equals(action)) {
                showPayment(request, response);
            } else if ("find-document".equals(action) || "add-item".equals(action)
                    || "edit-item".equals(action) || "remove-item".equals(action)
                    || "submit-import".equals(action)) {
                showReceipt(request, response);
            } else {
                showSupplier(request, response, trim(request.getParameter("keyword")), null);
            }
        } catch (SQLException | IllegalArgumentException exception) {
            request.setAttribute("error", userMessage(exception));
            request.setAttribute("keyword", "");
            request.setAttribute("suppliers", Collections.emptyList());
            forward(request, response, SUPPLIER_VIEW);
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.setAttribute("csrfToken", csrfToken(request.getSession()));
        response.setHeader("Cache-Control", "no-store");
        RequestDispatcher dispatcher = request.getRequestDispatcher(view);
        dispatcher.forward(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<ImportDetail8> details(HttpSession session) {
        Object value = session.getAttribute(SESSION_DETAILS);
        if (value instanceof List<?>) {
            return (List<ImportDetail8>) value;
        }
        List<ImportDetail8> created = new ArrayList<>();
        session.setAttribute(SESSION_DETAILS, created);
        return created;
    }

    private long resolveLibrarianId(HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_LIBRARIAN_ID, DEMO_LIBRARIAN_ID);
        return DEMO_LIBRARIAN_ID;
    }

    private long requiredSessionLong(HttpSession session, String key, String label) {
        Long value = sessionLong(session, key);
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Chưa chọn " + label + ".");
        }
        return value;
    }

    private Long sessionLong(HttpSession session, String key) {
        Object value = session.getAttribute(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private long requiredPositiveLong(String raw, String label) {
        try {
            long value = Long.parseLong(raw);
            if (value > 0) {
                return value;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException(label + " không hợp lệ.");
    }

    private int parseQuantity(String raw) {
        try {
            int quantity = Integer.parseInt(raw);
            if (quantity > 0) {
                return quantity;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Số lượng phải là số nguyên dương.");
    }

    private BigDecimal parsePrice(String raw) {
        try {
            BigDecimal price = new BigDecimal(raw == null ? "" : raw.trim().replace(',', '.'));
            if (ValidationUtil8.isValidMoney(price)) {
                return price;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Số tiền phải từ 0 đến 9.999.999.999,99 và tối đa 2 chữ số thập phân.");
    }

    private int parseYear(String raw) {
        try {
            int year = Integer.parseInt(raw);
            if (year >= 1000 && year <= 9999) {
                return year;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Năm xuất bản phải nằm trong khoảng 1000-9999.");
    }

    private int parseIndex(String raw) {
        try {
            int index = Integer.parseInt(raw);
            if (index >= 0) {
                return index;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Dòng chi tiết không hợp lệ.");
    }

    private void validateLine(Integer quantity, BigDecimal price) {
        if (!ValidationUtil8.isPositiveQuantity(quantity)) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên dương.");
        }
        if (!ValidationUtil8.isValidMoney(price)) {
            throw new IllegalArgumentException("Đơn giá phải từ 0 đến 9.999.999.999,99 và tối đa 2 chữ số thập phân.");
        }
    }

    private String csrfToken(HttpSession session) {
        synchronized (session) {
            Object existing = session.getAttribute(SESSION_CSRF_TOKEN);
            if (existing instanceof String token && ValidationUtil8.hasText(token)) {
                return token;
            }
            String created = SubmissionTokenUtil8.create();
            session.setAttribute(SESSION_CSRF_TOKEN, created);
            return created;
        }
    }

    private boolean isValidCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object expected = session == null ? null : session.getAttribute(SESSION_CSRF_TOKEN);
        String supplied = request.getParameter("csrfToken");
        return expected instanceof String expectedToken
                && supplied != null
                && MessageDigest.isEqual(expectedToken.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
    }

    private void validateSupplier(Supplier8 supplier) {
        if (supplier == null || !hasLength(supplier.getSupplierName(), 1, 200)
                || !hasLength(supplier.getAddress(), 1, 65535)
                || !hasLength(supplier.getPhone(), 1, 20)
                || !hasLength(supplier.getEmail(), 1, 255)) {
            throw new IllegalArgumentException("Vui lòng nhập đúng thông tin nhà cung cấp.");
        }
    }

    private void validateDocument(Document8 document) {
        if (document == null || document.getDocumentId() == null
                || !document.getDocumentId().matches("[A-Za-z0-9][A-Za-z0-9_-]{0,29}")
                || !hasLength(document.getTitle(), 1, 255) || !hasLength(document.getCategory(), 1, 80)
                || !hasLength(document.getAuthor(), 1, 150) || !hasLength(document.getPublisher(), 1, 150)
                || document.getPublishYear() == null || document.getPublishYear() < 1000
                || document.getPublishYear() > 9999) {
            throw new IllegalArgumentException("Vui lòng nhập đúng thông tin tài liệu.");
        }
    }

    private boolean hasLength(String value, int min, int max) {
        return value != null && value.trim().length() >= min && value.trim().length() <= max;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String userMessage(Exception exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof IllegalArgumentException || current instanceof IllegalStateException) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return "Không thể xử lý dữ liệu. Vui lòng thử lại.";
    }
}
