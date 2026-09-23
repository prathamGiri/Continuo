package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.UpdateUserRequest;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.exceptions.*;
import com.example.taskmanager.mappers.UserMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserMapper.toDTO(user);
    }

    @Override
    public UserResponse createUser(User user) {
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request){
        UserResponse updatedUser = null;
        boolean updated = false;
        if (request.getNewRole() != null){
            updatedUser = updateUserRole(userId, request);
            updated=true;
        }
        if (request.getNewTeamId() != null){
            updatedUser = updateUserTeam(userId, request);
            updated=true;
        }
        return updatedUser;
    }

    private UserResponse updateUserTeam(Long userId, UpdateUserRequest request){
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Team newTeam = teamRepository.findById(request.getNewTeamId())
                .orElseThrow(() -> new TeamNotFoundException(request.getNewTeamId()));
        if (existing.getTeam() != null && existing.getTeam().getTeamId().equals(newTeam.getTeamId())){
            throw new DuplicateTeamAssignmentException(newTeam.getTeamName());
        }else if (existing.getRole() == UserRole.SysAdmin){
            throw new DuplicateRoleAssignmentException(UserRole.SysAdmin);
        }else {
            existing.setTeam(newTeam);
            existing.setRole(UserRole.Member);
        }
        return UserMapper.toDTO(userRepository.save(existing));
    }

    private UserResponse updateUserRole(Long userId, UpdateUserRequest request){
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        switch (request.getNewRole()){
            case UserRole.Member -> {
                if (existing.getTeam() == null){
                    throw new NoTeamAssignedException();
                }else if (existing.getRole() == UserRole.Member){
                    throw new DuplicateRoleAssignmentException(UserRole.Member);
                }else{
                    existing.setRole(request.getNewRole());
                }
            }
            case UserRole.SysAdmin -> {
                if (existing.getRole() != UserRole.SysAdmin){
                    existing.setTeam(null);
                    existing.setRole(request.getNewRole());
                }else {
                    throw new DuplicateRoleAssignmentException(UserRole.SysAdmin);
                }
            }
            case UserRole.TeamAdmin -> {
                if (existing.getTeam() == null){
                    throw new NoTeamAssignedException();
                }else if (existing.getRole() == UserRole.TeamAdmin){
                    throw new DuplicateRoleAssignmentException(UserRole.TeamAdmin);
                }else{
                    existing.setRole(request.getNewRole());
                }
            }
            case UserRole.Normal -> {
                if (existing.getRole() != UserRole.Normal){
                    existing.setTeam(null);
                    existing.setRole(request.getNewRole());
                }else{
                    throw new DuplicateRoleAssignmentException(UserRole.Normal);
                }
            }
            default -> {
                throw new NoRoleFoundException(request.getNewRole());
            }
        }
        return UserMapper.toDTO(userRepository.save(existing));
    }

    @Override
    public UserResponse removeUserFromTeam(Long userId, UpdateUserRequest request){
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        existingUser.setTeam(null);
        existingUser.setRole(UserRole.Normal);

        userRepository.save(existingUser);
        return UserMapper.toDTO(userRepository.save(existingUser));
    }
}
