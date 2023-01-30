package com.thullo.web.payload.response;

import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

@Data
public class UserProfileResponse {
    private String name;
    private String email;
    private String bio;

    private String phoneNumber;

    @JsonIgnore
    private String password;

    private String imageUrl;
}
