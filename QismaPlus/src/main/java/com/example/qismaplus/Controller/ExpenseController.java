package com.example.qismaplus.Controller;

import com.example.qismaplus.API.ApiResponse;
import com.example.qismaplus.Model.Expense;
import com.example.qismaplus.Service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expense")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllExpenses() {
        return ResponseEntity.status(200).body(expenseService.getAllExpenses());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addExpense(@RequestBody @Valid Expense expense) {
        expenseService.addExpense(expense);
        return ResponseEntity.status(200).body(new ApiResponse("Expense added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Integer id, @RequestBody @Valid Expense expense) {
        expenseService.updateExpense(id, expense);
        return ResponseEntity.status(200).body(new ApiResponse("Expense updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Integer id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.status(200).body(new ApiResponse("Expense deleted successfully"));
    }

    // ----------------------------------------------------------END CRUD---------------------------------------------------------------------

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getExpensesByUserId(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(expenseService.getExpensesByUserId(userId));
    }

    @GetMapping("/category/{category}/user/{userId}")
    public ResponseEntity<?> getExpensesByCategoryAndUser(@PathVariable String category, @PathVariable Integer userId) {
        return ResponseEntity.status(200).body(expenseService.getExpensesByCategoryAndUser(category, userId));
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<?> getTotalExpensesByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(expenseService.getTotalExpensesByUserId(userId));
    }

    @GetMapping("/top-category/{userId}")
    public ResponseEntity<?> topCategory(@PathVariable Integer userId) {
        return ResponseEntity.ok(expenseService.getTopCategory(userId));
    }
    @GetMapping("/expenses/category-percentage/{userId}")
    public ResponseEntity<?> getExpenseCategoryPercentage(@PathVariable Integer userId) {
        return ResponseEntity.ok(expenseService.getExpenseCategoryPercentage(userId));
    }

}