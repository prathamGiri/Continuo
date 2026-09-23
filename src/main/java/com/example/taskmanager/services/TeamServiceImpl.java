package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.exceptions.TeamNotFoundException;
import com.example.taskmanager.mappers.TeamMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService{

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public TeamResponse getTeamById(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new TeamNotFoundException(teamId));
        return TeamMapper.teamDTO(team);
    }

    @Override
    public TeamResponse getTeamUsersById(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new TeamNotFoundException(teamId));
        List<User> users = userRepository.findByTeam_TeamId(teamId);
        return TeamMapper.teamUsersDTO(team, users);
    }

    @Override
    public List<TeamResponse> getAllTeams(){
        return teamRepository.findAll()
                .stream()
                .map(TeamMapper::teamDTO)
                .toList();
    }

    @Override
    public TeamResponse createTeam(CreateTeamRequest request){
        Team team = TeamMapper.toEntity(request);
        return TeamMapper.teamDTO(teamRepository.save(team));
    }
}
