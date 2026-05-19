[README.md](https://github.com/user-attachments/files/28004156/README.md)
# HISBA & NISBAH (Qisma Plus) — حسبة ونسبة

> Smart Group Expense & Contribution Management System

A Spring Boot REST API that helps groups manage shared financial contributions, track personal expenses, and analyze financial behavior — with real-time WhatsApp and Email notifications.

## Overview
> HISBA & NISBAH is a backend system that helps users:
- Manage shared group contributions
- Track personal expenses
- Control financial risk
- Monitor payment behavior (streaks & commitment)
- Receive real-time notifications via Email & WhatsApp

---

## Tech Stack

| Layer         | Technology                  |
| ------------- | --------------------------- |
| Backend       | Java 17                     |
| Framework     | Spring Boot 3               |
| Database      | MySQL 8                     |
| ORM           | Spring Data JPA (Hibernate) |
| Notifications | Twilio WhatsApp + JavaMail  |
| Validation    | Jakarta Bean Validation     |
| Utilities     | Lombok                      |

---

## Project Structure

```
src/main/java/com/example/qismaplus/
├── Controller/
│   ├── UserController.java
│   ├── GroupController.java
│   ├── ContributionController.java
│   ├── PaymentController.java
│   └── ExpenseController.java
├── Service/
│   ├── UserService.java
│   ├── GroupService.java
│   ├── ContributionService.java
│   ├── PaymentService.java
│   └── ExpenseService.java
├── Model/
│   ├── User.java
│   ├── Group.java
│   ├── Contribution.java
│   ├── Payment.java
│   └── Expense.java
├── Repository/
├── External/
│   ├── NotificationService.java
│   ├── WhatsAppService.java
│   └── EmailService.java
└── API/
    ├── ApiException.java
    └── ApiResponse.java
```

---

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8+
- Maven
- Twilio account
- Gmail account with App Password enabled

### Setup

**1. Clone Project**
```bash
git clone https://github.com/maysun9/Capstone-2.git
cd Capstone-2
```

**2. Create the database**
```sql
CREATE DATABASE Hisbah_Nisbah;
```

**3. Configure `application.properties`**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/Hisbah_Nisbah
spring.datasource.username=
spring.datasource.password=

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

twilio.account.sid=${ACCOUNT_SID}
twilio.auth.token=${AUTH_TOKEN}
twilio.whatsapp.number=+14155238886
```

**4. Set environment variables** (IntelliJ → Run → Edit Configurations → Environment Variables)
```
EMAIL_USERNAME=name@gmail.com
EMAIL_PASSWORD=key_app_password
ACCOUNT_SID=app_twilio_sid
AUTH_TOKEN=key_twilio_token
```

**5. Run the application**
```bash
mvn spring-boot:run
```

Base URL: `http://localhost:8080/api/v1`

---

## API Endpoints

### Users `/api/v1/user`

| Method | Endpoint                       | Description             |
|--------|--------------------------------|-------------------------|
| GET    | `/get`                         | Get all users           |
| POST   | `/add`                         | Register new user       |
| PUT    | `/update/{id}`                 | Update user             |
| DELETE | `/delete/{id}`                 | Delete user             |
| GET    | `/{id}/dashboard`              | User dashboard summary  |
| GET    | `/risk/{id}`                   | Financial risk analysis |
| GET    | `/users/{id}/smart-saving-tip` | Personalized saving tip |

### Groups `/api/v1/group`

| Method | Endpoint                                             | Description |
|--------|------------------------------------------------------|--------------------------------------|
| GET    | `/get`                                               | Get all groups                       |
| POST   | `/create/{userId}`                                   | Create group (creator becomes admin) |
| PUT    | `/update/{adminId}/{groupId}`                        | Update group                         |
| DELETE | `/delete/{adminId}/{groupId}`                        | Delete group                         |
| POST   | `/add-member/{adminId}/{groupId}/{memberId}`         | Add member with budget check         |
| DELETE | `/remove-member/{adminId}/{groupId}/{memberId}`      | Remove member                        |   
| GET    | `/admin/{adminId}`                                   | Get groups by admin                  |
| GET    | `/{groupId}/preview/{userId}`                        | Preview group before joining         |

### Contributions `/api/v1/contribution`

| Method | Endpoint           | Description                                  |
|--------|--------------------|----------------------------------------------|
| GET    | `/get`             | Get all contributions                        |
| POST   | `/add`             | Create contribution + auto-generate payments |
| PUT    | `/update/{id}`     | Update contribution                          |
| DELETE | `/delete/{id}`     | Delete contribution                          |
| GET    | `/group/{groupId}` | Get contributions by group                   |
| PUT    | `/complete/{id}`   | Mark contribution as completed               |

### Payments `/api/v1/payment`

| Method | Endpoint                                 | Description                                 |
|--------|------------------------------------------|---------------------------------------------|
| GET    | `/get-all`                               | Get all payments                            |
| DELETE | `/delete/{id}`                           | Delete payment                              |
| POST   | `/make/{userId}/{paymentId}`             | Make payment (+1% late penalty if overdue)  |
| POST   | `/pay-for-member/{payerId}/{paymentId}`  | Pay on behalf of another member             |
| PUT    | `/verify/{adminId}/{paymentId}`          | Admin verifies payment → sends notification |
| GET    | `/user/{userId}`                         | Get payments by user                        |
| GET    | `/unpaid/{userId}`                       | Get unpaid payments                         |
| GET    | `/contribution/{contributionId}`         | Get payments by contribution                |
| GET    | `/commitment/{userId}/{contributionId}`  | Commitment rate                             |
| GET    | `/late-users/{groupId}`                  | Late payments in group                      |
| GET    | `/summary/{userId}`                      | Payment summary                             |
| GET    | `/payment-streak/{userId}`               | Consecutive payment streak                  |

### Expenses `/api/v1/expense`

| Method | Endpoint                                 | Description               |
|--------|------------------------------------------|---------------------------|
| GET    | `/get`                                   | Get all expenses          |
| POST   | `/add`                                   | Add expense               |
| PUT    |`/update/{id}`                            | Update expense            |
| DELETE | `/delete/{id}`                           | Delete expense            |
| GET    | `/user/{userId}`                         | Get expenses by user      |
| GET    | `/category/{category}/user/{userId}`     | Filter by category        |
| GET    | `/total/{userId}`                        | Total expenses            |
| GET    | `/top-category/{userId}`                 | Highest spending category |
| GET    | `/expenses/category-percentage/{userId}` | Spending breakdown by %   |

---

## Business Logic

### Financial Risk Score
```
Risk = (Expenses + Debt) / Monthly Income

< 70%   → Low Risk
70-100% → Medium Risk
≥ 100%  → High Risk
```

### Late Penalty
- If payment is delayed > 2 days
- 1% penalty added automatically

### Budget Protection
- Member cannot be added if:
- Budget < required contribution
- Contribution > 30% of income

### Payment Streak
- Counts consecutive months of successful payments.

### Payment Flow 
- User pays → PENDING → Admin verifies → PAID
---

## Notification Events

All notifications are sent via **WhatsApp (Twilio) + Email (SMTP)** simultaneously.

| Event            | Sent To          |
| ---------------- | ---------------- |
| Payment Pending  | User             |
| Payment Verified | User             |
| Group Payment    | Payer + Receiver |
---

## Recommended Flow

```
1. POST /user/add                              → Register all users
2. POST /group/create/{userId}                 → Create group (creator = admin)
3. POST /group/add-member                      → Add members (budget checked)
4. POST /contribution/add                      → Create contribution (payments auto-generated)
5. GET  /payment/user/{id}                     → View generated payments
6. POST /payment/make/{userId}/{paymentId}     → User pays
7. PUT  /payment/verify/{adminId}/{paymentId}  → Admin verifies + notification sent
8. GET  /user/risk/{id}                        → Check financial risk
```

---

## Author

Developed by **Maysun Alharbi**  
Backend Development · Spring 2026
