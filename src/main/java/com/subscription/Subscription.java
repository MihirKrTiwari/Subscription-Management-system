package com.subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Subscription class representing a user's subscription.
 */
public class Subscription {
    private String userId;
    private String plan;
    private LocalDate startDate;
    private LocalDate endDate;

    // Plan details: we'll define the plans as constants or use an enum, but for simplicity we'll use a method to get plan details.
    // Alternatively, we can have a Plan class, but the requirement is to use arrays for predefined plans in Java.
    // We'll handle the plans in the Main or CSVHandler.

    // Constructors
    public Subscription() {
    }

    public Subscription(String userId, String plan, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.plan = plan;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the subscription details as a CSV line.
     * Format: userId,plan,startDate,endDate
     */
    public String toCsvLine() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s,%s,%s,%s",
                userId,
                plan,
                startDate.format(formatter),
                endDate.format(formatter));
    }

    /**
     * Creates a Subscription object from a CSV line.
     * Expected format: userId,plan,startDate,endDate
     */
    public static Subscription fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            return null;
        }
        String userId = parts[0];
        String plan = parts[1];
        LocalDate startDate = LocalDate.parse(parts[2]);
        LocalDate endDate = LocalDate.parse(parts[3]);
        return new Subscription(userId, plan, startDate, endDate);
    }

    @Override
    public String toString() {
        return "Subscription [userId=" + userId + ", plan=" + plan + ", startDate=" + startDate + ", endDate=" + endDate + "]";
    }
}