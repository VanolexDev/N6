# n6 URL Shortener

A lightweight URL shortener service built with Kotlin and Ktor, featuring QR code generation and rate limiting.

## Overview

n6 is a simple, high-performance URL shortener that:
- Shortens long URLs into 4-character base64 codes.
- Automatically generates QR codes for shortened links.
- Provides a web interface for easy link creation.
- Includes basic rate limiting to prevent abuse.
- Uses MariaDB/MySQL for persistent storage.

## Requirements

- **Java JDK 21** or higher.
- **MariaDB** or **MySQL** database.
- **Gradle** (included via Gradle Wrapper).

## Setup

### 1. Database Configuration

Create a database and a table named `Links`. You can use the following SQL schema:

```sql
CREATE DATABASE n6;

USE n6;

CREATE TABLE Links (
    id INT AUTO_INCREMENT PRIMARY KEY,
    link VARCHAR(512) NOT NULL,
    ip VARCHAR(45) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Application Configuration

The application uses Typesafe Config. By default, it looks for `application.conf` in the same directory as the JAR file. If not found, it falls back to the internal resources.

Example `application.conf`:
```hocon
ktorHost = "0.0.0.0"
ktorPort = 8080
dbHost = "localhost"
dbDatabase = "n6"
dbUsername = "your_username"
dbPassword = "your_password"
prime = 983449
inverse = 3326633
salt = 8455662
```

*Note: Ensure you create this file in the same directory as the JAR.*

## Running the Application

### Using Gradle

To run the application in development mode:
```powershell
./gradlew run
```

To build a fat JAR (Shadow JAR):
```powershell
./gradlew shadowJar
```
The resulting JAR will be located in `build/libs/`.

To run the JAR:
```powershell
java -jar build/libs/n6-1.0-SNAPSHOT-all.jar
```

## Project Structure

```text
.
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── net/vanolex/
│       │       ├── Config.kt       # Configuration data class
│       │       ├── Database.kt     # Database interaction logic
│       │       ├── Knuth.kt        # ID obfuscation (bijective mapping)
│       │       ├── Main.kt         # Entry point and Routing
│       │       ├── QrGenerator.kt  # QR code generation using ZXing
│       │       └── Utils.kt        # Utility functions and rate limiting
│       └── resources/
│           ├── assets/             # Static web assets (HTML, CSS, JS, SVGs)
│           └── application.conf    # Main configuration file
├── build.gradle.kts        # Gradle build script
└── settings.gradle.kts     # Gradle settings
```

## API Endpoints

- `GET /` - Serves the web interface.
- `POST /api/genurl` - Generates a shortened URL.
    - **Body**: Plain text URL.
    - **Returns**: Shortened code.
- `GET /{code}` - Redirects to the original URL.
- `GET /qr/{code}.png` - Returns a QR code image for the given shortened code.

## Scripts

- `./gradlew run`: Starts the Ktor server.
- `./gradlew build`: Compiles and builds the project.
- `./gradlew test`: Runs unit tests (if any).
- `./gradlew shadowJar`: Creates a standalone executable JAR.

## TODOs

- [ ] Add comprehensive unit and integration tests.
- [ ] Implement a more robust rate limiting system (e.g., Redis-based).
- [ ] Add URL validation for reachable hosts.
- [ ] Improve error handling and logging.

## License

MIT License
