package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateUserRequest;
import com.example.taskmanager.dto.requests.UpdateUserRoleRequest;
import com.example.taskmanager.dto.requests.UpdateUserTeamRequest;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.dto.responses.UserResponseWithTeam;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.exceptions.*;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Team engineeringTeam;
    private Team designTeam;
    private User normalUser;

    @BeforeEach
    void setUp() {
        engineeringTeam = new Team();
        engineeringTeam.setTeamId(1L);
        engineeringTeam.setTeamName("Engineering");

        designTeam = new Team();
        designTeam.setTeamId(2L);
        designTeam.setTeamName("Design");

        normalUser = new User();
        normalUser.setUserId(1L);
        normalUser.setUserName("utk");
        normalUser.setEmailId("utk@gmail.com");
        normalUser.setRole(UserRole.Normal);
        normalUser.setTeam(null);
    }

    @Nested
    class GetAllUsers {

        @Test
        void returnsMappedListForAllUsers() {
            User second = new User();
            second.setUserId(2L);
            second.setUserName("jane");
            second.setEmailId("jane@gmail.com");
            second.setRole(UserRole.Member);
            second.setTeam(engineeringTeam);

            when(userRepository.findAll()).thenReturn(List.of(normalUser, second));

            List<UserResponseWithTeam> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            verify(userRepository, times(1)).findAll();
        }

        @Test
        void returnsEmptyListWhenNoUsersExist() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponseWithTeam> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetUserById {

        @Test
        void returnsUserWhenFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

            UserResponseWithTeam result = userService.getUserById(1L);

            assertEquals(1L, result.getUserId());
        }

        @Test
        void throwsUserNotFoundWhenMissing() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> userService.getUserById(99L));
        }
    }

    // ------------------------------------------------------------------
    // createUser
    // ------------------------------------------------------------------
    @Nested
    class CreateUser {

        @Test
        void savesAndReturnsMappedUser() {
            CreateUserRequest request = new CreateUserRequest("new_user", "new_user@gmail.com", "password123");

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setUserId(10L);
                return saved;
            });

            UserResponse result = userService.createUser(request);

            assertEquals("new_user", result.getUserName());
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    // ------------------------------------------------------------------
    // updateUserTeam
    // ------------------------------------------------------------------
    @Nested
    class UpdateUserTeam {

        @Test
        void throwsUserNotFoundWhenUserMissing() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());
            UpdateUserTeamRequest request = new UpdateUserTeamRequest(1L);

            assertThrows(UserNotFoundException.class,
                    () -> userService.updateUserTeam(99L, request));

            verify(teamRepository, never()).findById(any());
        }

        @Test
        void throwsTeamNotFoundWhenTeamMissing() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(teamRepository.findById(5L)).thenReturn(Optional.empty());
            UpdateUserTeamRequest request = new UpdateUserTeamRequest(5L);

            assertThrows(TeamNotFoundException.class,
                    () -> userService.updateUserTeam(1L, request));
        }

        @Test
        void throwsDuplicateTeamAssignmentWhenAlreadyOnThatTeam() {
            normalUser.setTeam(engineeringTeam);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(teamRepository.findById(1L)).thenReturn(Optional.of(engineeringTeam));
            UpdateUserTeamRequest request = new UpdateUserTeamRequest(1L);

            assertThrows(DuplicateTeamAssignmentException.class,
                    () -> userService.updateUserTeam(1L, request));

            verify(userRepository, never()).save(any());
        }

        @Test
        void throwsDuplicateRoleAssignmentWhenUserIsSysAdmin() {
            normalUser.setRole(UserRole.SysAdmin);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(teamRepository.findById(2L)).thenReturn(Optional.of(designTeam));
            UpdateUserTeamRequest request = new UpdateUserTeamRequest(2L);

            assertThrows(DuplicateRoleAssignmentException.class,
                    () -> userService.updateUserTeam(1L, request));

            verify(userRepository, never()).save(any());
        }

        @Test
        void assignsNewTeamAndSetsRoleToMemberOnSuccess() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.TeamAdmin);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(teamRepository.findById(2L)).thenReturn(Optional.of(designTeam));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            UpdateUserTeamRequest request = new UpdateUserTeamRequest(2L);

            UserResponseWithTeam result = userService.updateUserTeam(1L, request);

            assertEquals(UserRole.Member, normalUser.getRole());
            assertEquals(2L, normalUser.getTeam().getTeamId());
            verify(userRepository, times(1)).save(normalUser);
        }
    }

    // ------------------------------------------------------------------
    // updateUserRole
    // ------------------------------------------------------------------
    @Nested
    class UpdateUserRole {

        private UpdateUserRoleRequest requestWithRole(UserRole role) {
            return new UpdateUserRoleRequest(role);
        }

        @Test
        void throwsUserNotFoundWhenMissing() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> userService.updateUserRole(99L, requestWithRole(UserRole.Member)));
        }

        // ---- Member ----
        @Test
        void member_throwsNoTeamAssignedWhenUserHasNoTeam() {
            normalUser.setTeam(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

            assertThrows(NoTeamAssignedException.class,
                    () -> userService.updateUserRole(1L, requestWithRole(UserRole.Member)));
        }

        @Test
        void member_throwsDuplicateWhenAlreadyMember() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.Member);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

            assertThrows(DuplicateRoleAssignmentException.class,
                    () -> userService.updateUserRole(1L, requestWithRole(UserRole.Member)));
        }

        @Test
        void member_succeedsWhenUserHasTeamAndIsNotAlreadyMember() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.Normal);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUserRole(1L, requestWithRole(UserRole.Member));

            assertEquals(UserRole.Member, normalUser.getRole());
        }

        // ---- SysAdmin ----
        @Test
        void sysAdmin_throwsDuplicateWhenAlreadySysAdmin() {
            normalUser.setRole(UserRole.SysAdmin);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

            assertThrows(DuplicateRoleAssignmentException.class,
                    () -> userService.updateUserRole(1L, requestWithRole(UserRole.SysAdmin)));
        }

        @Test
        void sysAdmin_succeedsAndClearsTeam() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.TeamAdmin);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUserRole(1L, requestWithRole(UserRole.SysAdmin));

            assertEquals(UserRole.SysAdmin, normalUser.getRole());
            assertNull(normalUser.getTeam());
        }

        // ---- TeamAdmin ----
        @Test
        void teamAdmin_throwsNoTeamAssignedWhenUserHasNoTeam() {
            normalUser.setTeam(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

            assertThrows(NoTeamAssignedException.class,
                    () -> userService.updateUserRole(1L, requestWithRole(UserRole.TeamAdmin)));
        }

        @Test
        void teamAdmin_throwsDuplicateWhenAlreadyTeamAdmin() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.TeamAdmin);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

            assertThrows(DuplicateRoleAssignmentException.class,
                    () -> userService.updateUserRole(1L, requestWithRole(UserRole.TeamAdmin)));
        }

        @Test
        void teamAdmin_succeedsWhenUserHasTeamAndIsNotAlreadyTeamAdmin() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.Member);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUserRole(1L, requestWithRole(UserRole.TeamAdmin));

            assertEquals(UserRole.TeamAdmin, normalUser.getRole());
        }

        // ---- Normal ----
        @Test
        void normal_throwsDuplicateWhenAlreadyNormal() {
            normalUser.setRole(UserRole.Normal);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

            assertThrows(DuplicateRoleAssignmentException.class,
                    () -> userService.updateUserRole(1L, requestWithRole(UserRole.Normal)));
        }

        @Test
        void normal_succeedsAndClearsTeam() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.Member);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUserRole(1L, requestWithRole(UserRole.Normal));

            assertEquals(UserRole.Normal, normalUser.getRole());
            assertNull(normalUser.getTeam());
        }
    }

    // ------------------------------------------------------------------
    // removeUserFromTeam
    // ------------------------------------------------------------------
    @Nested
    class RemoveUserFromTeam {

        @Test
        void throwsUserNotFoundWhenMissing() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> userService.removeUserFromTeam(99L));
        }

        @Test
        void clearsTeamAndSetsRoleToNormal() {
            normalUser.setTeam(engineeringTeam);
            normalUser.setRole(UserRole.Member);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.removeUserFromTeam(1L);

            assertEquals(UserRole.Normal, normalUser.getRole());
            assertNull(normalUser.getTeam());
        }

        @Test
        void savesUserTwiceDueToImplementationBug() {
            // NOTE: removeUserFromTeam currently calls userRepository.save(existingUser)
            // twice — once discarded, once returned. This test documents that behavior;
            // consider removing the redundant first save() call in the implementation.
            normalUser.setTeam(engineeringTeam);
            when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.removeUserFromTeam(1L);

            verify(userRepository, times(2)).save(normalUser);
        }
    }
}