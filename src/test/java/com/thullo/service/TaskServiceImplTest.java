//package com.thullo.service;
//
//import com.thullo.data.model.Task;
//import com.thullo.data.repository.TaskRepository;
//import com.thullo.web.exception.BadRequestException;
//import com.thullo.web.exception.ResourceNotFoundException;
//import com.thullo.web.payload.request.TaskRequest;
//import org.apache.commons.io.IOUtils;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class TaskServiceImplTest {
//
//    @Mock
//    private TaskRepository taskRepository;
//
//    @Mock
//    private ModelMapper mapper;
//
//    @Mock
//    private FileService fileService;
//
//    @InjectMocks
//    private TaskServiceImpl taskService;
//
//
//    private TaskRequest taskRequest;
//    private Task task;
//    String taskName = "First task";
//    String imageUrl = "http://localhost:8080/api/v1/thullo/files/123e4567-e89b-12d3-a456-426655440000";
//
//    @BeforeEach
//    void setUp() {
//        task = new com.thullo.data.model.Task();
//        task.setName(taskName);
//
//
//        taskRequest = new TaskRequest();
//        taskRequest.setName(taskName);
////        taskRequest.setTask(task);
//
//
//        task = new Task();
//        task.setName(taskName);
//        task.setImageUrl(imageUrl);
//
//    }
//
//    @Test
//    void testCreateTask_withValidName_thenTaskIsCreated() throws BadRequestException, IOException, ResourceNotFoundException {
//
//        when(taskRepository.save(any())).thenReturn(task);
//
//
//        Task actualResponse = taskService.createTask("", taskRequest);
//
//        verify(mapper).map(task, Task.class);
//        verify(taskRepository).save(task);
//        Assertions.assertEquals(taskName, actualResponse.getName());
//    }
//
//    @Test
//    void testCreateTask_withCoverImage_thenTaskIsCreated() throws IOException, BadRequestException, ResourceNotFoundException {
//        MultipartFile multipartFile = getMultipartFile("src/main/resources/static/code.png");
//        String url = "http://localhost:8080/api/v1/thullo";
//        taskRequest.setFile(multipartFile);
//        taskRequest.setRequestUrl(url);
//
//
//        when(taskRepository.save(any())).thenReturn(task);
//
//        when(mapper.map(task, Task.class))
//                .thenReturn(task);
//
//
//        when(fileService.uploadFile(taskRequest.getFile(), taskRequest.getRequestUrl()))
//                .thenReturn(imageUrl);
//
//        Task actualResponse = taskService.createTask("", taskRequest);
//        verify(fileService).uploadFile(multipartFile, url);
//        assertEquals(taskName, actualResponse.getName());
//        assertEquals(imageUrl, actualResponse.getImageUrl());
//    }
//
//    @Test
//    void testMoveTask_validTaskColumnIdAndIndex_thenTaskIsMovedToTheNewColumn() throws ResourceNotFoundException {
//
//
//        task.setId(1L);
//        task.setPosition(1L);
//        task.setName("Move me");
////        task.setTaskColumn(todo);
//
//        TaskColumn backlog = new TaskColumn();
//        backlog.setId(1L);
//        backlog.setName("Backlog");
//
//        com.thullo.data.model.Task task1 = new com.thullo.data.model.Task();
//        task1.setName("First task");
//        task1.setId(2L);
//        task1.setPosition(1L);
////        task1.setTaskColumn(backlog);
//
//        com.thullo.data.model.Task task2 = new com.thullo.data.model.Task();
//        task2.setName("Second task");
//        task2.setId(3L);
//        task2.setPosition(2L);
////        task2.setTaskColumn(backlog);
//
//        com.thullo.data.model.Task updatedTask = new com.thullo.data.model.Task();
//        updatedTask.setName("Move me");
//        updatedTask.setId(1L);
//        updatedTask.setPosition(2L);
////        updatedTask.setTaskColumn(backlog);
//
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.ofNullable(task));
//
//        when(taskRepository.findByTaskColumnOrderByPositionAsc(anyLong()))
//                .thenReturn(Optional.of(List.of(task1, task2)));
//
//        when(taskColumnRepository.findById(anyLong()))
//                .thenReturn(Optional.of(backlog));
//
//        when(taskRepository.save(any(com.thullo.data.model.Task.class)))
//                .thenReturn(updatedTask);
//
//        com.thullo.data.model.Task response = taskService.moveTask(1L, 1L, 2L);
//
//        verify(taskRepository).findById(1L);
//        verify(taskColumnRepository).findById(1L);
//        verify(taskRepository).findByTaskColumnOrderByPositionAsc(1L);
//        verify(taskRepository).save(task);
//
//        assertNotNull(response);
////        assertEquals(1L, response.getTaskColumn().getId());
//        assertEquals(2, response.getPosition());
//    }
//
//
//    @Test
//    void testMoveTask_InvalidColumnId_throwRecordNotFoundException(){
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.ofNullable(task));
//
//        when(taskRepository.findByTaskColumnOrderByPositionAsc(anyLong()))
//                .thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () ->
//                taskService.moveTask(1L, 5L, 2L));
//    }
//
//    @Test
//    void testMoveTask_InvalidTaskId_throwRecordNotFoundException() {
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () ->
//                taskService.moveTask(1L, 5L, 2L));
//    }
//
//    @Test
//    void testMoveTask_indexGreaterThanTasksInTheNewColumn_moveToNewColumnEqualToTheNumberOfTaskInTheColumn() throws ResourceNotFoundException {
//
//        TaskColumn backlog = new TaskColumn();
//        backlog.setId(1L);
//        backlog.setName("Backlog");
//
//        TaskColumn todo = new TaskColumn();
//        todo.setId(2L);
//        todo.setName("Todo");
//
//
//        task.setId(1L);
//        task.setPosition(1L);
//        task.setName("Move me");
////        task.setTaskColumn(todo);
//
//        com.thullo.data.model.Task task1 = new com.thullo.data.model.Task();
//        task1.setName("First task");
//        task1.setId(2L);
//        task1.setPosition(0L);
////        task1.setTaskColumn(backlog);
//
//        com.thullo.data.model.Task task2 = new com.thullo.data.model.Task();
//        task2.setName("Second task");
//        task2.setId(3L);
//        task2.setPosition(1L);
////        task2.setTaskColumn(backlog);
//
//        com.thullo.data.model.Task updatedTask = new com.thullo.data.model.Task();
//        updatedTask.setName("Move me");
//        updatedTask.setId(1L);
//        updatedTask.setPosition(2L);
////        updatedTask.setTaskColumn(backlog);
//
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.ofNullable(task));
//
//        when(taskRepository.findByTaskColumnOrderByPositionAsc(anyLong()))
//                .thenReturn(Optional.of(List.of(task1, task2)));
//
//        when(taskColumnRepository.findById(anyLong()))
//                .thenReturn(Optional.of(backlog));
//
//        when(taskRepository.save(any(com.thullo.data.model.Task.class)))
//                .thenReturn(updatedTask);
//
//        com.thullo.data.model.Task response = taskService.moveTask(1L, 1L, 10L);
//
//        assertNotNull(response);
////        assertEquals(1L, response.getTaskColumn().getId());
//        assertEquals(2, response.getPosition());
//    }
//
//
//    @Test
//    void testMoveTask_alreadyInTheNewColumn_taskRemainInTheSameColumnAndIndex() throws ResourceNotFoundException {
//
//        TaskColumn todo = new TaskColumn();
//        todo.setId(2L);
//        todo.setName("Todo");
//
//
//        task.setId(1L);
//        task.setPosition(1L);
//        task.setName("Move me");
////        task.setTaskColumn(todo);
//
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.ofNullable(task));
//
//        when(taskRepository.findByTaskColumnOrderByPositionAsc(anyLong()))
//                .thenReturn(Optional.of(List.of(task)));
//
//        when(taskColumnRepository.findById(anyLong()))
//                .thenReturn(Optional.of(todo));
//
//        when(taskRepository.save(any(com.thullo.data.model.Task.class)))
//                .thenReturn(task);
//
//        com.thullo.data.model.Task response = taskService.moveTask(1L, 2L, 1L);
//
//        verify(taskRepository).findById(1L);
//        verify(taskColumnRepository).findById(2L);
//        verify(taskRepository).findByTaskColumnOrderByPositionAsc(2L);
//        verify(taskRepository).save(task);
//
//        assertNotNull(response);
////        assertEquals(2L, response.getTaskColumn().getId());
//        assertEquals(1, response.getPosition());
//    }
//
//
//    @Test
//    void testMoveTask_sameColumnIdAndValidIndex_moveTaskToNewIndexOnTheColumn() throws ResourceNotFoundException {
//        TaskColumn todo = new TaskColumn();
//        todo.setId(2L);
//        todo.setName("Todo");
//
//        task.setId(1L);
//        task.setPosition(1L);
//        task.setName("Move me");
////        task.setTaskColumn(todo);
//
//        com.thullo.data.model.Task task1 = new com.thullo.data.model.Task();
//        task1.setName("First task");
//        task1.setId(2L);
//        task1.setPosition(0L);
////        task1.setTaskColumn(todo);
//
//        com.thullo.data.model.Task updatedTask = new com.thullo.data.model.Task();
//        updatedTask.setName("Move me");
//        updatedTask.setId(1L);
//        updatedTask.setPosition(0L);
////        updatedTask.setTaskColumn(todo);
//
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.ofNullable(task));
//
//        when(taskRepository.findByTaskColumnOrderByPositionAsc(anyLong()))
//                .thenReturn(Optional.of(List.of(task1, task)));
//
//        when(taskColumnRepository.findById(anyLong()))
//                .thenReturn(Optional.of(todo));
//
//        when(taskRepository.save(any(com.thullo.data.model.Task.class)))
//                .thenReturn(task);
//
//        com.thullo.data.model.Task response = taskService.moveTask(1L, 2L, 0L);
//
//        verify(taskRepository).findById(1L);
//        verify(taskColumnRepository).findById(2L);
//        verify(taskRepository).findByTaskColumnOrderByPositionAsc(2L);
//        verify(taskRepository).save(task);
//
//        assertNotNull(response);
////        assertEquals(2L, response.getTaskColumn().getId());
//        assertEquals(0, response.getPosition());
//    }
//
//    @Test
//    void getTask_withValidId_getTask() throws ResourceNotFoundException {
//        task.setId(1L);
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.ofNullable(task));
//
//        com.thullo.data.model.Task response = taskService.getTask(1L);
//
//        verify(taskRepository).findById(1L);
//        assertNotNull(response);
//        assertEquals(1L, response.getId());
//    }
//
//    @Test
//    void getTask_withInvalidId_throwRequestNotFoundException(){
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class,
//                () -> taskService.getTask(1L));
//    }
//    @Test
//    void testEditTask_withValidDescription_taskIsUpdated(){
//        TaskRequest request = new TaskRequest();
//
//        when(taskRepository.findById(anyLong()))
//                .thenReturn(Optional.of(task));
//
//    }
//
//    public MultipartFile getMultipartFile(String filePath) throws IOException {
//        File file = new File(filePath);
//        InputStream input = new FileInputStream(file);
//        return new MockMultipartFile("file", file.getName(), "image/jpeg", IOUtils.toByteArray(input));
//    }
//}