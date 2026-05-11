package com.derabbit.seolstudy.domain.planner.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.derabbit.seolstudy.domain.planner.Planner;

public interface PlannerRepository extends JpaRepository<Planner, Long> {

    Optional<Planner> findByUser_IdAndDate(Long userId, LocalDate date);
}
