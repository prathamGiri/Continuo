package com.example.taskmanager.services;

import com.example.taskmanager.entities.Team;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    Optional<Team> getTeamById(Long teamId);
    List<Team> getAllTeams();
    Team createTeam(Team team);
}

