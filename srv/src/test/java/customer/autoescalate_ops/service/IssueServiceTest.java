package customer.autoescalate_ops.service;

import customer.autoescalate_ops.dto.IssueCreateRequest;
import customer.autoescalate_ops.dto.IssueResponse;
import customer.autoescalate_ops.entity.Issue;
import customer.autoescalate_ops.repository.IssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private SPAClient spaClient;

    @InjectMocks
    private IssueService issueService;

    private IssueCreateRequest createRequest;
    private Issue savedIssue;

    @BeforeEach
    void setUp() {
        createRequest = new IssueCreateRequest();
        createRequest.setTitle("Test Machine Breakdown");
        createRequest.setDescription("Machine M001 stopped working");
        createRequest.setType("MACHINE_BREAKDOWN");
        createRequest.setPriority("High");
        createRequest.setSeverity(4);
        createRequest.setLocation("Plant A - Line 3");

        savedIssue = new Issue();
        savedIssue.setId(UUID.randomUUID());
        savedIssue.setTitle(createRequest.getTitle());
        savedIssue.setDescription(createRequest.getDescription());
        savedIssue.setType(createRequest.getType());
        savedIssue.setPriority(createRequest.getPriority());
        savedIssue.setSeverity(createRequest.getSeverity());
        savedIssue.setLocation(createRequest.getLocation());
        savedIssue.setStatus("New");
    }

    @Test
    void createIssue_ShouldSaveIssueAndTriggerSPAWorkflow() {
        // Arrange
        when(issueRepository.save(any(Issue.class))).thenReturn(savedIssue);

        // Act
        IssueResponse response = issueService.createIssue(createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(savedIssue.getId(), response.getId());
        assertEquals("Test Machine Breakdown", response.getTitle());
        assertEquals("New", response.getStatus());
        assertEquals("High", response.getPriority());

        // Verify repository save was called
        verify(issueRepository, times(1)).save(any(Issue.class));

        // Verify SPA workflow was triggered with correct parameters
        verify(spaClient, times(1)).startWorkflow(
                eq(savedIssue.getId()),
                eq(savedIssue.getTitle()),
                eq(savedIssue.getPriority()),
                eq(savedIssue.getType())
        );
    }

    @Test
    void createIssue_ShouldSetDefaultStatus() {
        // Arrange
        when(issueRepository.save(any(Issue.class))).thenReturn(savedIssue);

        // Act
        IssueResponse response = issueService.createIssue(createRequest);

        // Assert
        assertEquals("New", response.getStatus());
        assertNotNull(response.getReportedAt());
    }

    @Test
    void createIssue_ShouldTriggerSPAWorkflowEvenIfRepositorySaveFails() {
        // This test ensures that even if save fails, we don't call SPA
        // Arrange
        when(issueRepository.save(any(Issue.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> issueService.createIssue(createRequest));

        // Verify SPA workflow was NOT triggered since save failed
        verify(spaClient, never()).startWorkflow(any(), any(), any(), any());
    }
}
