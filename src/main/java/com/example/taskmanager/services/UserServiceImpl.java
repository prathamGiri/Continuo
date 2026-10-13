package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateUserRequest;
import com.example.taskmanager.dto.requests.LoginRequest;
import com.example.taskmanager.dto.requests.UpdateUserRoleRequest;
import com.example.taskmanager.dto.requests.UpdateUserTeamRequest;
import com.example.taskmanager.dto.responses.AuthResponse;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.dto.responses.UserResponseWithTeam;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.exceptions.*;
import com.example.taskmanager.mappers.UserMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.AccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AccessPolicy accessPolicy;

    @Override
    public List<UserResponseWithTeam> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTOWithTeam)
                .toList();
    }

    @Override
    public UserResponseWithTeam getUserById(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserMapper.toDTOWithTeam(user);
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return UserMapper.toDTOWithoutTeam(userRepository.save(user));
    }

    @Override
    public AuthResponse loginUser(LoginRequest request){
        User user = userRepository.findUserByEmailId(request.getEmailId())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return tokenService.generateToken(user);
    }

    @Override
    public UserResponseWithTeam updateUserTeam(Long actorId, Long userId, UpdateUserTeamRequest request){
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Team newTeam = teamRepository.findById(request.getNewTeamId())
                .orElseThrow(() -> new TeamNotFoundException(request.getNewTeamId()));
        accessPolicy.checkCanAddToTeam(actorId, existing, newTeam.getTeamId());
        if (existing.getTeam() != null && existing.getTeam().getTeamId().equals(newTeam.getTeamId())){
            throw new DuplicateTeamAssignmentException(newTeam.getTeamName());
        }else if (existing.getRole() == UserRole.SysAdmin){
            throw new DuplicateRoleAssignmentException(UserRole.SysAdmin);
        }else {
            existing.setTeam(newTeam);
            existing.setRole(UserRole.Member);
        }
        return UserMapper.toDTOWithTeam(userRepository.save(existing));
    }

    @Override
    public UserResponseWithTeam updateUserRole(Long userId, UpdateUserRoleRequest request){
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
        return UserMapper.toDTOWithTeam(userRepository.save(existing));
    }

    @Override
    public UserResponseWithTeam removeUserFromTeam(Long actorId, Long userId){
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        Long currentTeamId = existingUser.getTeam() == null ? null : existingUser.getTeam().getTeamId();
        accessPolicy.checkCanManageTeamMembers(actorId, currentTeamId);
        existingUser.setTeam(null);
        existingUser.setRole(UserRole.Normal);

        userRepository.save(existingUser);
        return UserMapper.toDTOWithTeam(userRepository.save(existingUser));
    }
}
