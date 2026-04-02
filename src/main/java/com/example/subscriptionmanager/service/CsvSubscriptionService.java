package com.example.subscriptionmanager.service;

import com.example.subscriptionmanager.model.Subscription;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class CsvSubscriptionService {

    private final File storeFile;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CsvSubscriptionService(@Value("${subscription.csv.path:subscriptions.csv}") String path) {
        storeFile = new File(path);
    }

    @PostConstruct
    public void init() {
        try {
            if (!storeFile.exists()) {
                storeFile.createNewFile();
                writeAll(Collections.emptyList());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize subscription storage", e);
        }
    }

    public List<Subscription> findAll() {
        lock.readLock().lock();
        try {
            if (!storeFile.exists() || storeFile.length() == 0) {
                return new ArrayList<>();
            }
            try (CSVReader csvReader = new CSVReader(new FileReader(storeFile))) {
                List<String[]> rows = csvReader.readAll();
                List<Subscription> subs = new ArrayList<>();
                for (String[] row : rows) {
                    if (row.length < 8) continue;
                    Subscription s = new Subscription();
                    s.setId(row[0]);
                    s.setName(row[1]);
                    s.setCategory(row[2]);
                    s.setCost(new BigDecimal(row[3]));
                    s.setBillingCycle(row[4]);
                    s.setNextBillingDate(LocalDate.parse(row[5]));
                    s.setNotes(row[6]);
                    s.setStatus(row[7]);
                    subs.add(s);
                }
                return subs;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read subscriptions", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Subscription save(Subscription subscription) {
        lock.writeLock().lock();
        try {
            List<Subscription> existing = findAll();
            optionalEnsureId(subscription);
            existing.removeIf(s -> s.getId().equals(subscription.getId()));
            existing.add(subscription);
            writeAll(existing);
            return subscription;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Subscription> findById(String id) {
        return findAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            List<Subscription> existing = findAll();
            boolean removed = existing.removeIf(s -> s.getId().equals(id));
            if (!removed) {
                throw new IllegalArgumentException("Subscription not found: " + id);
            }
            writeAll(existing);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void optionalEnsureId(Subscription subscription) {
        if (subscription.getId() == null || subscription.getId().isBlank()) {
            subscription.setId(java.util.UUID.randomUUID().toString());
        }
    }

    private void writeAll(List<Subscription> subs) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(storeFile, false))) {
            for (Subscription s : subs) {
                writer.writeNext(new String[]{
                        s.getId(),
                        s.getName(),
                        s.getCategory(),
                        s.getCost().toPlainString(),
                        s.getBillingCycle(),
                        s.getNextBillingDate().toString(),
                        s.getNotes() == null ? "" : s.getNotes(),
                        s.getStatus()
                });
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to write subscriptions", e);
        }
    }
}
