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

### 3. Run the application

```bash
mvn clean install
```
Build your docker image with the command below:
```bash
docker build -t bank-transfer-service .
```
After successfully generating your docker image, start your application by running the command below:
```bash
docker compose up --build
```
If your application has successfully started, then you're on track.
Run the command below to deploy to a kubernetes cluster. But before then, make sure you stop your application
Before running the command below, make sure you're in the root directory of your application. Run them one after another.
```bash
cd kubernetes/prod
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml 
```
After running the commands above, Run the command below.
```bash
kubectl get all
```
Run this command to expose the service outside of the kubernetes cluster.
```bash
kubectl port-forward svc/gs-transfer-service-k8s 9090:80
```
### 4. Resources
Load the resources via the swagger url {base_url}/swagger-ui/index.html
