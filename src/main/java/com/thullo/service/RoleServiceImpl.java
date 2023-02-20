package com.thullo.service;

import com.thullo.data.model.*;
import com.thullo.data.repository.PrivilegeRepository;
import com.thullo.data.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;


    public Role createOwnerRole() {
        Role ownerRole = roleRepository.findByName("ROLE_OWNER").orElseThrow();
        List<Privilege> ownerPrivileges = privilegeRepository.findAllByNameIn(Arrays.asList(
                "READ_PRIVILEGE",
                "WRITE_PRIVILEGE"
        ));
        ownerRole.setPrivileges(ownerPrivileges);
        return roleRepository.save(ownerRole);
    }

    public Role createBoardRole(String boardTag) {
        String roleName = "ROLE_BOARD_" + boardTag;
        Role boardRole = roleRepository.findByName(roleName).orElse(null);
        if (boardRole == null) {
            boardRole = new Role(roleName);
        }
        List<Privilege> boardPrivileges = privilegeRepository.findAllByNameIn(Arrays.asList(
                "BOARD_CREATE_TASK_PRIVILEGE",
                "BOARD_VIEW_PRIVILEGE",
                "BOARD_UPDATE_TASK_PRIVILEGE",
                "BOARD_DELETE_TASK_PRIVILEGE",
                "BOARD_ADD_MEMBER_PRIVILEGE"
        ));
        boardRole.setPrivileges(boardPrivileges);
        return roleRepository.save(boardRole);
    }

    public Role createTaskRole(String boardRef) {
        String roleName = "ROLE_TASK_" + boardRef;
        Role taskRole = roleRepository.findByName(roleName).orElse(null);
        if (taskRole == null) {
            taskRole = new Role(roleName);
        }
        List<Privilege> boardPrivileges = privilegeRepository.findAllByNameIn(Arrays.asList(
                "TASK_UPDATE_TASK_PRIVILEGE",
                "TASK_VIEW_PRIVILEGE"
        ));
        taskRole.setPrivileges(boardPrivileges);
        return roleRepository.save(taskRole);
    }

    public void addTaskRoleToUser(User user, Board board) {
        String boardTag = board.getBoardTag();
        Role boardRole = createBoardRole(boardTag);
        user.getRoles().add(boardRole);
        roleRepository.save(boardRole);
    }

    public void addTaskRoleToUser(User user, Task task) {
        String boardRef = task.getBoardRef();
        Role taskRole = createTaskRole(boardRef);
        user.getRoles().add(taskRole);
        roleRepository.save(taskRole);
    }

    public void addOwnerRoleToUser(User user) {
        Role ownerRole = createOwnerRole();
        user.getRoles().add(ownerRole);
        roleRepository.save(ownerRole);
    }

    public void addBoardRoleToBoardCreator(User user, String boardTag) {
        Role creatorRole = boardCreatorRole(user.getEmail(), boardTag);
        user.getRoles().add(creatorRole);
        roleRepository.save(creatorRole);
    }

    private Role boardCreatorRole(String email, String boardTag) {
        return new Role("ROLE_BOARD_" + boardTag + "_" + email);
    }

    public Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName).orElse(null);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public void removeBoardRoleFromUser(User user, Board board) {
        String boardTag = board.getBoardTag();
        String roleName = "ROLE_TASK_" + boardTag;
        roleRepository.findByName(roleName).ifPresent(boardRole -> user.getRoles().remove(boardRole));
    }

    public void removeTaskRoleFromUser(User user, Task task) {
        String boardRef = task.getBoardRef();
        String roleName = "ROLE_TASK_" + boardRef;
        roleRepository.findByName(roleName).ifPresent(taskRole -> user.getRoles().remove(taskRole));
    }
}
