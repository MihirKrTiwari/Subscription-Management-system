package com.example.subscriptionmanager.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Subscription {

    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be positive")
    private BigDecimal cost;

    @NotBlank(message = "Billing cycle is required")
    private String billingCycle;

    @NotNull(message = "Next billing date is required")
    private LocalDate nextBillingDate;

    private String notes;

    @NotBlank(message = "Status is required")
    private String status;

    public Subscription() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getCost() {
        return cost;
    }
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getBillingCycle() {
        return billingCycle;
    }
    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }
    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
