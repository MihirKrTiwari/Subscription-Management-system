package com.subscription;

/**
 * Plan class representing a subscription plan.
 */
public class Plan {
    private String name;
    private double price;
    private String duration;
    private String features;

    // Constructors
    public Plan() {
    }

    public Plan(String name, double price, String duration, String features) {
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.features = features;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    @Override
    public String toString() {
        return "Plan [name=" + name + ", price=" + price + ", duration=" + duration + ", features=" + features + "]";
    }
}