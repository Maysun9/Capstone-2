package com.example.qismaplus.Repository;

import com.example.qismaplus.Model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {


    @Query("select g from Group g where g.id = ?1")
    Group findGroupById(Integer id);

    @Query("select g from Group g where g.createdByUserId = ?1")
    List<Group> findGroupsByCreatedByUserId(Integer userId);

}
