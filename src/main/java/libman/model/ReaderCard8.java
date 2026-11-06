package libman.model;

import java.time.LocalDate;

public class ReaderCard8 {
    private String cardId;
    private Long readerId;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
