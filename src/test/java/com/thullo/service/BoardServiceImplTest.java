package com.thullo.service;

import com.thullo.data.model.Board;
import com.thullo.data.model.User;
import com.thullo.data.repository.BoardRepository;
import com.thullo.data.repository.UserRepository;
import com.thullo.security.UserPrincipal;
import com.thullo.web.exception.UserException;
import com.thullo.web.payload.request.BoardRequest;
import com.thullo.web.payload.response.BoardResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private ModelMapper mapper;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private BoardServiceImpl boardService;

    private Board board;
    private BoardRequest boardRequest;

    private BoardResponse boardResponse;

    private UserPrincipal userPrincipal;
    String boardName = "DevDegree challenge";
    String imageUrl = "http://localhost:8080/api/v1/thullo/files/123e4567-e89b-12d3-a456-426655440000";

    @BeforeEach
    void setUp() {
        board = new Board();
        board.setName(boardName);

        boardRequest = new BoardRequest();
        boardRequest.setName(boardName);

        boardResponse = new BoardResponse();
        boardResponse.setName(boardName);

        userPrincipal = new UserPrincipal(
                1L,
                "Ismail Abdullah"
                , "admin@gmail.com",
                "password"
                ,true
                , List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void testCreateBoard_withBoardName_createANewBoard() throws UserException {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
            when(mapper.map(boardRequest, Board.class))
                    .thenReturn(board);
        when(mapper.map(board, BoardResponse.class))
                .thenReturn(boardResponse);
            when(boardRepository.save(board)).thenReturn(board);

        BoardResponse actualResponse = boardService.createBoard(boardRequest, userPrincipal);

        verify(mapper).map(boardRequest, Board.class);
        verify(boardRepository).save(board);
        verify(userRepository).findByEmail(userPrincipal.getEmail());
        assertEquals(boardName, actualResponse.getName());
    }

    @Test
    void testCreateBoard_WithBoardNameAndCoverImage_createANewBoard() throws IOException, UserException {
        MultipartFile multipartFile = getMultipartFile("src/main/resources/static/code.png");
        boardRequest.setFile(multipartFile);
        boardRequest.setRequestUrl("http://localhost:8080/api/v1/thullo");

        boardResponse.setImageUrl(imageUrl);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        when(mapper.map(boardRequest, Board.class))
                .thenReturn(board);

        when(fileService.uploadFile(boardRequest.getFile(), boardRequest.getRequestUrl()))
                .thenReturn(imageUrl);

        when(boardRepository.save(board)).thenReturn(board);

        when(mapper.map(board, BoardResponse.class))
                .thenReturn(boardResponse);



        BoardResponse actualResponse = boardService.createBoard(boardRequest, userPrincipal);

        verify(mapper).map(boardRequest, Board.class);
        verify(boardRepository).save(board);
        verify(userRepository).findByEmail(userPrincipal.getEmail());
        verify(fileService).uploadFile(multipartFile, boardRequest.getRequestUrl());
        assertEquals(boardName, actualResponse.getName());
        assertEquals(imageUrl, actualResponse.getImageUrl());
    }


    public MultipartFile getMultipartFile(String filePath) throws IOException {
        File file = new File(filePath);
        InputStream input = new FileInputStream(file);
        return new MockMultipartFile("file", file.getName(), "image/jpeg", IOUtils.toByteArray(input));
    }
}