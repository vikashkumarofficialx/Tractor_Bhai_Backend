package com.tractorbhai.controller;

import com.tractorbhai.model.Tractor;
import com.tractorbhai.model.RentalEntry;
import com.tractorbhai.model.Expense;
import com.tractorbhai.repository.TractorRepository;
import com.tractorbhai.repository.RentalEntryRepository;
import com.tractorbhai.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tractorbhai.model.User;
import com.tractorbhai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/tractors")
@CrossOrigin(origins = "*")
public class TractorController {

    @Autowired
    private TractorRepository repository;

    @Autowired
    private RentalEntryRepository entryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/{id}/details")
    public Map<String, Object> getTractorDetails(@PathVariable Long id) {
        User user = getCurrentUser();
        Tractor tractor = repository.findById(id).orElse(null);
        Map<String, Object> details = new HashMap<>();
        
        if (tractor != null && tractor.getUser() != null && tractor.getUser().getId().equals(user.getId())) {
            String name = tractor.getName();
            List<RentalEntry> income = entryRepository.findByUser(user).stream()
                .filter(e -> name.equalsIgnoreCase(e.getTractorName()))
                .collect(Collectors.toList());
            
            List<Expense> costs = expenseRepository.findByUser(user).stream()
                .filter(e -> name.equalsIgnoreCase(e.getTractorName()))
                .collect(Collectors.toList());

            details.put("tractor", tractor);
            details.put("income", income);
            details.put("expenses", costs);
            details.put("totalIncome", income.stream().mapToDouble(e -> e.getTotalAmount() != null ? e.getTotalAmount() : 0.0).sum());
            details.put("totalExpense", costs.stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0).sum());
        }
        
        return details;
    }

    @GetMapping
    public List<Tractor> getAllTractors() {
        return repository.findByUser(getCurrentUser());
    }

    @PostMapping
    public Tractor addTractor(@RequestBody Tractor tractor) {
        tractor.setUser(getCurrentUser());
        return repository.save(tractor);
    }

    @PutMapping("/{id}")
    public Tractor updateTractor(@PathVariable Long id, @RequestBody Tractor tractor) {
        User user = getCurrentUser();
        Tractor existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Tractor not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        tractor.setId(id);
        tractor.setUser(user);
        return repository.save(tractor);
    }

    @DeleteMapping("/{id}")
    public void deleteTractor(@PathVariable Long id) {
        User user = getCurrentUser();
        Tractor existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Tractor not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        repository.deleteById(id);
    }
}
