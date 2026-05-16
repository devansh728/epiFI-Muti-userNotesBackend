# Notes App Backend

A production-ready multi-user notes service built with Spring Boot 3.5.0, Java 21, and PostgreSQL.

## Features

- ✅ Multi-user authentication with JWT (access + refresh tokens)
- ✅ Note sharing with granular permissions (READ/WRITE)
- ✅ Full-text search with relevance ranking
- ✅ Note version history (snapshots on every edit)
- ✅ Soft deletion with permanent deletion capability
- ✅ Rate limiting (5 req/min for auth, 100 req/min for APIs)
- ✅ Caffeine in-memory caching
- ✅ OpenAPI 3.1 documentation with Swagger UI
- ✅ RFC 7807 Problem Details for standard error responses
- ✅ Optimistic locking for concurrent edits

## Technology Stack

- **Framework**: Spring Boot 3.0.5
- **Language**: Java 21 (with virtual threads)
- **Database**: PostgreSQL 16 (with Flyway migrations)
- **Security**: Spring Security 7, JWT (jjwt 0.12), BCrypt (strength 12)
- **Caching**: Caffeine
- **Rate Limiting**: Bucket4j
- **API Docs**: springdoc-openapi 2.6+
- **Mapping**: MapStruct
- **Testing**: JUnit 5, Testcontainers, RestAssured, Postman
- **Deployment**: Docker, Docker Compose, Render.com
- **Development Tool**: VS Code, Github Copilot(Claude, GPT4)

## Api_doc_link

- BASE_URL=http://localhost:8080 / render-deployed-domain

- open-api.json : {BASE_URL}/v3/api-docs
- swagger-ui.html : {BASE_URL}/swagger-ui.html
- about : {BASE_URL}/about

## Smart Feature

### There is two smart feature, which i developed:

[Click here to see the Documentation](SMART_FEATURE.md)

## API_DOCUMENTATION

[Click here to see the Documentation](API_DOCUMENTATION.md)

## Database Schema Design

[Click here to see the Documentation](DATABASE_DESIGN.md)


## System Architecture

[Click here to see the Documentation](SYSTEM_DESIGN.md)

