package com.example.taskmanager.controller;

import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public List<TeamResponse> getAllTeams(){
        return teamService.getAllTeams();
    }

    @GetMapping("/{teamId}")
    public TeamResponse getTeamById(@PathVariable Long teamId){
        return teamService.getTeamById(teamId);
    }

    @GetMapping("/{teamId}/users")
    public TeamResponse getTeamUsersById(@PathVariable Long teamId){
        return teamService.getTeamUsersById(teamId);
    }

    @PostMapping
    public TeamResponse createTeam(@RequestBody Team team){
        return teamService.createTeam(team);
    }
}
