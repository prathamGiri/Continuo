package com.example.taskmanager.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponseWithUserList {
    private Long teamId;
    private String teamName;
    private List<UserResponse> teamUsersList;
}
