package com.example.taskmanager.controller;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.services.TeamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService){
        this.teamService = teamService;
    }

    @GetMapping("/teams")
    public List<Team> getAllTeams(){
        return teamService.getAllTeams();
    }

    @GetMapping("/teams/{teamId}")
    public Optional<Team> getTeamById(@PathVariable Long teamId){
        return teamService.getTeamById(teamId);
    }

    @PostMapping("/teams")
    public Team createTeam(@RequestBody Team team){
        return teamService.createTeam(team);
    }
}
