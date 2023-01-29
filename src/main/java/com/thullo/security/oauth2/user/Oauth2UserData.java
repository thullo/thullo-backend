package com.thullo.security.oauth2.user;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public abstract class Oauth2UserData {
    protected final Map<String, Object> attributes;

    public abstract String getUserId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();

}
