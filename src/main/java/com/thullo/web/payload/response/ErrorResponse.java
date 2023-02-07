package com.thullo.web.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        timestamp = LocalDateTime.now();
    }
}
