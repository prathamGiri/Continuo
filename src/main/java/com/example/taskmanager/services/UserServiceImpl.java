package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.UpdateUserRequest;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public UserServiceImpl(UserRepository userRepository, TeamRepository teamRepository){
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResponse getUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserResponse createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserResponse updateUserTeam(Long userId, UpdateUserRequest request){
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Team newTeam = teamRepository.findById(request.getNewTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (newTeam != existing.getTeam()){
            existing.setTeam(newTeam);
            existing.setRole(UserRole.Member);
        }

        return userRepository.save(existing);
    }

    @Override
    public UserResponse updateUserRole(Long userId, UpdateUserRequest request){
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        switch (request.getNewRole()){
            case UserRole.Member -> {
                if (existing.getTeam() != null && existing.getRole() != UserRole.Member){
                    existing.setRole(request.getNewRole());
                }
                break;
            }
            case UserRole.SysAdmin -> {
                if (existing.getRole() != UserRole.SysAdmin){
                    existing.setTeam(null);
                    existing.setRole(request.getNewRole());
                }
                break;
            }
            case UserRole.TeamAdmin -> {
                if (existing.getTeam() != null && existing.getRole() != UserRole.TeamAdmin){
                    existing.setRole(request.getNewRole());
                }
                break;
            }
            case UserRole.Normal -> {
                if (existing.getRole() != UserRole.Normal){
                    existing.setTeam(null);
                    existing.setRole(request.getNewRole());
                }
            }
        }
        return userRepository.save(existing);
    }

    @Override
    public UserResponse removeUserFromTeam(Long userId, UpdateUserRequest request){
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User Not Found"));
        existingUser.setTeam(null);
        existingUser.setRole(UserRole.Normal);

        userRepository.save(existingUser);
        return existingUser;
    }
}
