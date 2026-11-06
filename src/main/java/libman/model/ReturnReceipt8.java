package libman.model;

import java.time.LocalDate;

public class ReturnReceipt8 {
    private Long returnId;
    private Long readerId;
    private Long librarianId;
    private LocalDate returnDate;

    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }
    public Long getLibrarianId() { return librarianId; }
    public void setLibrarianId(Long librarianId) { this.librarianId = librarianId; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}
