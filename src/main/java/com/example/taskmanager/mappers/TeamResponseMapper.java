package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;

import java.util.List;

public class TeamResponseMapper {
    public static TeamResponse teamDTO(Team team){
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .build();
    }

    public static TeamResponse teamUsersDTO(Team team, List<User> users){
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .teamUsersList(users.stream().map(UserResponseMapper::toDTO).toList())
                .build();
    }
}
