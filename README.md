# Fetsy E-Commerce Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-red.svg)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Maven-Project-C71A36.svg)](https://maven.apache.org/)

---

## Introduction

**Fetsy** is a robust, full-stack e-commerce solution designed to provide a seamless shopping experience for users and a powerful management interface for administrators. Built with a modern **Spring Boot** backend, it leverages **JWT-based security** and a **MySQL** database to ensure data integrity, scalability, and security.

> This project follows the DAO (Data Access Object) pattern, ensuring a clean separation between business logic and data persistence.

---

## Key Features

- **Secure Authentication**: Multi-role authentication (User/Admin) using JWT tokens.
- **Category Management**: Browse products by categories or manage them as an admin.
- **Product Discovery**: Advanced search and filtering capabilities for products.
- **Dynamic Shopping Cart**: Real-time cart updates and persistent storage.
- **Checkout & Orders**: Streamlined checkout process with order history tracking.
- **User Profiles**: Personalized user data management and preferences.

---

## Visuals & Screenshots

### Project Demo

<div align="center">
  <h3>Frontend Demo</h3>
  <img src="/demo.gif" alt="Description of Screenshot" width="800">
</div>

<div align="center">
  <h3>Checkout Confirmation</h3>
  <img src="/confirmation.png" alt="Description of Screenshot" width="800">
</div>

<div align="center">
  <h3>Home Screen</h3>
  <img src="/home.png" alt="Description of Screenshot" width="800">
</div>

<div align="center">
  <h3>Checkout</h3>
  <img src="/checkout.png" alt="Description of Screenshot" width="800">
</div>

---

## Highlights

- **Modern Architecture**: Cleanly separated layers (Controllers, DAOs, Models, Security).
- **RESTful API**: Standardized endpoints for seamless frontend integration.
- **Robust Security**: Protected routes and role-based access control.
- **Data Persistence**: Reliable MySQL integration with high-performance DAO implementations.

---

## Project Structure

```text
src
└── main
    ├── java
    │   └── org.yearup
    │       ├── EasyShopApplication.java       # Main entry point
    │       ├── configurations                 # Spring configuration classes
    │       ├── controllers                    # REST API Endpoints
    │       │   ├── AuthenticationController   # Login & Registration
    │       │   ├── CategoriesController       # Category Management
    │       │   ├── OrdersController           # Order History & Processing
    │       │   ├── ProductsController         # Product Catalog
    │       │   ├── ProfileController          # User Account Management
    │       │   └── ShoppingCartController     # Cart Logic
    │       ├── data                           # Data Access Layer
    │       │   ├── mysql                      # JDBC Implementations
    │       │   └── *Dao.java                  # DAO Interfaces
    │       ├── models                         # Entity & DTO Classes
    │       └── security                       # JWT & Security Logic
    └── resources
        ├── application.properties             # App configuration
        └── database                           # SQL scripts for DB setup
```

---

## Getting Started

### 1. Prerequisites
- **Java 17** or higher
- **Maven 3.x**
- **MySQL 8.0**

### 2. Database Setup
1. Open your MySQL Workbench or CLI.
2. Run the script found in `database/create_database_easyshop.sql` to initialize the database schema and sample data.

### 3. Configuration
Update `src/main/resources/application.properties` with your database credentials:
```properties
datasource.url=jdbc:mysql://localhost:3306/easyshop
datasource.username=YOUR_USERNAME
datasource.password=YOUR_PASSWORD
```

### 4. Running the Backend
```bash
run EasyshopApplication.java
```
The API will be available at `http://localhost:8080`.

---

## API Endpoints

### Products

`GET /products`

**Optional query params:**
- `cat` (categoryId)
- `minPrice`
- `maxPrice`
- `subCategory`

**Example:**
```bash
curl "http://localhost:8080/products?cat=1&minPrice=10&maxPrice=200"
```
Sample Response:
```bash
[
	{
		"productId": 25,
		"name": "Men's Suit",
		"price": 199.99,
		"categoryId": 2,
		"description": "Look sharp and elegant with this tailored suit.",
		"subCategory": "Dark Blue",
		"stock": 10,
		"imageUrl": "mens-suit.jpg",
		"featured": false
	},
	{
		"productId": 32,
		"name": "Men's Watch",
		"price": 149.99,
		"categoryId": 2,
		"description": "A sophisticated and elegant watch to complete your look.",
		"subCategory": "Black",
		"stock": 20,
		"imageUrl": "mens-watch.jpg",
		"featured": true
	},
	{
		"productId": 36,
		"name": "Men's Winter Coat",
		"price": 149.99,
		"categoryId": 2,
		"description": "Stay warm and fashionable during the winter season with this coat.",
		"subCategory": "Red",
		"stock": 10,
		"imageUrl": "mens-winter-coat.jpg",
		"featured": false
	},
	{
		"productId": 55,
		"name": "Women's Winter Coat",
		"price": 149.99,
		"categoryId": 2,
		"description": "Stay warm and fashionable during the winter season with this coat.",
		"subCategory": "Pink",
		"stock": 10,
		"imageUrl": "womens-winter-coat.jpg",
		"featured": false
	},
	{
		"productId": 57,
		"name": "Women's Formal Gown",
		"price": 199.99,
		"categoryId": 2,
		"description": "A stunning and glamorous gown for formal events.",
		"subCategory": "Burgundy",
		"stock": 30,
		"imageUrl": "womens-gown.jpg",
		"featured": true
	}
]
```
### Shopping Cart (logged-in users)

This controller is routed under `/cart` and uses `Principal`, meaning it depends on the authenticated user context.

- `GET /cart`: Returns the current user’s cart.
- `POST /cart/products/{productId}`: Adds product to cart. If it already exists, increments quantity.
- `PUT /cart/products/{productId}`: Updates quantity using a `ShoppingCartItem` body.

### Orders (checkout)

- `POST /orders`: Creates a new order from the current user’s cart, creates order line items, then clears the cart.

### Profile

Profile endpoints are implemented via `ProfileController` + MySQL profile DAO.

---

## Architecture Flow

```text
Client (Frontend / Postman)
-> Controller (REST endpoints, auth via Principal)
-> DAO interface (behavior contract)
-> MySql* DAO implementation (SQL queries)
-> MySQL database
<- Model objects (serialized to JSON)
<- JSON response
```

---

## Frontend Integration

### Running a frontend (important)

This repository is an API backend (Java-only repo layout: `src/`, `pom.xml`, no `package.json`).
Your frontend is expected to live elsewhere, but any client can work. Typical setup:

1. Start the backend on `http://localhost:8080`
2. In your frontend, set the API base URL to that host
3. Call endpoints like:
   - `GET /products?...`
   - `GET /cart`
   - `POST /cart/products/{productId}`
   - `PUT /cart/products/{productId}`
   - `POST /orders`

---

---

## Future Enhancements (v2.0)

- [ ] **Payment Integration**: Stripe or PayPal integration for real payments.
- [ ] **Search Optimization**: Implementation of Elasticsearch for faster product discovery.
- [ ] **Admin Dashboard**: A dedicated UI for managing sales and inventory analytics.
- [ ] **Email Notifications**: Automated order confirmation and delivery updates.
- [ ] **Mobile App**: Cross-platform mobile version using React Native.

---

## License

This project is licensed under the **MIT License**.

---