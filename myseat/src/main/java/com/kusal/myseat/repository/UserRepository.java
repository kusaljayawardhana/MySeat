package com.kusal.myseat.repository;

import com.kusal.myseat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}