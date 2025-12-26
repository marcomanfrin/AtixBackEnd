package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.Task;
import marcomanfrin.softwareops.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByWorkId(UUID workId);
    List<Task> findByWorkIdAndStatus(UUID workId, TaskStatus status);
    long countByWorkId(UUID workId);
    long countByWorkIdAndStatus(UUID workId, TaskStatus taskStatus);

    Optional<Task> findByIdAndWork_Id(UUID taskId, UUID workId);
    void deleteByIdAndWork_Id(UUID taskId, UUID workId);
}
