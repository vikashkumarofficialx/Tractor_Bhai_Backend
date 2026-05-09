package com.tractorbhai.controller;

import com.tractorbhai.model.Expense;
import com.tractorbhai.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.tractorbhai.model.User;
import com.tractorbhai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseRepository repository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Expense> getAllExpenses() {
        return repository.findByUser(getCurrentUser());
    }

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        expense.setUser(getCurrentUser());
        return repository.save(expense);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        User user = getCurrentUser();
        Expense existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        repository.deleteById(id);
    }
}
