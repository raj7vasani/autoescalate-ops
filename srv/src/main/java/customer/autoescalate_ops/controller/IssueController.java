package customer.autoescalate_ops.controller;

import customer.autoescalate_ops.dto.IssueCreateRequest;
import customer.autoescalate_ops.dto.IssueResponse;
import customer.autoescalate_ops.dto.IssueUpdateRequest;
import customer.autoescalate_ops.service.IssueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<List<IssueResponse>> getAllIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        List<IssueResponse> issues = issueService.getAllIssues(status, priority);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable UUID id) {
        IssueResponse issue = issueService.getIssueById(id);
        return ResponseEntity.ok(issue);
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@RequestBody IssueCreateRequest request) {
        IssueResponse createdIssue = issueService.createIssue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIssue);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable UUID id,
            @RequestBody IssueUpdateRequest request) {
        IssueResponse updatedIssue = issueService.updateIssue(id, request);
        return ResponseEntity.ok(updatedIssue);
    }
}
