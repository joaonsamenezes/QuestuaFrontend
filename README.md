<h1 align="center">Questua - Mobile</h1>

<p align="center">
  Gamified language learning app built with modern Android architecture
</p>

---

## Overview

Questua is a mobile application that delivers a gamified language learning experience through quests, interactive dialogues and progression systems.

Users explore structured learning paths, interact with characters, and track their evolution using a dynamic progression model.

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

The application follows a layered architecture inspired by Clean Architecture principles.

- **Presentation Layer (Compose)**  
  Responsible for UI rendering and state handling  

- **Domain Layer**  
  Contains core business models and domain logic  

- **Data Layer**  
  Handles repositories, API communication and data sources  

- **Core Layer**  
  Provides shared modules, utilities and common configurations  

### Key Characteristics

- Clear separation of concerns  
- Unidirectional data flow  
- Dependency Injection with Hilt  
- Scalable and maintainable structure  

---

## Project Structure

- `presentation/` → Jetpack Compose screens and UI state handling  
- `domain/` → core business models and domain entities  
- `data/` → repositories, API services and data sources  
- `core/` → shared modules, utilities and common configurations  

---

## Core Features

### Quest System
- Navigation through cities and quest points  
- Structured progression model  

### Dialogue Engine
- Interactive dialogue flow  
- Support for user input and branching paths  

### Progression System
- XP accumulation  
- Levels and streak tracking  
- Quest completion metrics  

### Gamification
- Achievements and rewards  
- Unlockable content based on progression  

---

## API Integration

- Consumes REST endpoints from Questua Backend  
- Handles user progression and dynamic content  
- Structured communication between layers  

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
4. Run on emulator or physical device  

---

## Project Context

This project focuses on:

- Modern Android development with Jetpack Compose  
- Scalable architecture (MVVM + DI)  
- State management and reactive UI  
- Integration with complex backend systems  

---

## License

Educational project.
