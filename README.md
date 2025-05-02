Bank Transfer Service
A Spring Boot application that simulates money transfer operations between bank accounts with transaction tracking, fee calculation, and commission processing.
Implementation Details
This application implements:

REST Endpoints:

POST /api/v1/transfers - Process money transfers between accounts
GET /api/v1/transactions - Retrieve transaction history with filters (status, accountNumber, date range)
GET /api/v1/transactions/summary/{date} - Get daily transaction summary


Scheduled Jobs:

Commission calculation (runs daily at 01:00 AM) - Updates successful transactions as commission-worthy and calculates commission (20% of transaction fee)
Daily summary generation (runs daily at 02:00 AM) - Creates a summary of the previous day's transactions


Transaction Processing:

Validates source and destination accounts
Processes transfer amounts
Calculates transaction fee (0.5% of amount, capped at 100)
Sets appropriate status (SUCCESSFUL, INSUFFICIENT_FUND, FAILED)
Stores complete transaction records



Technology Stack

Java 17
Spring Boot 3.4.x
Spring Data JPA
MySQL
Maven
Docker
JUnit 5 & Mockito

Running the Application
Prerequisites

Java 17
Maven 3.6 or higher
Docker (optional, for containerization)

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/Legacy721/Bank_Transfer_Service.git
git checkout dev
```

### 2. Set Up Environment Variables

Refer to the provided **`env.example`** file in the root directory to configure all necessary environment variables.
Rename the file to `.env` and populate the required values:
Also change the spring profiles active from dev to prod

### 3. Setting up account numbers
```sql
INSERT INTO accounts (id, account_number, account_name, balance, created_at, updated_at) VALUES
(1, '1000000001', 'John Doe', 1500.75, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '1000000002', 'Jane Smith', 2450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '1000000003', 'Michael Brown', 302.25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '1000000004', 'Emily Davis', 9800.10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '1000000005', 'Chris Wilson', 74.35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '1000000006', 'Laura Johnson', 5120.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '1000000007', 'Daniel Miller', 230.90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, '1000000008', 'Sophia Moore', 845.60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, '1000000009', 'James Anderson', 1930.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, '1000000010', 'Olivia Taylor', 110.75, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


```
### 4. Run the application

```bash
mvn clean install
```
```bash
mvn spring-boot:run

```
### 5. Resources
Load the resources via the swagger url {base_url}/swagger-ui/index.html
