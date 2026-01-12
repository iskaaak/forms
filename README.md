# Forms Application

A full-stack application for building and distributing dynamic forms. This project consists of a **Quarkus** backend and a **Nuxt 3** frontend.

> **Note:** This is a demo project. It was fully created with **Google Antigravity**, **Gemini 3 Pro (High)** in **Fast conversation mode**.

## 🏗 Project Structure

The repository is organized into two main directories:

- **`rest/`**: The backend API service built with **Quarkus** (Kotlin, Hibernate Panache, H2, JWT).
- **`front/`**: The frontend web application built with **Nuxt 3** and **PrimeVue**.

## 🚀 Getting Started

### Prerequisites

- **Java JDK 21+** (for the backend)
- **Node.js** (v18+ recommended) and **pnpm** (for the frontend)

### 1. Running the Backend

The backend handles user authentication, form management, and persistence using an H2 database.

1.  Navigate to the `rest` directory:
    ```bash
    cd rest
    ```
2.  Start the Quarkus development server:
    ```bash
    ./gradlew quarkusDev
    ```
    The API will be available at `http://localhost:8080`.
    *   **Dev UI:** `http://localhost:8080/q/dev/`
    *   **Swagger UI:** `http://localhost:8080/q/swagger-ui/`

### 2. Running the Frontend

The frontend provides the user interface for authenticating, building forms, and viewing them.

1.  Navigate to the `front` directory:
    ```bash
    cd front
    ```
2.  Install dependencies:
    ```bash
    pnpm install
    ```
3.  Start the development server:
    ```bash
    pnpm dev
    ```
    The application will be accessible at `http://localhost:3000`.

## ✨ Features

- **User Authentication:** Secure login and registration using JWT.
- **Form Builder:** Intuitive interface to create forms with various section types.
- **Data Persistence:** Forms and user data are stored in a persistent H2 database.
- **Responsive Design:** Built with PrimeVue for a modern, responsive UI.

## 🛠 Tech Stack

**Backend:**
- [Quarkus](https://quarkus.io/) (Kotlin)
- Hibernate ORM with Panache
- H2 Database
- SmallRye JWT & OpenAPI

**Frontend:**
- [Nuxt 3](https://nuxt.com/) (Vue.js)
- [PrimeVue](https://primevue.org/)
- TypeScript

## 📄 License

[Unlicense](UNLICENSE)
