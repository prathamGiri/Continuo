package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.dto.responses.TeamResponseWithUserList;

import java.util.List;

public interface TeamService {
    TeamResponseWithUserList getTeamById(Long teamId);
    List<TeamResponseWithUserList> getAllTeams();
    TeamResponse createTeam(CreateTeamRequest team);
    TeamResponseWithUserList getTeamUsersById(Long teamId);
}

