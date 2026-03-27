<h1 align="center">Questua - Mobile</h1>

<p align="center">
  Gamified language learning app built with modern Android architecture
</p>

---

## Overview

Questua is a mobile application that provides a gamified language learning experience through quests, interactive dialogues and progression systems.

The app allows users to explore structured learning paths, interact with characters, and track their evolution using a dynamic progression model.

---

## Tech Stack

- Kotlin
- Jetpack Compose
- Android SDK
- Hilt (Dependency Injection)
- MVVM Architecture
- REST API integration

---

## Architecture

The application is built using **MVVM (Model-View-ViewModel)** with clear separation of concerns:

- **UI Layer (Compose)**  
  Declarative UI built with Jetpack Compose

- **ViewModel Layer**  
  Handles UI state, business logic orchestration and lifecycle awareness

- **Domain / Data Layer**  
  Responsible for API communication and data handling

- **Dependency Injection**  
  Managed with Hilt for scalability and testability

---

## Core Features

### Quest System
- Navigation through cities and quest points  
- Structured learning progression  

### Dialogue Engine
- Interactive dialogue flow  
- Support for user choices and responses  
- Dynamic progression between scenes  

### Progression System
- XP accumulation  
- Levels and streak tracking  
- Quest completion metrics  

### Gamification
- Achievement system  
- Reward mechanics  
- Progress-based unlockables  

---

## API Integration

- Consumes REST endpoints from Questua Backend  
- Handles user state, progression and content dynamically  
- Structured data flow between layers  

---

## Project Structure

- `ui/` → Compose screens and components  
- `viewmodel/` → state and UI logic  
- `data/` → repositories and API layer  
- `di/` → dependency injection modules  
- `model/` → domain models  

---

## Getting Started

### Requirements

- Android Studio
- Android SDK
- Kotlin

### Setup

1. Clone the repository  
2. Open in Android Studio  
3. Sync Gradle  
4. Run the app on emulator or device  

---

## Project Context

This project focuses on:

- Modern Android development (Compose + MVVM)  
- Scalable architecture patterns  
- State management and reactive UI  
- Integration with complex backend systems  

---

## License

Educational project.
