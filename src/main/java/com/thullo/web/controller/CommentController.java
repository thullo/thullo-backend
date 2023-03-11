package com.thullo.web.controller;

import com.thullo.annotation.CurrentUser;
import com.thullo.security.UserPrincipal;
import com.thullo.service.CommentService;
import com.thullo.web.exception.ResourceNotFoundException;
import com.thullo.web.payload.request.CommentRequest;
import com.thullo.web.payload.response.ApiResponse;
import com.thullo.web.payload.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/thullo/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("{boardTag}/{boardRef}")
    @PreAuthorize("@boardServiceImpl.hasBoardRole(authentication.principal.email, #boardTag) or hasRole('BOARD_' + #boardTag) or hasRole('TASK_' + #boardRef)")
    public ResponseEntity<ApiResponse> createComment(@PathVariable String boardTag, @PathVariable String boardRef, @Valid @RequestBody CommentRequest commentRequest, @CurrentUser UserPrincipal principal) {
        try {
            CommentResponse comment = commentService.createComment(boardRef, principal.getEmail(), commentRequest);
            ApiResponse response = new ApiResponse(true, "Comment created successfully", comment);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PutMapping
    @PreAuthorize("@commentServiceImpl.isCommentOwner(#commentId, authentication.principal.email)")
    public ResponseEntity<ApiResponse> editComment(@RequestParam("boardRef") String boardRef, @RequestParam("commentId") Long commentId,  @Valid @RequestBody CommentRequest commentRequest) {
        try {
            CommentResponse comment = commentService.editComment(boardRef, commentId, commentRequest);
            ApiResponse response = new ApiResponse(true, "Comment successfully updated", comment);
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException ex){
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @DeleteMapping
    @PreAuthorize("@commentServiceImpl.isCommentOwner(#commentId, authentication.principal.email)")
    public ResponseEntity<ApiResponse> deleteComment(@RequestParam("boardRef") String boardRef, @RequestParam("commentId") Long commentId) {
        try {
            commentService.deleteComment(boardRef, commentId);
            ApiResponse response = new ApiResponse(true, "Comment successfully deleted");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @GetMapping("{boardTag}/{boardRef}")
    @PreAuthorize("@boardServiceImpl.hasBoardRole(authentication.principal.email, #boardTag) or hasRole('BOARD_' + #boardTag) or hasRole('TASK_' + #boardRef)")
    public ResponseEntity<ApiResponse> getTaskComments(@PathVariable String boardTag, @PathVariable String boardRef) {
        try {
            List<CommentResponse> taskComments = commentService.getTaskComment(boardRef);
            ApiResponse response = new ApiResponse(true, "Comments successfully fetched", taskComments);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }
}
