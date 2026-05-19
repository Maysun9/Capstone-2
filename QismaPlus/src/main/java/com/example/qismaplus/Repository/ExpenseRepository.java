package com.example.qismaplus.Repository;

import com.example.qismaplus.Model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    @Query("select e from Expense e where e.id = ?1")
    Expense findExpenseById(Integer id);

    @Query("select e from Expense e where e.userId = ?1")
    List<Expense> findExpensesByUserId(Integer userId);

    @Query("select e from Expense e where e.category = ?1 and e.userId = ?2")
    List<Expense> findExpensesByCategoryAndUserId(String category, Integer userId);

    @Query("select sum(e.amount) from Expense e where e.userId = ?1")
    Double sumExpensesByUserId(Integer userId);

    @Query("""
select e.category, sum(e.amount)
    from Expense e where e.userId = ?1 group by e.category
    order by sum(e.amount) desc""")
    List<Object[]> findCategoryTotals(Integer userId);
    @Query("""
        SELECT e.category, SUM(e.amount)
        FROM Expense e
        WHERE e.userId = :userId
        GROUP BY e.category
    """)
    List<Object[]> getExpenseCategorySummary(Integer userId);
}
