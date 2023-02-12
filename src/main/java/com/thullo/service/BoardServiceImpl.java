package com.thullo.service;


import com.thullo.data.model.Board;
import com.thullo.data.model.TaskColumn;
import com.thullo.data.model.User;
import com.thullo.data.repository.BoardRepository;
import com.thullo.data.repository.TaskColumnRepository;
import com.thullo.data.repository.UserRepository;
import com.thullo.security.UserPrincipal;
import com.thullo.util.Helper;
import com.thullo.web.exception.BadRequestException;
import com.thullo.web.exception.UserException;
import com.thullo.web.payload.request.BoardRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final ModelMapper mapper;

    private final FileService fileService;

    private final UserRepository userRepository;
    private final TaskColumnRepository taskColumnRepository;

    /**
     * Creates a new board based on the provided board request.
     *
     * @param boardRequest The request containing the information for the new board to be created.
     * @return A response object containing the result of the board creation process.
     */

    public Board createBoard(BoardRequest boardRequest, UserPrincipal userPrincipal) throws UserException, BadRequestException, IOException {
        if(Helper.isNullOrEmpty(boardRequest.getName())) throw new BadRequestException("Board name cannot be empty");
        User user = internalFindUserByEmail(userPrincipal.getEmail());
        Board board = mapper.map(boardRequest, Board.class);
        board.setUser(user);
        String imageUrl = null;
        if (boardRequest.getFile() != null){
            imageUrl = fileService.uploadFile(boardRequest.getFile(), boardRequest.getRequestUrl());
        }
        board.setImageUrl(imageUrl);
        createDefaultTaskColumn(board);
        board.setBoardTag(generateThreeLetterWord(boardRequest.getName().toUpperCase()));
        Board savedBoard = boardRepository.save(board);
        savedBoard.getTaskColumns().forEach(this::updateTaskColumnCache);
        return savedBoard;
    }

    @Override
    public Board getBoard(Long id) throws BadRequestException {
        return boardRepository.findById(id).orElseThrow(()-> new BadRequestException ("Board not found!"));
    }


    private Board getBoardInternal(Long id){
        return boardRepository.findById(id).orElse(null);
    }

    @Override
    public List<Board> getBoards(UserPrincipal userPrincipal) throws UserException {
        User user = internalFindUserByEmail(userPrincipal.getEmail());
        return boardRepository.getAllByUserOrderByCreatedAtAsc(user);
    }

    public boolean isBoardOwner(Long boardId, String email) {
        Board board = getBoardInternal(boardId);
        if (board == null) return false;
        return board.getUser().getEmail().equals(email);
    }

    private void createDefaultTaskColumn(Board board) {
        board.setTaskColumns(List.of(
                new TaskColumn("Backlog \uD83E\uDD14", board),
                new TaskColumn("In Progress \uD83D\uDCDA", board),
                new TaskColumn("In Review ⚙️", board),
                new TaskColumn("Completed \uD83D\uDE4C\uD83C\uDFFD", board))
        );
    }

    private User internalFindUserByEmail(String email) throws UserException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserException(format("user not found with email %s", email)));
    }

    @CachePut(value = "taskColumns", key = "#taskColumn.id")
    public void updateTaskColumnCache(TaskColumn taskColumn) {
        taskColumnRepository.save(taskColumn);
    }

    private String generateThreeLetterWord(String boardName) {
        Set<String> usedThreeLetterWords = boardRepository.findAll().stream()
                .map(Board::getBoardTag)
                .collect(Collectors.toSet());

        for (int i = 0; i < boardName.length() - 2; i++) {
            String threeLetterWord = boardName.replace(" ", "").substring(i, i + 3);
            if (!usedThreeLetterWords.contains(threeLetterWord)) {
                return threeLetterWord;
            }
        }

        throw new IllegalStateException("All three-letter substrings have been used. Please choose a different board name.");
    }
}
