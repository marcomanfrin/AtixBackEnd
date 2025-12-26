package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.Task;
import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.enums.TaskStatus;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.TaskRepository;
import marcomanfrin.softwareops.repositories.WorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService implements ITaskService {

    private final TaskRepository taskRepository;
    private final WorkRepository workRepository;

    public TaskService(TaskRepository taskRepository, WorkRepository workRepository) {
        this.taskRepository = taskRepository;
        this.workRepository = workRepository;
    }

    @Override
    public Task createTask(UUID workId, String text) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found: " + workId));

        Task task = new Task();
        task.setWork(work);
        task.setText(validateText(text));
        task.setStatus(TaskStatus.TODO);

        Task saved = taskRepository.save(task);
        onTasksChanged(workId);
        return saved;
    }

    @Override
    public List<Task> getTasksByWorkId(UUID workId) {
        ensureWorkExists(workId);
        return taskRepository.findByWorkId(workId);
    }

    @Override
    public List<Task> getTasksByStatus(UUID workId, TaskStatus status) {
        if (status == null) throw new IllegalArgumentException("Task status cannot be null");
        ensureWorkExists(workId);
        return taskRepository.findByWorkIdAndStatus(workId, status);
    }

    @Override
    public Task getTaskByIdOrThrow(UUID workId, UUID taskId) {
        ensureWorkExists(workId);
        return taskRepository.findByIdAndWork_Id(taskId, workId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId + " for work: " + workId));
    }

    @Override
    public Task updateTaskText(UUID workId, UUID taskId, String text) {
        Task task = getTaskByIdOrThrow(workId, taskId);
        task.setText(validateText(text));
        Task saved = taskRepository.save(task);
        onTasksChanged(workId);
        return saved;
    }

    @Override
    public Task updateTaskStatus(UUID workId, UUID taskId, TaskStatus status) {
        if (status == null) throw new IllegalArgumentException("Task status cannot be null");

        Task task = getTaskByIdOrThrow(workId, taskId);
        if (task.getStatus() == status) return task;

        task.setStatus(status);
        Task saved = taskRepository.save(task);
        onTasksChanged(workId);
        return saved;
    }

    @Override
    @Transactional
    public void deleteTask(UUID workId, UUID taskId) {
        getTaskByIdOrThrow(workId, taskId); // valida ownership
        taskRepository.deleteByIdAndWork_Id(taskId, workId);
        onTasksChanged(workId);
    }

    @Override
    public List<Task> setAllTasksStatus(UUID workId, TaskStatus status) {
        if (status == null) throw new IllegalArgumentException("Task status cannot be null");

        ensureWorkExists(workId);
        List<Task> tasks = taskRepository.findByWorkId(workId);
        for (Task t : tasks) t.setStatus(status);

        List<Task> saved = taskRepository.saveAll(tasks);
        onTasksChanged(workId);
        return saved;
    }

    @Override
    public boolean areAllTasksCompleted(UUID workId) {
        ensureWorkExists(workId);
        long total = taskRepository.countByWorkId(workId);
        if (total == 0) return false;
        long done = taskRepository.countByWorkIdAndStatus(workId, TaskStatus.DONE);
        return done == total;
    }

    @Override
    public double getCompletionRate(UUID workId) {
        ensureWorkExists(workId);
        long total = taskRepository.countByWorkId(workId);
        if (total == 0) return 0.0;
        long done = taskRepository.countByWorkIdAndStatus(workId, TaskStatus.DONE);
        return (done * 100.0) / total;
    }

    private void ensureWorkExists(UUID workId) {
        if (!workRepository.existsById(workId)) {
            throw new NotFoundException("Work not found: " + workId);
        }
    }

    private String validateText(String text) {
        if (text == null || text.trim().isBlank()) {
            throw new IllegalArgumentException("Task text cannot be blank");
        }
        return text.trim();
    }

    private void onTasksChanged(UUID workId) {
        // qui puoi chiamare WorkService.refreshProgress(workId) se vuoi.
    }
}