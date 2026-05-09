package com.tractorbhai.controller;

import com.tractorbhai.model.RentalEntry;
import com.tractorbhai.model.Customer;
import com.tractorbhai.repository.RentalEntryRepository;
import com.tractorbhai.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.tractorbhai.model.User;
import com.tractorbhai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/entries")
@CrossOrigin(origins = "*") // For development
public class EntryController {

    @Autowired
    private RentalEntryRepository repository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<RentalEntry> getAllEntries() {
        return repository.findByUser(getCurrentUser());
    }

    @PostMapping
    public RentalEntry createEntry(@RequestBody RentalEntry entry) {
        User user = getCurrentUser();
        entry.setUser(user);

        if (entry.getTotalAmount() == null && entry.getRate() != null) {
            double qty = entry.getHoursWorked() != null ? entry.getHoursWorked()
                    : entry.getAcresWorked() != null ? entry.getAcresWorked() : 0;
            entry.setTotalAmount(qty * entry.getRate());
        }

        // Find or Create Customer
        String name = entry.getCustomerName();
        String mobile = entry.getMobileNumber();
        String village = entry.getVillage();

        if (name != null && !name.isEmpty()) {
            Customer customer = customerRepository.findByUser(user).stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name)
                            || (mobile != null && mobile.equals(c.getMobileNumber())))
                    .findFirst()
                    .orElse(null);

            if (customer == null) {
                customer = new Customer();
                customer.setName(name);
                customer.setMobileNumber(mobile);
                customer.setVillage(village);
                customer.setUser(user);
                customer = customerRepository.save(customer);
            }
            entry.setCustomer(customer);
        }

        return repository.save(entry);
    }

    @PutMapping("/{id}/settle")
    public RentalEntry settlePayment(@PathVariable Long id, @RequestParam Double paymentAmount) {
        User user = getCurrentUser();
        RentalEntry entry = repository.findById(id).orElseThrow(() -> new RuntimeException("Entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        double currentPaid = entry.getAmountPaid() != null ? entry.getAmountPaid() : 0.0;
        double total = entry.getTotalAmount() != null ? entry.getTotalAmount() : 0.0;

        double newPaid = currentPaid + paymentAmount;
        entry.setAmountPaid(newPaid);
        entry.setBalanceDue(Math.max(0, total - newPaid));

        if (entry.getBalanceDue() <= 0) {
            entry.setPaymentStatus("PAID");
        } else {
            entry.setPaymentStatus("PARTIAL");
        }

        return repository.save(entry);
    }
}
