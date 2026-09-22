package com.example.taskmanager.repository;

import com.example.taskmanager.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

}
