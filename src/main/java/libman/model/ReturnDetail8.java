package libman.model;

public class ReturnDetail8 {
    private Long returnDetailId;
    private Long returnId;
    private Long loanDetailId;
    private String conditionWhenReturned;

    public Long getReturnDetailId() { return returnDetailId; }
    public void setReturnDetailId(Long returnDetailId) { this.returnDetailId = returnDetailId; }
    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
    public Long getLoanDetailId() { return loanDetailId; }
    public void setLoanDetailId(Long loanDetailId) { this.loanDetailId = loanDetailId; }
    public String getConditionWhenReturned() { return conditionWhenReturned; }
    public void setConditionWhenReturned(String conditionWhenReturned) {
        this.conditionWhenReturned = conditionWhenReturned;
    }
}
