package com.example.taskmanager.services;

import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.mappers.TeamResponseMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService{

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamServiceImpl(TeamRepository teamRepository, UserRepository userRepository){
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TeamResponse getTeamById(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new RuntimeException("Team Not Found"));
        return TeamResponseMapper.teamDTO(team);
    }

    @Override
    public TeamResponse getTeamUsersById(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new RuntimeException("Team Not Found"));
        List<User> users = userRepository.findByTeam_TeamId(teamId);
        return TeamResponseMapper.teamUsersDTO(team, users);
    }

    @Override
    public List<TeamResponse> getAllTeams(){
        return teamRepository.findAll()
                .stream()
                .map(TeamResponseMapper::teamDTO)
                .toList();
    }

    @Override
    public TeamResponse createTeam(Team team){
        return TeamResponseMapper.teamDTO(teamRepository.save(team));
    }
}
