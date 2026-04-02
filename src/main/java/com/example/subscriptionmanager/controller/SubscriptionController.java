package com.example.subscriptionmanager.controller;

import com.example.subscriptionmanager.model.Subscription;
import com.example.subscriptionmanager.service.CsvSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@Validated
public class SubscriptionController {

    private final CsvSubscriptionService service;

    public SubscriptionController(CsvSubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Subscription>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<Subscription> create(@Valid @RequestBody Subscription subscription) {
        subscription.setId(null); // enforce new id
        Subscription saved = service.save(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> update(@PathVariable String id, @Valid @RequestBody Subscription subscription) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        subscription.setId(id);
        Subscription saved = service.save(subscription);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        List<Subscription> all = service.findAll();
        BigDecimal totalMonthly = BigDecimal.ZERO;

        for (Subscription s : all) {
            BigDecimal cost = s.getCost();
            if ("yearly".equalsIgnoreCase(s.getBillingCycle())) {
                cost = cost.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
            }
            totalMonthly = totalMonthly.add(cost);
        }

        LocalDate now = LocalDate.now();
        List<Subscription> upcoming = all.stream()
                .filter(s -> !s.getNextBillingDate().isBefore(now))
                .sorted((a, b) -> a.getNextBillingDate().compareTo(b.getNextBillingDate()))
                .limit(5)
                .toList();

        Map<String, Long> byCategory = all.stream().collect(java.util.stream.Collectors.groupingBy(Subscription::getCategory, java.util.stream.Collectors.counting()));
        Map<String, Long> byStatus = all.stream().collect(java.util.stream.Collectors.groupingBy(Subscription::getStatus, java.util.stream.Collectors.counting()));

        Map<String, Object> response = new HashMap<>();
        response.put("totalMonthly", totalMonthly);
        response.put("upcoming", upcoming);
        response.put("byCategory", byCategory);
        response.put("byStatus", byStatus);
        response.put("totalCount", all.size());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath().toString();
            errors.put(path, v.getMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

}
