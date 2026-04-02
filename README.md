# Machine Learning Based Authentication Anomaly Detection System

This prototype system monitors user authentication activities and detects suspicious login behavior using machine learning (Isolation Forest). 

## Components
1. **Database**: MySQL schema (`db/schema.sql`). Stores users, login logs, and alerts.
2. **Machine Learning Module**: Python + FastAPI (`ml/`). Trains an anomaly detection model and serves predictions over HTTP.
3. **Backend API**: Spring Boot (`backend/`). Exposes REST endpoints, logs login attempts, calls the ML API, and persists data to MySQL.
4. **Monitoring Dashboard**: JavaFX (`dashboard/`). Desktop UI that fetches logs and alerts from the backend every 10 seconds.

## How to Run

### 1. Database
- Start your local MySQL server.
- Run the SQL script located in `db/schema.sql`.

### 2. Machine Learning Module
- Open a terminal in the `ml/` directory.
- Run `pip install -r requirements.txt`.
- Train the model: `python train_model.py`.
- Start the API Server: `python app.py`. (Runs on port 8000).

### 3. Spring Boot Backend
- Open a terminal in the `backend/` directory.
- Update `src/main/resources/application.properties` with your MySQL credentials (currently `root`/`root`).
- Run using Maven: `mvn spring-boot:run`. (Runs on port 8080).

### 4. JavaFX Dashboard
- Open a terminal in the `dashboard/` directory.
- Run using Maven: `mvn clean javafx:run`. 

### 5. Testing the Flow
With the ML Server, Backend, and Dashboard all running:
- You can simulate logins by sending POST requests to the backend:
```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\", \"ipAddress\":\"192.168.1.5\", \"deviceInfo\":\"iPhone\"}"
```
- You can also test the raw ML prediction endpoint:
```bash
python ml/test_app.py
```
- The dashboard will fetch the events every 10 seconds and automatically display normal vs. anomalous logins.
