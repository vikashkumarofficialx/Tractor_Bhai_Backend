package com.tractorbhai.controller;

import com.tractorbhai.model.WorkType;
import com.tractorbhai.repository.WorkTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.tractorbhai.model.User;
import com.tractorbhai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "*")
public class WorkTypeController {

    @Autowired
    private WorkTypeRepository repository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<WorkType> getCatalog() {
        return repository.findByUser(getCurrentUser());
    }

    @PostMapping
    public WorkType addWorkType(@RequestBody WorkType workType) {
        workType.setUser(getCurrentUser());
        return repository.save(workType);
    }

    @PutMapping("/{id}")
    public WorkType updateWorkType(@PathVariable Long id, @RequestBody WorkType workType) {
        User user = getCurrentUser();
        WorkType existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Work type not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        workType.setId(id);
        workType.setUser(user);
        return repository.save(workType);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkType(@PathVariable Long id) {
        User user = getCurrentUser();
        WorkType existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Work type not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        repository.deleteById(id);
    }
}
