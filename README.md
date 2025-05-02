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
cd ios-user-management-service
git checkout dev
```

### 2. Set Up Environment Variables

Refer to the provided **`env.example`** file in the root directory to configure all necessary environment variables.
Rename the file to `.env` and populate the required values: