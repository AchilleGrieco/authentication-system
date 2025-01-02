# Authentication System

A robust authentication system built with Spring Boot that provides both local authentication with email verification and Google OAuth 2.0 integration.

## Features

- **Local Authentication**
  - User registration with email verification
  - Secure password hashing
  - JWT-based authentication
  - Token refresh mechanism

- **Google OAuth 2.0**
  - Seamless Google sign-in integration
  - Automatic user profile creation
  - Secure token handling

- **Security Features**
  - Password encryption
  - JWT token-based session management
  - Protection against common security vulnerabilities

## Technologies

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Tokens)
- Google OAuth 2.0 API
- Maven

## Prerequisites

- JDK 17 or later
- Maven 3.6+
- PostgreSQL database
- Google Cloud Console account (for OAuth credentials)

## Setup & Configuration

1. **Clone the repository**
   ```bash
   git clone https://github.com/AchilleGrieco/oauth-login.git
   cd oauth-login
   ```

2. **Database Configuration**
   - Create a PostgreSQL database
   - Update `application.properties` with your database credentials:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:your_port/your_database
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

3. **Google OAuth Setup**
   - Go to the Google Cloud Console
   - Create a new project
   - Enable the OAuth 2.0 API
   - Create OAuth 2.0 credentials (Client ID and Client Secret)
   - Add authorized redirect URIs
   - Update `application.properties` with your OAuth credentials:
     ```properties
     spring.security.oauth2.client.registration.google.client-id=your_client_id
     spring.security.oauth2.client.registration.google.client-secret=your_client_secret
     ```

4. **Email Configuration**
   - Configure your email service in `application.properties`:
     ```properties
     spring.mail.host=smtp.gmail.com
     spring.mail.port=587
     spring.mail.username=your_email@gmail.com
     spring.mail.password=your_app_password
     ```

## Running the Application

1. **Build the project**
   ```bash
   mvn clean install
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/login` - Login with email and password
- `POST /api/auth/refresh-token` - Refresh access token
- `GET /api/auth/verify-email` - Verify email address

### OAuth
- `GET /oauth2/authorization/google` - Initiate Google OAuth2 login
- `GET /oauth2/callback/google` - Google OAuth2 callback URL

## Security Considerations

- All passwords are encrypted using BCrypt
- JWT tokens are signed and have an expiration time
- Sensitive data is not exposed in responses

## Index.html file
The index.html serves only testing pourposes.
Don't use it for real implementation.

## License

This project is licensed under the MIT License.
