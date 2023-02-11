package com.thullo.web.controller;

import com.thullo.data.model.Comment;
import com.thullo.service.CommentService;
import com.thullo.web.exception.ResourceNotFoundException;
import com.thullo.web.payload.request.CommentRequest;
import com.thullo.web.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("api/v1/thullo/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse> createComment(@Valid @RequestBody CommentRequest commentRequest) {
        try {
            Comment comment = commentService.createComment(commentRequest);
            ApiResponse response = new ApiResponse(true, "Comment created successfully", comment);
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException ex){
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }

}
