package customer.autoescalate_ops.service;

import customer.autoescalate_ops.dto.IssueCreateRequest;
import customer.autoescalate_ops.dto.IssueResponse;
import customer.autoescalate_ops.dto.IssueUpdateRequest;
import customer.autoescalate_ops.entity.Issue;
import customer.autoescalate_ops.repository.IssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final SPAClient spaClient;

    public IssueService(IssueRepository issueRepository, SPAClient spaClient) {
        this.issueRepository = issueRepository;
        this.spaClient = spaClient;
    }

    public List<IssueResponse> getAllIssues(String status, String priority) {
        List<Issue> issues;

        if (status != null && priority != null) {
            issues = issueRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            issues = issueRepository.findByStatus(status);
        } else if (priority != null) {
            issues = issueRepository.findByPriority(priority);
        } else {
            issues = issueRepository.findAll();
        }

        return issues.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public IssueResponse getIssueById(UUID id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse createIssue(IssueCreateRequest request) {
        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setType(request.getType());
        issue.setPriority(request.getPriority());
        issue.setSeverity(request.getSeverity());
        issue.setLocation(request.getLocation());
        issue.setStatus("New");
        issue.setReportedAt(LocalDateTime.now());

        Issue savedIssue = issueRepository.save(issue);

        // Trigger SAP Build Process Automation workflow
        spaClient.startWorkflow(
                savedIssue.getId(),
                savedIssue.getTitle(),
                savedIssue.getPriority(),
                savedIssue.getType()
        );

        return toResponse(savedIssue);
    }

    @Transactional
    public IssueResponse updateIssue(UUID id, IssueUpdateRequest request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));

        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());

            // Update timestamps based on status
            if ("InProgress".equalsIgnoreCase(request.getStatus()) && issue.getAcknowledgedAt() == null) {
                issue.setAcknowledgedAt(LocalDateTime.now());
            } else if ("Resolved".equalsIgnoreCase(request.getStatus()) || "Closed".equalsIgnoreCase(request.getStatus())) {
                issue.setResolvedAt(LocalDateTime.now());
            }
        }

        if (request.getResolutionComment() != null) {
            issue.setResolutionComment(request.getResolutionComment());
        }

        Issue updatedIssue = issueRepository.save(issue);
        return toResponse(updatedIssue);
    }

    private IssueResponse toResponse(Issue issue) {
        IssueResponse response = new IssueResponse();
        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setType(issue.getType());
        response.setPriority(issue.getPriority());
        response.setStatus(issue.getStatus());
        response.setSeverity(issue.getSeverity());
        response.setLocation(issue.getLocation());
        response.setReportedAt(issue.getReportedAt());
        response.setAcknowledgedAt(issue.getAcknowledgedAt());
        response.setResolvedAt(issue.getResolvedAt());
        response.setResolutionComment(issue.getResolutionComment());
        return response;
    }
}
