package com.tractorbhai.repository;

import com.tractorbhai.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tractorbhai.model.User;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);
    List<Expense> findByExpenseDateBetweenAndUser(LocalDateTime start, LocalDateTime end, User user);
}
