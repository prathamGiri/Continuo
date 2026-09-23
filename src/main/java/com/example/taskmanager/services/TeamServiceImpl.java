package com.example.taskmanager.services;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.dto.responses.TeamResponseWithUserList;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import com.example.taskmanager.exceptions.TeamNotFoundException;
import com.example.taskmanager.mappers.TeamMapper;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService{

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public TeamResponseWithUserList getTeamById(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new TeamNotFoundException(teamId));
        List<User> users = userRepository.findByTeam(team);
        return TeamMapper.teamUsersDTO(team, users);
    }

    @Override
    public TeamResponseWithUserList getTeamUsersById(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new TeamNotFoundException(teamId));
        List<User> users = userRepository.findByTeam(team);
        return TeamMapper.teamUsersDTO(team, users);
    }

    @Override
    public List<TeamResponseWithUserList> getAllTeams(){
        List<Team> listOfTeams = teamRepository.findAll();
        List<TeamResponseWithUserList> result = new ArrayList<>();
        for (var team : listOfTeams){
            List<User> users = userRepository.findByTeam(team);
            result.add(TeamMapper.teamUsersDTO(team, users));
        }
        return result;
    }

    @Override
    public TeamResponse createTeam(CreateTeamRequest request){
        Team team = TeamMapper.toEntity(request);
        return TeamMapper.teamDTO(teamRepository.save(team));
    }
}
