package com.example.qismaplus.Repository;

import com.example.qismaplus.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("select p from Payment p where p.id = ?1")
    Payment findPaymentById(Integer id);

    @Query("select p from Payment p where p.userId = ?1")
    List<Payment> findPaymentsByUserId(Integer userId);

    @Query("select p from Payment p where p.contributionId = ?1")
    List<Payment> findPaymentsByContributionId(Integer contributionId);

    @Query("select p from Payment p where p.userId = ?1 and p.status = 'UNPAID'")
    List<Payment> findUnpaidPaymentsByUserId(Integer userId);

    @Query("select count(p) from Payment p where p.userId = ?1 and p.contributionId = ?2 and p.status = 'PAID'")
    int countPaidPaymentsByUserIdAndContributionId(Integer userId, Integer contributionId);

    boolean existsByUserIdAndContributionIdAndMonth(Integer userId, Integer contributionId, String month);

    @Query("select p from Payment p where p.contributionId = ?1 and p.userId = ?2 and p.status = 'UNPAID'")
    List<Payment> findUnpaidPaymentsByContributionAndUser(Integer contributionId, Integer userId);
    List<Payment> findByUserIdOrderByPaymentDateAsc(Integer userId);

}