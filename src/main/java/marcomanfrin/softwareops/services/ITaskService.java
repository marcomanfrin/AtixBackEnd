package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.Task;
import marcomanfrin.softwareops.enums.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface ITaskService {
    Task createTask(UUID workId, String text);
    List<Task> getTasksByWorkId(UUID workId);
    List<Task> getTasksByStatus(UUID workId, TaskStatus status);

    Task getTaskByIdOrThrow(UUID workId, UUID taskId);
    Task updateTaskText(UUID workId, UUID taskId, String text);
    Task updateTaskStatus(UUID workId, UUID taskId, TaskStatus status);

    void deleteTask(UUID workId, UUID taskId);

    List<Task> setAllTasksStatus(UUID workId, TaskStatus status);

    boolean areAllTasksCompleted(UUID workId);
    double getCompletionRate(UUID workId);
}