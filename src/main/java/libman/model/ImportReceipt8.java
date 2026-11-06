package libman.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ImportReceipt8 {
    public static final String STATUS_UNPAID = "UNPAID";
    public static final String STATUS_PAID = "PAID";

    private Long importId;
    private Long supplierId;
    private Long librarianId;
    private LocalDate importDate;
    private String status;
    private String submissionToken;
    private List<ImportDetail8> details = new ArrayList<>();

    public Long getImportId() { return importId; }
    public void setImportId(Long importId) { this.importId = importId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getLibrarianId() { return librarianId; }
    public void setLibrarianId(Long librarianId) { this.librarianId = librarianId; }
    public LocalDate getImportDate() { return importDate; }
    public void setImportDate(LocalDate importDate) { this.importDate = importDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubmissionToken() { return submissionToken; }
    public void setSubmissionToken(String submissionToken) { this.submissionToken = submissionToken; }
    public List<ImportDetail8> getDetails() { return details; }
    public void setDetails(List<ImportDetail8> details) {
        this.details = details == null ? new ArrayList<>() : new ArrayList<>(details);
    }

    /** Tổng tiền là dữ liệu dẫn xuất, không được lưu trong tblImportReceipt8. */
    public BigDecimal getTotalAmount() {
        return details.stream()
                .map(ImportDetail8::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
