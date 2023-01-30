package com.thullo.service;

import com.thullo.data.model.Role;
import com.thullo.data.model.Token;
import com.thullo.data.model.User;
import com.thullo.data.repository.UserRepository;
import com.thullo.web.payload.request.UserProfileRequest;
import com.thullo.web.payload.response.UserProfileResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockedUser;

    private UserProfileResponse mockedUserResponse;
    private UserProfileRequest mockedUserRequest;

    private Role role;
    private Token token;

    @BeforeEach
    void setUp() {
        mockedUser = new User();
        mockedUser.setId(1L);
        mockedUser.setName("Abdullah Ismail");
        mockedUser.setEmail("ismail@gmail.com");
        mockedUser.setPassword("pass1234");
        role = new Role("ROLE_USER");
        mockedUser.getRoles().add(role);

        token = new Token();
        token.setUser(mockedUser);
        token.setToken(UUID.randomUUID().toString());

        mockedUserResponse = new UserProfileResponse();
        mockedUserResponse.setName(mockedUser.getName());
        mockedUserResponse.setEmail(mockedUser.getEmail());

        mockedUserRequest = new UserProfileRequest();
        mockedUserRequest.setName(mockedUser.getName());
        mockedUserRequest.setEmail(mockedUser.getEmail());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canRetrieveLoggedInUserDetails() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(mockedUser));

        when(mapper.map(mockedUser, UserProfileResponse.class))
                .thenReturn(mockedUserResponse);

        UserProfileResponse userDetails = userService.getUserDetails(mockedUser.getEmail());

        verify(userRepository).findByEmail(mockedUser.getEmail());
        assertAll(() -> {
            assertEquals(mockedUser.getEmail(), userDetails.getEmail());
            assertEquals(mockedUser.getName(), userDetails.getName());
        });
    }


    @Test
    void canUpdateLoggedInUserDetails() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(mockedUser));

        doAnswer((invocation) -> {
            UserProfileRequest request = invocation.getArgument(0);
            User user = invocation.getArgument(1);
            // mock the mapping logic here
            return null;
        }).when(mapper).map(mockedUserRequest, mockedUser);

        doNothing().when(userRepository).save(any(User.class));

        userService.updateUserDetails(mockedUserRequest, mockedUser.getEmail());

        verify(userRepository).findByEmail(mockedUser.getEmail());
        verify(userRepository).save(any());
    }

}