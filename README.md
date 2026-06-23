# Farm Direct - Farmer-to-Customer Web Application

## Overview
Farm Direct is a full-stack web application that connects farmers directly with customers, eliminating intermediaries and enabling transparent agricultural commerce. The platform allows farmers to manage products and orders while customers can browse products, place orders, and communicate with farmers.

## Features

### Customer Features
- User registration and login
- Browse agricultural products
- Shopping cart management
- Place orders
- Track order status
- Chat with farmers

### Farmer Features
- Farmer registration and login
- Product management (Add, Update, Delete)
- Order management
- Farmer profile management
- Community participation
- Chat with customers

### Community Features
- Farmer discussion forum
- Knowledge sharing
- Community engagement

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- REST APIs
- Maven

### Frontend
- HTML5
- CSS3
- JavaScript

### Database
- MySQL

## Architecture

The project follows a layered MVC architecture:

Frontend (HTML/CSS/JS)
        |
Controller Layer
        |
Service Layer
        |
Repository Layer (JPA)
        |
MySQL Database

## Authentication and Authorization

- Login-based authentication
- User credentials verified against MySQL database
- User information stored in localStorage after successful login
- Role-based access control:
  - FARMER
  - CUSTOMER
- Dynamic UI rendering based on user role

## Database Configuration

Configured in application.properties:

- Database: farmtohome_v2
- MySQL Driver: com.mysql.cj.jdbc.Driver
- JPA Hibernate ddl-auto=update

## Running Locally

### Prerequisites
- Java 17
- Maven
- MySQL

### Steps

1. Create MySQL database:
   CREATE DATABASE farmtohome_v2;

2. Update database credentials in application.properties

3. Run the application:
   mvn spring-boot:run

4. Open:
   http://localhost:8080

## Build

Create executable JAR:

mvn clean package

Run JAR:

java -jar target/farmtohome-0.0.1-SNAPSHOT.jar

## Deployment

The project can be deployed on Railway:

1. Push code to GitHub
2. Connect repository to Railway
3. Configure environment variables
4. Railway builds and deploys automatically

## Future Enhancements

- Spring Security integration
- JWT authentication
- Password encryption using BCrypt
- Payment gateway integration
- AI-based crop recommendations
- Mobile application support

## Project Workflow

1. User interacts with frontend.
2. Request reaches Controller layer.
3. Controller invokes Service layer.
4. Service executes business logic.
5. Repository performs database operations.
6. MySQL returns data.
7. Response is sent back to user.

