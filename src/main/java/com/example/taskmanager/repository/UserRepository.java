package com.example.taskmanager.repository;


import com.example.taskmanager.entities.Team;
import com.example.taskmanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByTeam(Team team);
    Optional<User> findUserByEmailId(String emailId);
}
