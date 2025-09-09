# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Skyapp is an Android application built with Java and Gradle, focusing on location-based services with Google Maps integration. The app appears to be designed for logistics/delivery tracking with features for routes, checkpoints, and shipments.

**Key Technologies:**
- Android SDK (compileSdk 35, minSdk 30, targetSdk 34)
- Google Maps API and Location Services
- Retrofit for HTTP networking
- OkHttp for HTTP client
- View Binding enabled
- Material Design components

## Common Build Commands

```bash
# Build the project (debug)
./gradlew assembleDebug

# Build and install debug APK
./gradlew installDebug

# Clean build
./gradlew clean

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Check for lint issues
./gradlew lint

# Generate signed release build
./gradlew assembleRelease
```

## Project Structure

**Core Directories:**
- `app/src/main/java/com/example/skyapp/` - Main application code
  - `ui/` - Activity classes (MainActivity, MapsActivity, ProfileActivity, etc.)
  - `api_config/` - HTTP client configuration, authentication interceptors
  - `bo/` - Business objects/data models
- `app/src/main/res/` - Android resources (layouts, drawables, strings)
- `app/src/main/AndroidManifest.xml` - App permissions and component declarations

**Key Activities:**
- `MainActivity` - Main entry point
- `MapsActivity` - Google Maps integration
- `ProfileActivity`, `RouteActivity`, `CheckpointActivity`, `ShipmentsActivity` - Feature-specific screens

## Configuration Notes

**Google Maps Setup:**
- Requires `MAPS_API_KEY` in `local.properties`
- API key configured in AndroidManifest.xml via `${MAPS_API_KEY}` placeholder

**Permissions:**
- Internet access
- Network state access
- Fine and coarse location permissions
- GPS hardware requirement

**Network Configuration:**
- Uses Retrofit with Gson converter
- Custom authentication interceptor implemented
- API endpoints configured in `api_config/` package

## Development Environment

The project uses Gradle Kotlin DSL (`.gradle.kts` files) and follows Android development best practices with proper dependency management through version catalogs (`gradle/libs.versions.toml`).

## Design System

**Login Screen (MainActivity):**
- Modern Material Design 3 implementation
- Gradient background with card-based layout
- Smooth entrance animations (logo slides down, card slides up)
- Interactive feedback animations (button clicks, loading states, error shakes)
- Material TextInputLayout with floating labels and password toggle
- Social login buttons for Google and Microsoft with proper branding
- Responsive design that works on different screen sizes

**Modern UI Components:**
- MaterialCardView with rounded corners and elevation
- Material3 color system with proper contrast ratios
- Custom drawables for buttons, inputs, and backgrounds
- Ripple effects on interactive elements
- Success/error state animations