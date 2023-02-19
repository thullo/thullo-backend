package com.thullo.service;


import com.thullo.data.model.*;
import com.thullo.data.repository.BoardRepository;
import com.thullo.data.repository.UserRepository;
import com.thullo.security.UserPrincipal;
import com.thullo.util.Helper;
import com.thullo.web.exception.BadRequestException;
import com.thullo.web.exception.UserException;
import com.thullo.web.payload.request.BoardRequest;
import com.thullo.web.payload.response.BoardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
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

    private final RoleServiceImpl roleService;

    private final NotificationService notificationService;


    /**
     * Creates a new board based on the provided board request.
     *
     * @param boardRequest The request containing the information for the new board to be created.
     * @return A response object containing the result of the board creation process.
     */

    public Board createBoard(BoardRequest boardRequest, UserPrincipal userPrincipal) throws UserException, BadRequestException, IOException {
        if (Helper.isNullOrEmpty(boardRequest.getName())) throw new BadRequestException("Board name cannot be empty");
        User user = internalFindUserByEmail(userPrincipal.getEmail());
        Board board = mapper.map(boardRequest, Board.class);
        board.setUser(user);
        String imageUrl = null;
        if (boardRequest.getFile() != null) {
            imageUrl = fileService.uploadFile(boardRequest.getFile(), boardRequest.getRequestUrl());
        }
        board.setImageUrl(imageUrl);
        board.setBoardTag(generateThreeLetterWord(boardRequest.getName().toUpperCase()));
        roleService.addBoardRoleToBoardCreator(user, board.getBoardTag());
        return boardRepository.save(board);
    }

    @Override
    public BoardResponse getBoard(String boardTag) throws BadRequestException {
        Board board = getBoardInternal(boardTag);
        return getBoardResponse(board);
    }

    private Board getBoardInternal(String boardTag) throws BadRequestException {
        return boardRepository.findByBoardTag(boardTag).orElseThrow(() -> new BadRequestException("Board not found!"));
    }

    private BoardResponse getBoardResponse(Board board) {
        BoardResponse boardResponse = mapper.map(board, BoardResponse.class);

        List<Task> tasks = board.getTasks();
        Map<Status, List<Task>> columns = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus));

        if (columns.isEmpty()) {
            Arrays.stream(Status.values()).forEach(columnName -> {
                BoardResponse.Column column = new BoardResponse.Column();
                column.setName(columnName.getContent());
                column.setTasks(Collections.emptyList());
                boardResponse.getTaskColumn().add(column);
            });
        } else {
            Arrays.stream(Status.values()).forEach(columnName -> {
                BoardResponse.Column column = new BoardResponse.Column();
                column.setName(columnName.getContent());
                List<Task> columnTasks = columns.getOrDefault(columnName, Collections.emptyList());
                column.setTasks(columnTasks);
                boardResponse.getTaskColumn().add(column);
            });
        }

        return boardResponse;
    }

    @Override
    public List<BoardResponse> getBoards(UserPrincipal userPrincipal) throws UserException {
        User user = internalFindUserByEmail(userPrincipal.getEmail());
        List<Board> allUserBoards = boardRepository.getAllByUserOrderByCreatedAtAsc(user);
        List<BoardResponse> boardResponses = new ArrayList<>(allUserBoards.size());

        for (Board board : allUserBoards) {
            boardResponses.add(getBoardResponse(board));
        }

        return boardResponses;
    }

    @Override
    public void addCollaboratorToBoard(String boardTag, Set<String> collaborators) throws BadRequestException {
        String title = "You have been added as a collaborator on board: " + boardTag;
        String message = "You have been added as a collaborator on board " + boardTag;

        Board board = getBoardInternal(boardTag);
        Set<User> existingCollaborators = board.getCollaborators();
        List<User> newCollaborators = userRepository.findAllByEmails(collaborators);
        for (User contributor : newCollaborators) {
            if (!existingCollaborators.contains(contributor)) {
                existingCollaborators.add(contributor);
                roleService.addBoardRoleToUser(contributor, board);
                notificationService.sendNotificationToUser(contributor, message, title, NotificationType.ADDED_AS_COLLABORATOR);
            }
        }
    }


    @Override
    public void removeCollaboratorsFromBoard(String boardTag, Set<String> emails) throws BadRequestException {
        String title = "You have been removed as a collaborator on board: " + boardTag;
        String message = "You have been removed as a collaborator on board " + boardTag;

        Board board = getBoardInternal(boardTag);
        Set<User> existingCollaborators = board.getCollaborators();
        List<User> usersToRemove = userRepository.findAllByEmails(emails);
        for (User userToRemove : usersToRemove) {
            if (existingCollaborators.contains(userToRemove)) {
                existingCollaborators.remove(userToRemove);
                roleService.removeBoardRoleFromUser(userToRemove, board);
                notificationService.sendNotificationToUser(userToRemove, message, title, NotificationType.REMOVED_AS_COLLABORATOR);
            }
        }
    }


    private User internalFindUserByEmail(String email) throws UserException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserException(format("user not found with email %s", email)));
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
