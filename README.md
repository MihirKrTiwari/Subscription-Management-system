# Subscription Management System

A simple web-based subscription management system built with Java backend and HTML/CSS/JavaScript frontend.

## Features

- Display subscription plans (Basic, Standard, Premium)
- Subscribe to a plan
- Upgrade/downgrade subscription
- Cancel subscription
- View current subscription on dashboard
- Data stored in CSV file (no SQL database required)

## Tech Stack

- **Frontend**: HTML, CSS, JavaScript
- **Backend**: Java (Simple HTTP server using `com.sun.net.httpserver`)
- **Database**: CSV file
- **Build Tool**: Maven

## Project Structure

```
subscription-oops/
├── backend/
│   ├── Main.java
│   ├── User.java
│   ├── Subscription.java
│   ├── Plan.java
│   ├── CSVHandler.java
│   └── data.csv
├── frontend/
│   ├── index.html
│   ├── style.css
│   └── script.js
├── pom.xml
└── README.md
```

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher

## Setup and Installation

1. **Clone or download the project**

2. **Navigate to the project directory**
   ```bash
   cd subscription-oops
   ```

3. **Install dependencies and build the project**
   ```bash
   mvn clean install
   ```

4. **Run the backend server**
   ```bash
   mvn exec:java
   ```
   Alternatively, you can run:
   ```bash
   java -cp target/subscription-oops-1.0-SNAPSHOT.jar com.subscription.Main
   ```

5. **Open the frontend**
   Open `frontend/index.html` in your web browser, or serve it using a simple HTTP server:
   ```bash
   # Using Python 3
   cd frontend
   python3 -m http.server 8080
   ```
   Then visit `http://localhost:8080` in your browser.

## API Endpoints

The backend runs on `http://localhost:8080` and provides the following endpoints:

- `GET /plans` - Get all available plans
- `POST /subscribe` - Subscribe a user to a plan
- `POST /update` - Update/change a user's subscription (upgrade/downgrade)
- `POST /cancel` - Cancel a user's subscription
- `GET /user/{userId}` - Get user details and current subscription

## Usage

1. Start the backend server as described above
2. Open the frontend in your browser
3. Navigate to the Home page to see available plans
4. Go to the Manage Subscription page to subscribe, upgrade/downgrade, or cancel
5. Visit the Dashboard to view your current subscription

## Notes

- The system uses a fixed user ID (`user1`) for demonstration purposes in the frontend. In a real application, you would implement proper authentication.
- Subscription durations are simplified to monthly periods for all plans.
- When a subscription is cancelled, the user retains access until the end date of their current billing period.
- Data is persisted in `backend/data.csv`.

## Troubleshooting

- If you encounter port conflicts, change the port number in `Main.java`
- Ensure that the backend and frontend are running on different ports or enable CORS if needed
- Check the console for error messages if something doesn't work as expected

## License

This project is for educational purposes.