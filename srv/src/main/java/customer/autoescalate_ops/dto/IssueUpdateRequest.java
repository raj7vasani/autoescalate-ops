package customer.autoescalate_ops.dto;

public class IssueUpdateRequest {

    private String status;
    private String resolutionComment;

    // Constructors
    public IssueUpdateRequest() {
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }
}
