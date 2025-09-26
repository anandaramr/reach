# Getting Started

## Prerequisites
- Android Studio
- JDK 17+
- Gradle 8+

## Installation
1. Clone the repository
```bash
    git clone https://github.com/anandaramr/reach.git
```
2. Open in [Android Studio](https://developer.android.com/studio)
3. Build and run on emulator or device (preferably on a device)

# Project Structure
All Android source folders for this project are located in `app/src/main/java/com/project/reach`
```
.
├── core/               # Core utilities and dependency injection
│   └── di/             # Hilt modules and DI setup
├── data/               # Data layer
│   ├── repository/     # Repository interfaces and implementations
│   ├── local/          # Local data sources
│   └── network/        # Network data sources, API clients
├── domain/             # Business logic and use cases
├── network/            # Network-related utilities and models
└── ui/                 # Presentation layer
    ├── theme/          # App-wide UI themes and styles
    ├── screens/        # Feature screens
    ├── navigation/     # Navigation destinations and setup
    ├── components/     # Reusable UI components
    └── app/            # App-level UI setup (e.g., main activity, root composable)
```