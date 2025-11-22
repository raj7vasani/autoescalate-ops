package customer.autoescalate_ops.repository;

import customer.autoescalate_ops.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {

    List<Issue> findByStatus(String status);

    List<Issue> findByPriority(String priority);

    List<Issue> findByStatusAndPriority(String status, String priority);
}
