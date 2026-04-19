package ru.urfu.slivky.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.urfu.slivky.service.TaskApplicationService;
import ru.urfu.slivky.service.TaskService;
import ru.urfu.slivky.web.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskApplicationService taskApplicationService;

    @GetMapping
    public List<TaskResponse> list() {
        return taskService.listTasks();
    }

    /**
     * Задачи текущего пользователя (владелец / автор запросов).
     */
    @GetMapping("/mine")
    public List<TaskResponse> myTasks() {
        return taskService.listMyTasks();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(request);
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        return taskService.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/{id}/matches")
    public List<VolunteerMatchResponse> matches(@PathVariable Long id) {
        return taskService.getMatches(id);
    }

    @PostMapping("/{id}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskApplicationResponse apply(
            @PathVariable Long id,
            @Valid @RequestBody TaskApplicationRequest request
    ) {
        return taskApplicationService.apply(id, request);
    }

    @GetMapping("/{id}/applications")
    public List<TaskApplicationResponse> applications(@PathVariable Long id) {
        return taskApplicationService.listForTask(id);
    }
}
