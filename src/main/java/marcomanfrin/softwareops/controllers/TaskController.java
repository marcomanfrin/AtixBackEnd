package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.tasks.CreateTaskRequest;
import marcomanfrin.softwareops.DTO.tasks.UpdateTaskRequest;
import marcomanfrin.softwareops.DTO.tasks.UpdateTaskStatusRequest;
import marcomanfrin.softwareops.entities.Task;
import marcomanfrin.softwareops.enums.TaskStatus;
import marcomanfrin.softwareops.services.ITaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/works/{workId}/tasks")
public class TaskController {

    private final ITaskService taskService;

    public TaskController(ITaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Task createTask(@PathVariable UUID workId, @RequestBody @Valid CreateTaskRequest request) {
        return taskService.createTask(workId, request.text());
    }

    @GetMapping
    public List<Task> getTasks(@PathVariable UUID workId,
                               @RequestParam(required = false) TaskStatus status) {
        return (status == null)
                ? taskService.getTasksByWorkId(workId)
                : taskService.getTasksByStatus(workId, status);
    }

    @PatchMapping("/{taskId}")
    public Task updateTask(@PathVariable UUID workId,
                           @PathVariable UUID taskId,
                           @RequestBody @Valid UpdateTaskRequest request) {
        return taskService.updateTaskText(workId, taskId, request.text());
    }

    @PatchMapping("/{taskId}/status")
    public Task updateTaskStatus(@PathVariable UUID workId,
                                 @PathVariable UUID taskId,
                                 @RequestBody @Valid UpdateTaskStatusRequest request) {
        return taskService.updateTaskStatus(workId, taskId, request.status());
    }

    @GetMapping("/completion-rate")
    public double getCompletionRate(@PathVariable UUID workId) {
        return taskService.getCompletionRate(workId);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID workId, @PathVariable UUID taskId) {
        taskService.deleteTask(workId, taskId);
        return ResponseEntity.noContent().build();
    }
}