package com.derabbit.seolstudy.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.derabbit.seolstudy.domain.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);

    List<User> findAllByMentorId(Long mentorId);

}
