package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.dto.responses.TeamResponseWithUserList;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.exceptions.TeamNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team engineeringTeam;
    private User memberOne;
    private User memberTwo;

    @BeforeEach
    void setUp() {
        engineeringTeam = new Team();
        engineeringTeam.setTeamId(1L);
        engineeringTeam.setTeamName("Engineering");

        memberOne = new User();
        memberOne.setUserId(10L);
        memberOne.setUserName("alice");
        memberOne.setEmailId("alice@gmail.com");
        memberOne.setRole(UserRole.Member);
        memberOne.setTeam(engineeringTeam);

        memberTwo = new User();
        memberTwo.setUserId(11L);
        memberTwo.setUserName("bob");
        memberTwo.setEmailId("bob@gmail.com");
        memberTwo.setRole(UserRole.TeamAdmin);
        memberTwo.setTeam(engineeringTeam);
    }

    // ------------------------------------------------------------------
    // getTeamById
    // ------------------------------------------------------------------
    @Nested
    class GetTeamById {

        @Test
        void returnsTeamWithUsersWhenFound() {
            when(teamRepository.findById(1L)).thenReturn(Optional.of(engineeringTeam));
            when(userRepository.findByTeam(engineeringTeam)).thenReturn(List.of(memberOne, memberTwo));

            TeamResponseWithUserList result = teamService.getTeamById(1L);

            assertEquals(1L, result.getTeamId());
            assertThat(result.getTeamUsersList()).hasSize(2);
        }

        @Test
        void returnsTeamWithEmptyListWhenNoUsersAssigned() {
            when(teamRepository.findById(1L)).thenReturn(Optional.of(engineeringTeam));
            when(userRepository.findByTeam(engineeringTeam)).thenReturn(List.of());

            TeamResponseWithUserList result = teamService.getTeamById(1L);

            assertThat(result.getTeamUsersList()).isEmpty();
        }

        @Test
        void throwsTeamNotFoundWhenMissing() {
            when(teamRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> teamService.getTeamById(99L));

            verify(userRepository, never()).findByTeam(any());
        }
    }

    // ------------------------------------------------------------------
    // getTeamUsersById
    // NOTE: this method is currently byte-for-byte identical to getTeamById
    // (same lookup, same exception, same mapper call). Tests mirror
    // GetTeamById above; worth considering whether one should just delegate
    // to the other, or whether they're meant to diverge later.
    // ------------------------------------------------------------------
    @Nested
    class GetTeamUsersById {

        @Test
        void returnsTeamWithUsersWhenFound() {
            when(teamRepository.findById(1L)).thenReturn(Optional.of(engineeringTeam));
            when(userRepository.findByTeam(engineeringTeam)).thenReturn(List.of(memberOne, memberTwo));

            TeamResponseWithUserList result = teamService.getTeamUsersById(1L);

            assertEquals(1L, result.getTeamId());
            assertThat(result.getTeamUsersList()).hasSize(2);
        }

        @Test
        void throwsTeamNotFoundWhenMissing() {
            when(teamRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> teamService.getTeamUsersById(99L));
        }
    }

    @Nested
    class GetAllTeams {

        @Test
        void returnsAllTeamsWithTheirUsers() {
            Team designTeam = new Team();
            designTeam.setTeamId(2L);
            designTeam.setTeamName("Design");

            when(teamRepository.findAll()).thenReturn(List.of(engineeringTeam, designTeam));
            when(userRepository.findByTeam(engineeringTeam)).thenReturn(List.of(memberOne, memberTwo));
            when(userRepository.findByTeam(designTeam)).thenReturn(List.of());

            List<TeamResponseWithUserList> result = teamService.getAllTeams();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTeamUsersList()).hasSize(2);
            assertThat(result.get(1).getTeamUsersList()).isEmpty();
            verify(userRepository, times(1)).findByTeam(engineeringTeam);
            verify(userRepository, times(1)).findByTeam(designTeam);
        }

        @Test
        void returnsEmptyListWhenNoTeamsExist() {
            when(teamRepository.findAll()).thenReturn(List.of());

            List<TeamResponseWithUserList> result = teamService.getAllTeams();

            assertThat(result).isEmpty();
            verify(userRepository, never()).findByTeam(any());
        }
    }

    // ------------------------------------------------------------------
    // createTeam
    // ------------------------------------------------------------------
    @Nested
    class CreateTeam {

        @Test
        void savesAndReturnsMappedTeam() {
            CreateTeamRequest request = new CreateTeamRequest("Marketing");

            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
                Team saved = invocation.getArgument(0);
                saved.setTeamId(5L);
                return saved;
            });

            TeamResponse result = teamService.createTeam(request);

            assertEquals("Marketing", result.getTeamName());
            assertEquals(5L, result.getTeamId());
            verify(teamRepository, times(1)).save(any(Team.class));
        }
    }
}