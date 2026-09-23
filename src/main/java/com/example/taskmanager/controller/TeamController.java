package com.example.taskmanager.controller;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.dto.responses.TeamResponseWithUserList;
import com.example.taskmanager.services.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamResponseWithUserList>> getAllTeams(){
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseWithUserList> getTeamById(@PathVariable Long teamId){
        return ResponseEntity.ok(teamService.getTeamById(teamId));
    }

    @GetMapping("/{teamId}/users")
    public ResponseEntity<TeamResponseWithUserList> getTeamUsersById(@PathVariable Long teamId){
        return ResponseEntity.ok(teamService.getTeamUsersById(teamId));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(req));
    }
}
