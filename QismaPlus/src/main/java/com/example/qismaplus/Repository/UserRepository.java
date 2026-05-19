package com.example.qismaplus.Repository;

import com.example.qismaplus.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("select u from User u where u.id = ?1")
    User findUserById(Integer id);

    @Query("select u from User u where u.email = ?1")
    User findUserByEmail(String email);
}