package com.tractorbhai.controller;

import com.tractorbhai.model.RentalEntry;
import com.tractorbhai.model.Expense;
import com.tractorbhai.repository.RentalEntryRepository;
import com.tractorbhai.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tractorbhai.model.User;
import com.tractorbhai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private RentalEntryRepository entryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats(@RequestParam(required = false, defaultValue = "this_month") String filter) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> stats = new HashMap<>();
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startDate;
        java.time.LocalDateTime endDate = now;
        
        if ("today".equals(filter)) {
            startDate = now.withHour(0).withMinute(0).withSecond(0);
        } else if ("yesterday".equals(filter)) {
            startDate = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
            endDate = now.minusDays(1).withHour(23).withMinute(59).withSecond(59);
        } else if ("this_week".equals(filter)) {
            startDate = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
        } else if ("this_month".equals(filter)) {
            startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        } else if ("last_month".equals(filter)) {
            startDate = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            endDate = now.withDayOfMonth(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
        } else if ("this_year".equals(filter)) {
            startDate = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        } else {
            startDate = java.time.LocalDateTime.of(2000, 1, 1, 0, 0); // All time
        }

        List<RentalEntry> entries;
        List<Expense> expenses;

        if ("all_time".equals(filter)) {
            entries = entryRepository.findByUser(user);
            expenses = expenseRepository.findByUser(user);
        } else {
            entries = entryRepository.findByEntryDateBetweenAndUser(startDate, endDate, user);
            expenses = expenseRepository.findByExpenseDateBetweenAndUser(startDate, endDate, user);
        }

        double totalRevenue = entries.stream().mapToDouble(e -> e.getTotalAmount() != null ? e.getTotalAmount() : 0.0).sum();
        double pendingDues = entries.stream().mapToDouble(e -> e.getBalanceDue() != null ? e.getBalanceDue() : 0.0).sum();
        
        double dieselExpense = expenses.stream().filter(e -> "DIESEL".equalsIgnoreCase(e.getExpenseType())).mapToDouble(e -> e.getAmount()).sum();
        double repairExpense = expenses.stream().filter(e -> "REPAIR".equalsIgnoreCase(e.getExpenseType())).mapToDouble(e -> e.getAmount()).sum();
        double emiExpense = expenses.stream().filter(e -> "EMI".equalsIgnoreCase(e.getExpenseType())).mapToDouble(e -> e.getAmount()).sum();
        double driverExpense = expenses.stream().filter(e -> "DRIVER".equalsIgnoreCase(e.getExpenseType())).mapToDouble(e -> e.getAmount()).sum();
        double totalExpenses = expenses.stream().mapToDouble(e -> e.getAmount()).sum();

        stats.put("totalProfit", totalRevenue - totalExpenses);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalExpenses", totalExpenses);
        stats.put("pendingDues", pendingDues);
        stats.put("dieselExpense", dieselExpense);
        stats.put("repairExpense", repairExpense);
        stats.put("emiExpense", emiExpense);
        stats.put("driverExpense", driverExpense);
        
        return stats;
    }
}

