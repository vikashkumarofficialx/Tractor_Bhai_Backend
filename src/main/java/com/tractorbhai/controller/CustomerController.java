package com.tractorbhai.controller;

import com.tractorbhai.model.Customer;
import com.tractorbhai.model.RentalEntry;
import com.tractorbhai.repository.CustomerRepository;
import com.tractorbhai.repository.RentalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.tractorbhai.model.User;
import com.tractorbhai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private RentalEntryRepository entryRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        User user = getCurrentUser();
        List<Customer> customers = repository.findByUser(user);
        List<RentalEntry> entries = entryRepository.findByUser(user);
        
        for (Customer customer : customers) {
            double totalDue = entries.stream()
                .filter(e -> e.getCustomer() != null && e.getCustomer().getId().equals(customer.getId()))
                .mapToDouble(e -> e.getBalanceDue() != null ? e.getBalanceDue() : 0.0)
                .sum();
            customer.setPendingDues(totalDue);
        }
        
        return customers;
    }

    @PostMapping
    public Customer addCustomer(@RequestBody Customer customer) {
        customer.setUser(getCurrentUser());
        return repository.save(customer);
    }

    @GetMapping("/{id}/details")
    public java.util.Map<String, Object> getCustomerDetails(@PathVariable Long id) {
        User user = getCurrentUser();
        Customer customer = repository.findById(id).orElse(null);
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        
        if (customer != null && customer.getUser() != null && customer.getUser().getId().equals(user.getId())) {
            List<RentalEntry> entries = entryRepository.findByUser(user).stream()
                .filter(e -> e.getCustomer() != null && e.getCustomer().getId().equals(id))
                .collect(java.util.stream.Collectors.toList());
            
            double totalDue = entries.stream()
                .mapToDouble(e -> e.getBalanceDue() != null ? e.getBalanceDue() : 0.0)
                .sum();
            
            customer.setPendingDues(totalDue);
            details.put("customer", customer);
            details.put("entries", entries);
        }
        return details;
    }

    @PostMapping("/{id}/pay")
    public java.util.Map<String, Object> payCustomerDues(@PathVariable Long id, @RequestParam Double paymentAmount) {
        User user = getCurrentUser();
        Customer customer = repository.findById(id).orElse(null);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (customer != null && customer.getUser() != null && customer.getUser().getId().equals(user.getId()) && paymentAmount != null && paymentAmount > 0) {
            List<RentalEntry> entries = entryRepository.findByUser(user).stream()
                .filter(e -> e.getCustomer() != null && e.getCustomer().getId().equals(id))
                .filter(e -> e.getBalanceDue() != null && e.getBalanceDue() > 0)
                .sorted((e1, e2) -> {
                    if (e1.getEntryDate() == null) return 1;
                    if (e2.getEntryDate() == null) return -1;
                    return e1.getEntryDate().compareTo(e2.getEntryDate());
                })
                .collect(java.util.stream.Collectors.toList());
            
            double remainingPayment = paymentAmount;
            
            for (RentalEntry entry : entries) {
                if (remainingPayment <= 0) break;
                
                double currentDue = entry.getBalanceDue() != null ? entry.getBalanceDue() : 0.0;
                double currentPaid = entry.getAmountPaid() != null ? entry.getAmountPaid() : 0.0;
                double total = entry.getTotalAmount() != null ? entry.getTotalAmount() : 0.0;
                
                double paymentToApply = Math.min(currentDue, remainingPayment);
                
                double newPaid = currentPaid + paymentToApply;
                entry.setAmountPaid(newPaid);
                entry.setBalanceDue(Math.max(0, total - newPaid));
                
                if (entry.getBalanceDue() <= 0) {
                    entry.setPaymentStatus("PAID");
                } else {
                    entry.setPaymentStatus("PARTIAL");
                }
                
                entryRepository.save(entry);
                remainingPayment -= paymentToApply;
            }
            
            response.put("success", true);
            response.put("message", "Payment applied successfully");
        } else {
            response.put("success", false);
            response.put("message", "Invalid customer or payment amount");
        }
        return response;
    }
}
