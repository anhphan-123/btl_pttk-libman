package libman.model;

import java.time.LocalDate;

public class LoanReceipt8 {
    private Long loanId;
    private Long readerId;
    private Long librarianId;
    private LocalDate loanDate;
    private LocalDate dueDate;

    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }
    public Long getLibrarianId() { return librarianId; }
    public void setLibrarianId(Long librarianId) { this.librarianId = librarianId; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
