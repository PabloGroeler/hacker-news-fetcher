# Hacker News Fetcher

A Spring Boot application that fetches and stores Hacker News stories using the Algolia API.

## Prerequisites

- Java 21
- Maven
- MongoDB
- Docker
- 
## Getting Started

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/hacker-news-fetcher.git
   cd hacker-news-fetcher
   ```

2. **Run the application with Docker Compose**
   docker-compose up --build -d
   
   The application will be available at `http://localhost:8080`

### Authentication

1. **Get JWT Token**
   - Open Swagger UI at: http://localhost:8080/swagger-ui.html
   - Find the `POST /login` endpoint under Authentication
   - Click "Try it out"
   - Enter the following credentials:
     "username": "admin",
     "password": "admin"
   - Click "Execute" to get your Bearer token

2. **Using the Token in Swagger**
   - After logging in, copy the `token` from the response
   - Click the "Authorize" button (lock icon) at the top of the page
   - In the dialog put the token
   - Click "Authorize" and then "Close"
   - You can now access all protected endpoints under Articles

### API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
  - Requires authentication as described above
