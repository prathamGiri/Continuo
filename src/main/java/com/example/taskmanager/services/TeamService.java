package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.entities.Team;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    TeamResponse getTeamById(Long teamId);
    List<TeamResponse> getAllTeams();
    TeamResponse createTeam(CreateTeamRequest team);
    TeamResponse getTeamUsersById(Long teamId);
}

