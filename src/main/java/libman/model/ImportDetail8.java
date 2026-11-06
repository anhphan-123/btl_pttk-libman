package libman.model;

import java.math.BigDecimal;

public class ImportDetail8 {
    private Long importDetailId;
    private Long importId;
    private String documentId;
    private Integer quantity;
    private BigDecimal price;

    public Long getImportDetailId() { return importDetailId; }
    public void setImportDetailId(Long importDetailId) { this.importDetailId = importDetailId; }
    public Long getImportId() { return importId; }
    public void setImportId(Long importId) { this.importId = importId; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getLineTotal() {
        if (quantity == null || price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
