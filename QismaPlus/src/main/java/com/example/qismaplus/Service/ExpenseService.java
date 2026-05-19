package com.example.qismaplus.Service;

import com.example.qismaplus.API.ApiException;
import com.example.qismaplus.Model.Expense;
import com.example.qismaplus.Model.User;
import com.example.qismaplus.Repository.ExpenseRepository;
import com.example.qismaplus.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public void addExpense(Expense expense) {
        User user = userRepository.findUserById(expense.getUserId());
        if (user == null) {
            throw new ApiException("sorry, user id not found");
        }
        expenseRepository.save(expense);
    }

    public void updateExpense(Integer id, Expense expense) {
        Expense e = expenseRepository.findExpenseById(id);
        if (e == null) {
            throw new ApiException("sorry, expense not found");
        }
        User user = userRepository.findUserById(expense.getUserId());
        if (user == null) {
            throw new ApiException("sorry, user id not found");
        }
        e.setTitle(expense.getTitle());
        e.setAmount(expense.getAmount());
        e.setCategory(expense.getCategory());
        e.setDate(expense.getDate());
        e.setUserId(expense.getUserId());
        expenseRepository.save(e);
    }

    public void deleteExpense(Integer id) {
        Expense e = expenseRepository.findExpenseById(id);
        if (e == null) {
            throw new ApiException("sorry, expense not found");
        }
        expenseRepository.delete(e);
    }

    // ----------------------------------------------------------END CRUD---------------------------------------------------------------------

    // get all expenses for one user
    public List<Expense> getExpensesByUserId(Integer userId) {
        //استدعيها من الداتا
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("sorry, user id not found");
        }
        //راح اجيب كل المصاريف الخاصة باليوزر
        List<Expense> expenses = expenseRepository.findExpensesByUserId(userId);
        //اذا اليوزر ما صرف بيرجع رسالة
        if (expenses.isEmpty()) {
            throw new ApiException("no expenses found for this user");
        }
        //يرجع لست
        return expenses;
    }

    // get expenses by category for a user
    public List<Expense> getExpensesByCategoryAndUser(String category, Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("sorry, user id not found");
        }
        //يجيب المصاريف الخاصه باليوزر بنفس الكاتقوري
        List<Expense> expenses = expenseRepository.findExpensesByCategoryAndUserId(category, userId);
        if (expenses.isEmpty()) {
            throw new ApiException("no expenses found for this category");
        }
        return expenses;
    }

    // get total expenses for a user
    public Map<String, Object> getTotalExpensesByUserId(Integer userId) {

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("sorry, user id not found");
        }

        Double total = expenseRepository.sumExpensesByUserId(userId);
        total = (total == null) ? 0.0 : total;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("User Id", userId);
        response.put("User Name", user.getName());
        response.put("Total Expenses", total);

        return response;
    }


    public Map<String, Object> getTopCategory(Integer userId) {
        List<Object[]> result = expenseRepository.findCategoryTotals(userId);
        if (result.isEmpty()) {
            throw new ApiException("no expenses found");
        }
        Object[] top = result.get(0);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("User Id", userId);
        response.put("Top Category", top[0]);
        response.put("Total Spent", top[1]);
        return response;
    }
    //مجموع الكاتقوري يتحول لنسب
    public Map<String, Object> getExpenseCategoryPercentage(Integer userId) {

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }

        List<Object[]> result = expenseRepository.getExpenseCategorySummary(userId);

        if (result.isEmpty()) {
            throw new ApiException("no expenses found");
        }

        // نحسب الإجمالي
        double total = result.stream()
                .mapToDouble(r -> (Double) r[1])
                .sum();

        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> categories = new ArrayList<>();

        for (Object[] row : result) {

            String category = (String) row[0];
            double amount = (Double) row[1];

            double percentage = (amount / total) * 100;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("category", category);
            data.put("amount", amount);
            data.put("percentage", String.format("%.1f", percentage) + "%");

            categories.add(data);
        }

        response.put("user", user.getName());
        response.put("totalExpenses", total);
        response.put("categories", categories);

        return response;
    }

}
