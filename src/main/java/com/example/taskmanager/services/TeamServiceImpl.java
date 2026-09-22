package com.example.taskmanager.services;

import com.example.taskmanager.entities.Team;
import com.example.taskmanager.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService{

    private final TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository){
        this.teamRepository = teamRepository;
    }

    @Override
    public Optional<Team> getTeamById(Long teamId){
        return teamRepository.findById(teamId);
    }

    @Override
    public List<Team> getAllTeams(){
        return teamRepository.findAll();
    }

    @Override
    public Team createTeam(Team team){
        return teamRepository.save(team);
    }
}
