
## Overview

**chat-api-tests** is a **black-box test suite** designed to validate a Chat REST API built with Spring Boot. It interacts with a **running application over HTTP**, verifying behavior from an external perspective rather than inspecting internal code.

---

## Requirements
Docker compose up from the chat directory must be run before running the tests!

## Purpose

The project ensures that the API:

* Works correctly across all major endpoints
* Enforces **authentication and role-based authorization**
* Handles both **valid and invalid inputs** properly
* Maintains consistent **HTTP status codes and response structures**

---

## Tech Stack

* **TestNG** – test execution and suite management
* **REST Assured** – HTTP requests and response validation
* **Jackson** – JSON serialization/deserialization
* **AssertJ** – fluent assertions
* **Lombok** – DTO simplification

---

## Scope of Testing

The suite includes **tests** covering:

* **Authentication** (login scenarios)
* **Users** (full CRUD + permissions)
* **Attachments** (CRUD + validation)
* **Chat Rooms** (CRUD + roles)
* **Personal Chats** (restricted access scenarios)
* **Smoke tests** (basic system validation without local backend)

---

## Key Characteristics

* **Black-box approach** – tests only public API endpoints
* **Role-based testing** – verifies behavior for (group) VIEWER, USER, ADMIN, AUDITOR, and anonymous users
* **Full CRUD coverage** – create, read, update, delete operations across resources
* **Error handling validation** – checks 401, 403, 404, 422 scenarios
* **Sequential execution** – controlled order via TestNG suite

---

## How It Works

* Tests run against a **live Chat application instance**
* Authentication is handled via **JWT tokens**
* Predefined users (seeded at startup) are used for role-based scenarios
* In general configurable base URL allows testing across environments (local, staging, etc.)

---

## Bottom Line

This project is a **comprehensive API verification suite** that simulates real client interactions, ensuring the Chat backend 
is **secure, reliable, and behaves correctly across all roles and cases**.
