package com.tractorbhai.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
public class RentalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Transient
    private String customerName; // For incoming JSON compatibility
    @Transient
    private String mobileNumber; // For incoming JSON compatibility
    @Transient
    private String village; // For incoming JSON compatibility
    private String tractorName;
    private String workType; // Rotavator, Trolley, etc.

    private Double hoursWorked;
    private Double acresWorked;
    private Double rate;
    private Double totalAmount;
    private Double discount = 0.0;
    private Double amountPaid = 0.0;
    private Double balanceDue = 0.0;

    private String paymentStatus; // PAID, PENDING, PARTIAL

    private LocalDateTime entryDate;
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Virtual getters for frontend compatibility
    public String getCustomerName() {
        return customer != null ? customer.getName() : customerName;
    }

    public String getMobileNumber() {
        return customer != null ? customer.getMobileNumber() : mobileNumber;
    }

    public String getVillage() {
        return customer != null ? customer.getVillage() : village;
    }
}
