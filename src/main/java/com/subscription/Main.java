package com.subscription;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class that sets up the HTTP server and handles API requests.
 */
public class Main {
    // Predefined plans array (requirement: use arrays to store predefined plans)
    public static final Plan[] PLANS = {
            new Plan("Basic", 9.99, "monthly", "Limited access, 1 user"),
            new Plan("Standard", 19.99, "monthly", "Standard access, 2 users"),
            new Plan("Premium", 29.99, "monthly", "Premium access, 4 users")
    };

    // CSV handler instance
    private static final CSVHandler csvHandler = new CSVHandler();

    // Simple in-memory cache for user sessions (for demonstration)
    // In a real app, you'd use proper sessions or tokens
    private static final Map<String, String> userSessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/plans", new PlansHandler());
        server.createContext("/subscribe", new SubscribeHandler());
        server.createContext("/update", new UpdateHandler());
        server.createContext("/cancel", new CancelHandler());
        server.createContext("/user", new UserHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Subscription server started on port 8080");
    }

    /**
     * Handler for GET /plans - returns all available plans.
     */
    static class PlansHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                JSONArray plansArray = new JSONArray();
                for (Plan plan : PLANS) {
                    JSONObject planObj = new JSONObject();
                    planObj.put("name", plan.getName());
                    planObj.put("price", plan.getPrice());
                    planObj.put("duration", plan.getDuration());
                    planObj.put("features", plan.getFeatures());
                    plansArray.put(planObj);
                }
                String response = plansArray.toString();
                sendResponse(exchange, response, 200);
            } else {
                sendResponse(exchange, "Method not allowed", 405);
            }
        }
    }

    /**
     * Handler for POST /subscribe - subscribes a user to a plan.
     * Expects JSON: { "userId": "...", "name": "...", "email": "...", "planName": "..." }
     */
    static class SubscribeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                JSONObject data = new JSONObject(body);
                String userId = data.optString("userId");
                String name = data.optString("name");
                String email = data.optString("email");
                String planName = data.optString("planName");

                // Validation using control statements (if-else)
                if (userId.isEmpty() || name.isEmpty() || email.isEmpty() || planName.isEmpty()) {
                    sendResponse(exchange, "Missing required fields", 400);
                    return;
                }

                // Find the plan using control statements (switch)
                Plan selectedPlan = null;
                for (Plan plan : PLANS) {
                    if (plan.getName().equalsIgnoreCase(planName)) {
                        selectedPlan = plan;
                        break;
                    }
                }
                if (selectedPlan == null) {
                    sendResponse(exchange, "Invalid plan selected", 400);
                    return;
                }

                // Check if user already exists
                String[] existingRecord = csvHandler.findRecordByUserId(userId);
                if (existingRecord != null) {
                    sendResponse(exchange, "User already exists", 409); // Conflict
                    return;
                }

                // Create user and subscription
                User user = new User(userId, name, email);
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusMonths(1); // Assuming monthly duration for simplicity
                Subscription subscription = new Subscription(userId, selectedPlan.getName(), startDate, endDate);

                // Save to CSV
                String[] record = CSVHandler.toCsvRecord(user, subscription);
                csvHandler.addRecord(record);

                // Create response
                JSONObject responseJson = new JSONObject();
                responseJson.put("message", "Subscription successful");
                responseJson.put("userId", userId);
                responseJson.put("plan", selectedPlan.getName());
                responseJson.put("startDate", startDate);
                responseJson.put("endDate", endDate);

                sendResponse(exchange, responseJson.toString(), 201);
            } else {
                sendResponse(exchange, "Method not allowed", 405);
            }
        }
    }

    /**
     * Handler for POST /update - updates a user's subscription (upgrade/downgrade).
     * Expects JSON: { "userId": "...", "newPlanName": "..." }
     */
    static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                JSONObject data = new JSONObject(body);
                String userId = data.optString("userId");
                String newPlanName = data.optString("newPlanName");

                if (userId.isEmpty() || newPlanName.isEmpty()) {
                    sendResponse(exchange, "Missing required fields", 400);
                    return;
                }

                // Find the new plan
                Plan newPlan = null;
                for (Plan plan : PLANS) {
                    if (plan.getName().equalsIgnoreCase(newPlanName)) {
                        newPlan = plan;
                        break;
                    }
                }
                if (newPlan == null) {
                    sendResponse(exchange, "Invalid plan selected", 400);
                    return;
                }

                // Find existing record
                String[] existingRecord = csvHandler.findRecordByUserId(userId);
                if (existingRecord == null) {
                    sendResponse(exchange, "User not found", 404);
                    return;
                }

                // Parse existing record to get user details and current subscription
                Object[] objArray = CSVHandler.fromCsvRecord(existingRecord);
                User user = (User) objArray[0];
                Subscription subscription = (Subscription) objArray[1];

                // Update subscription with new plan and reset dates (simple approach)
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusMonths(1); // Assuming monthly
                subscription.setPlan(newPlan.getName());
                subscription.setStartDate(startDate);
                subscription.setEndDate(endDate);

                // Update CSV
                String[] updatedRecord = CSVHandler.toCsvRecord(user, subscription);
                boolean updated = csvHandler.updateRecord(userId, updatedRecord);
                if (!updated) {
                    sendResponse(exchange, "Failed to update subscription", 500);
                    return;
                }

                // Create response
                JSONObject responseJson = new JSONObject();
                responseJson.put("message", "Subscription updated successfully");
                responseJson.put("userId", userId);
                responseJson.put("newPlan", newPlan.getName());
                responseJson.put("startDate", startDate);
                responseJson.put("endDate", endDate);

                sendResponse(exchange, responseJson.toString(), 200);
            } else {
                sendResponse(exchange, "Method not allowed", 405);
            }
        }
    }

    /**
     * Handler for POST /cancel - cancels a user's subscription.
     * Expects JSON: { "userId": "..." }
     */
    static class CancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                JSONObject data = new JSONObject(body);
                String userId = data.optString("userId");

                if (userId.isEmpty()) {
                    sendResponse(exchange, "Missing userId", 400);
                    return;
                }

                // Find existing record
                String[] existingRecord = csvHandler.findRecordByUserId(userId);
                if (existingRecord == null) {
                    sendResponse(exchange, "User not found", 404);
                    return;
                }

                // Parse existing record
                Object[] objArray = CSVHandler.fromCsvRecord(existingRecord);
                User user = (User) objArray[0];
                Subscription subscription = (Subscription) objArray[1];

                // Set plan to "Cancelled" and set endDate to today (or keep original endDate?)
                // We'll set the plan to "Cancelled" and keep the endDate as is (user has access until endDate)
                subscription.setPlan("Cancelled");

                // Update CSV
                String[] updatedRecord = CSVHandler.toCsvRecord(user, subscription);
                boolean updated = csvHandler.updateRecord(userId, updatedRecord);
                if (!updated) {
                    sendResponse(exchange, "Failed to cancel subscription", 500);
                    return;
                }

                // Create response
                JSONObject responseJson = new JSONObject();
                responseJson.put("message", "Subscription cancelled successfully");
                responseJson.put("userId", userId);
                responseJson.put("plan", "Cancelled");
                // Note: user still has access until the original endDate

                sendResponse(exchange, responseJson.toString(), 200);
            } else {
                sendResponse(exchange, "Method not allowed", 405);
            }
        }
    }

    /**
     * Handler for GET /user/{userId} - gets user details.
     */
    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().getPath();
                // Expected format: /user/{userId}
                String[] parts = path.split("/");
                String userId = (parts.length > 2) ? parts[2] : "";

                if (userId.isEmpty()) {
                    sendResponse(exchange, "Missing userId", 400);
                    return;
                }

                // Find record
                String[] record = csvHandler.findRecordByUserId(userId);
                if (record == null) {
                    sendResponse(exchange, "User not found", 404);
                    return;
                }

                // Parse record
                Object[] objArray = CSVHandler.fromCsvRecord(record);
                User user = (User) objArray[0];
                Subscription subscription = (Subscription) objArray[1];

                // Create response
                JSONObject responseJson = new JSONObject();
                responseJson.put("userId", user.getUserId());
                responseJson.put("name", user.getName());
                responseJson.put("email", user.getEmail());
                responseJson.put("currentPlan", subscription.getPlan());
                responseJson.put("startDate", subscription.getStartDate());
                responseJson.put("endDate", subscription.getEndDate());

                sendResponse(exchange, responseJson.toString(), 200);
            } else {
                sendResponse(exchange, "Method not allowed", 405);
            }
        }
    }

    /**
     * Utility method to read the request body.
     */
    private static String readBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    /**
     * Utility method to send a response.
     */
    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}