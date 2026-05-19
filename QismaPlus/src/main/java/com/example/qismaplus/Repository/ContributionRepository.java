package com.example.qismaplus.Repository;

import com.example.qismaplus.Model.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Integer> {
    @Query("select c from Contribution c where c.id = ?1")
    Contribution findContributionById(Integer id);

    @Query("select c from Contribution c where c.groupId = ?1")
    List<Contribution> findContributionsByGroupId(Integer groupId);

    @Query("select c from Contribution c where c.status = ?1")
    List<Contribution> findContributionsByStatus(String status);}
