package com.example.taskmanager.mappers;

import com.example.taskmanager.dto.requests.CreateTeamRequest;
import com.example.taskmanager.dto.responses.TeamResponse;
import com.example.taskmanager.dto.responses.TeamResponseWithUserList;
import com.example.taskmanager.dto.responses.UserResponse;
import com.example.taskmanager.dto.responses.UserResponseWithTeam;
import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;

import java.util.List;

public class TeamMapper {
    public static TeamResponse teamDTO(Team team){
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .build();
    }

    public static TeamResponseWithUserList teamUsersDTO(Team team, List<User> users){
        List<UserResponse> parsedUsers = users.stream().map(UserMapper::toDTOWithoutTeam).toList();
        return TeamResponseWithUserList.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .teamUsersList(parsedUsers)
                .build();
    }

    public static Team toEntity(CreateTeamRequest req){
        return Team.builder()
                .teamName(req.getTeamName())
                .build();
    }
}
