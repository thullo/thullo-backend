package com.thullo.service;

import com.thullo.data.model.Task;
import com.thullo.data.model.TaskColumn;
import com.thullo.data.model.User;
import com.thullo.data.repository.TaskColumnRepository;
import com.thullo.data.repository.TaskRepository;
import com.thullo.data.repository.UserRepository;
import com.thullo.web.exception.BadRequestException;
import com.thullo.web.exception.RecordNotFoundException;
import com.thullo.web.payload.request.TaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ModelMapper mapper;
    private final FileService fileService;
    private final TaskColumnRepository taskColumnRepository;

    private final UserRepository userRepository;


    @Override
    public Task createTask(TaskRequest taskRequest) throws BadRequestException, IOException {
        com.thullo.data.model.Task task = taskRequest.getTask();
        String imageUrl = uploadTaskFile(taskRequest);
        task.setImageUrl(imageUrl);
        com.thullo.data.model.Task saveTask = taskRepository.save(task);
        return mapper.map(saveTask, Task.class);
    }

    private String uploadTaskFile(TaskRequest taskRequest) throws BadRequestException, IOException {
        String imageUrl = null;
        if (taskRequest.getFile() != null) {
            imageUrl = fileService.uploadFile(taskRequest.getFile(), taskRequest.getRequestUrl());
        }
        return imageUrl;
    }

    @Override
    public com.thullo.data.model.Task moveTask(Long taskId, Long newColumnId, Long index) throws RecordNotFoundException {
        Long absoluteIndex = Math.abs(index);
        com.thullo.data.model.Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RecordNotFoundException("Task not found !"));

        List<com.thullo.data.model.Task> tasksInColumn = taskRepository.findByTaskColumnOrderByPositionAsc(newColumnId).orElseThrow(
                () -> new RecordNotFoundException("Task column not found !"));

        if (absoluteIndex > tasksInColumn.size())
            absoluteIndex = (long) tasksInColumn.size();

        if (task.getTaskColumn().getId().equals(newColumnId) && task.getPosition() < absoluteIndex) {
            absoluteIndex--;
        }


        Long finalIndex = absoluteIndex;
        tasksInColumn.stream()
                .filter(t -> t.getPosition() >= finalIndex)
                .forEach(t -> t.setPosition(t.getPosition() + 1));

        task.setPosition(absoluteIndex);
        task.setTaskColumn(getTaskColumn(newColumnId));
        return taskRepository.save(task);
    }

    @Override
    public com.thullo.data.model.Task editTask(Long taskId, TaskRequest taskRequest) throws BadRequestException, IOException, RecordNotFoundException {
        com.thullo.data.model.Task task = getTask(taskId);
        mapper.map(taskRequest, task);
        String imageUrl = uploadTaskFile(taskRequest);
        if (imageUrl != null)
            task.setImageUrl(imageUrl);
        return task;
    }


    public boolean isTaskOwner(Long taskColumnId, String email) {
        TaskColumn taskColumn = getTaskColumn(taskColumnId);
        if (taskColumn == null) return false;
        return taskColumn.getBoard().getUser().getEmail().equals(email);
    }

    public boolean isTaskOwnedByUser(Long taskId, Long newColumnId, String email) {
        com.thullo.data.model.Task task = getTaskInternal(taskId);
        if (task == null) {
            return false;
        }
        TaskColumn column = taskColumnRepository.findById(newColumnId).orElse(null);
        if (column == null) {
            return false;
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }
        if (!task.getTaskColumn().getBoard().getUser().getId().equals(user.getId())) {
            return false;
        }
        return column.getBoard().getUser().getId().equals(user.getId());
    }

    private com.thullo.data.model.Task getTaskInternal(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }


    private TaskColumn getTaskColumn(Long taskColumnId) {
        return taskColumnRepository.findById(taskColumnId).orElse(null);
    }

    public Task getTask(Long taskId) throws RecordNotFoundException {
        com.thullo.data.model.Task task = getTaskInternal(taskId);
        if (task == null) throw new RecordNotFoundException("Task not found !");
        return task;
    }

    public boolean isTaskCreator(Long taskId, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        com.thullo.data.model.Task task = getTaskInternal(taskId);
        if (task == null) return false;

        return task.getTaskColumn().getBoard().getUser().getId().equals(user.getId());
    }
}
