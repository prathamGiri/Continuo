package com.example.taskmanager.controller;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams(){
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long teamId){
        return ResponseEntity.ok(teamService.getTeamById(teamId));
    }

    @GetMapping("/{teamId}/users")
    public ResponseEntity<TeamResponse> getTeamUsersById(@PathVariable Long teamId){
        return ResponseEntity.ok(teamService.getTeamUsersById(teamId));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@RequestBody CreateTeamRequest team){
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(team));
    }
}
