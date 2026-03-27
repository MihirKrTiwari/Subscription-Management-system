package com.subscription;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles reading from and writing to the CSV file.
 * CSV format: userId, name, email, currentPlan, startDate, endDate
 */
public class CSVHandler {
    private static final String CSV_FILE = "data.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Reads all user records from the CSV file.
     * @return a list of user records as string arrays (each inner array is a CSV line split by commas)
     */
    public List<String[]> readAllRecords() {
        List<String[]> records = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(CSV_FILE))) {
                // Create an empty file if it doesn't exist
                Files.createFile(Paths.get(CSV_FILE));
                return records;
            }
            BufferedReader reader = Files.newBufferedReader(Paths.get(CSV_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                // Ensure we have exactly 6 fields; if not, skip or handle error
                if (parts.length == 6) {
                    records.add(parts);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        return records;
    }

    /**
     * Writes all user records to the CSV file, overwriting existing content.
     * @param records list of string arrays, each array representing a CSV line
     */
    public void writeAllRecords(List<String[]> records) {
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            for (String[] record : records) {
                writer.write(String.join(",", record));
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    /**
     * Finds a user record by userId.
     * @param userId the user ID to search for
     * @return the record as a string array, or null if not found
     */
    public String[] findRecordByUserId(String userId) {
        List<String[]> records = readAllRecords();
        for (String[] record : records) {
            if (record[0].equals(userId)) {
                return record;
            }
        }
        return null;
    }

    /**
     * Updates a user record in the CSV.
     * @param userId the user ID of the record to update
     * @param updatedRecord the updated record as a string array (6 fields)
     * @return true if the record was found and updated, false otherwise
     */
    public boolean updateRecord(String userId, String[] updatedRecord) {
        List<String[]> records = readAllRecords();
        boolean found = false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i)[0].equals(userId)) {
                records.set(i, updatedRecord);
                found = true;
                break;
            }
        }
        if (found) {
            writeAllRecords(records);
        }
        return found;
    }

    /**
     * Adds a new user record to the CSV.
     * @param record the new record as a string array (6 fields)
     */
    public void addRecord(String[] record) {
        List<String[]> records = readAllRecords();
        records.add(record);
        writeAllRecords(records);
    }

    /**
     * Deletes a user record by userId.
     * @param userId the user ID of the record to delete
     * @return true if the record was found and deleted, false otherwise
     */
    public boolean deleteRecord(String userId) {
        List<String[]> records = readAllRecords();
        boolean removed = false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i)[0].equals(userId)) {
                records.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            writeAllRecords(records);
        }
        return removed;
    }

    /**
     * Converts a User and Subscription object to a CSV record array.
     * @param user the User object
     * @param subscription the Subscription object
     * @return a string array with 6 fields: userId, name, email, plan, startDate, endDate
     */
    public static String[] toCsvRecord(User user, Subscription subscription) {
        // Ensure the userId matches
        if (!user.getUserId().equals(subscription.getUserId())) {
            throw new IllegalArgumentException("User and Subscription userId must match");
        }
        String[] record = new String[6];
        record[0] = user.getUserId();
        record[1] = user.getName();
        record[2] = user.getEmail();
        record[3] = subscription.getPlan();
        record[4] = subscription.getStartDate().format(DATE_FORMATTER);
        record[5] = subscription.getEndDate().format(DATE_FORMATTER);
        return record;
    }

    /**
     * Converts a CSV record array to a User and Subscription object.
     * @param record a string array with 6 fields: userId, name, email, plan, startDate, endDate
     * @return an array where index 0 is the User object and index 1 is the Subscription object
     */
    public static Object[] fromCsvRecord(String[] record) {
        if (record.length != 6) {
            throw new IllegalArgumentException("Invalid CSV record: expected 6 fields");
        }
        User user = new User(record[0], record[1], record[2]);
        Subscription subscription = new Subscription(
                record[0],
                record[3],
                LocalDate.parse(record[4], DATE_FORMATTER),
                LocalDate.parse(record[5], DATE_FORMATTER)
        );
        return new Object[]{user, subscription};
    }
}