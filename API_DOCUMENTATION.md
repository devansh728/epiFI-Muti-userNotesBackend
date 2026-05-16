# API_DOCUMENTATION

## OPEN_API.JSON

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "Notes App API",
    "description": "Multi-user Notes service with sharing, full-text search, and version history",
    "version": "1.0.0"
  },
  "servers": [
    {
      "url": "http://localhost:8080",
      "description": "Generated server url"
    }
  ],
  "tags": [
    {
      "name": "Notes",
      "description": "Create, read, update, and delete notes"
    },
    {
      "name": "Meta",
      "description": "Application information"
    },
    {
      "name": "Authentication",
      "description": "User registration, login, and token management"
    },
    {
      "name": "Actuator",
      "description": "Monitor and interact",
      "externalDocs": {
        "description": "Spring Boot Actuator Web API Documentation",
        "url": "https://docs.spring.io/spring-boot/docs/current/actuator-api/html/"
      }
    },
    {
      "name": "Search",
      "description": "Full-text search on notes"
    },
    {
      "name": "Versions",
      "description": "View note version history"
    },
    {
      "name": "Sharing",
      "description": "Share notes with other users"
    }
  ],
  "paths": {
    "/notes/{id}": {
      "get": {
        "tags": [
          "Notes"
        ],
        "summary": "Get note",
        "description": "Retrieve a note by ID if accessible",
        "operationId": "getNote",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Note retrieved successfully",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          },
          "404": {
            "description": "Note not found or not accessible",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      },
      "put": {
        "tags": [
          "Notes"
        ],
        "summary": "Update note",
        "description": "Update a note if owner or has WRITE share",
        "operationId": "updateNote",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/NoteRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Note updated successfully",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          },
          "400": {
            "description": "Validation error",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          },
          "403": {
            "description": "Permission denied",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          },
          "404": {
            "description": "Note not found",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          },
          "409": {
            "description": "Concurrent modification detected",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      },
      "delete": {
        "tags": [
          "Notes"
        ],
        "summary": "Delete note",
        "description": "Delete a note (soft delete, owner only)",
        "operationId": "deleteNote",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          }
        ],
        "responses": {
          "204": {
            "description": "Note deleted successfully"
          },
          "403": {
            "description": "Permission denied"
          },
          "404": {
            "description": "Note not found"
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/register": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Register a new user",
        "description": "Create a new user account with email and password",
        "operationId": "register",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RegisterRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "201": {
            "description": "User registered successfully",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object"
                  }
                }
              }
            }
          },
          "400": {
            "description": "Validation error",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object"
                  }
                }
              }
            }
          },
          "409": {
            "description": "Email already registered",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object"
                  }
                }
              }
            }
          }
        }
      }
    },
    "/notes": {
      "get": {
        "tags": [
          "Notes"
        ],
        "summary": "List notes",
        "description": "Get all notes of the authenticated user (paginated and sortable)",
        "operationId": "listNotes",
        "parameters": [
          {
            "name": "page",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 0
            }
          },
          {
            "name": "size",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 20
            }
          },
          {
            "name": "sort",
            "in": "query",
            "required": false,
            "schema": {
              "type": "string",
              "default": "createdAt,desc"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Notes retrieved successfully",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/PageResponseNoteResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      },
      "post": {
        "tags": [
          "Notes"
        ],
        "summary": "Create note",
        "description": "Create a new note",
        "operationId": "createNote",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/NoteRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "201": {
            "description": "Note created successfully",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          },
          "400": {
            "description": "Validation error",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/notes/{noteId}/versions/{versionId}/restore": {
      "post": {
        "tags": [
          "Versions"
        ],
        "summary": "Restore note version",
        "operationId": "restoreVersion",
        "parameters": [
          {
            "name": "noteId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          },
          {
            "name": "versionId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Version restored",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/notes/{noteId}/share": {
      "post": {
        "tags": [
          "Sharing"
        ],
        "summary": "Share note",
        "description": "Share a note with another user (owner only)",
        "operationId": "shareNote",
        "parameters": [
          {
            "name": "noteId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/ShareRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Note shared successfully",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ShareResponse"
                }
              }
            }
          },
          "400": {
            "description": "Validation error or cannot share with self",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ShareResponse"
                }
              }
            }
          },
          "403": {
            "description": "Permission denied (not owner)",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ShareResponse"
                }
              }
            }
          },
          "404": {
            "description": "Note or user not found",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ShareResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/login": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Login",
        "description": "Authenticate with email and password to receive access and refresh tokens",
        "operationId": "login",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/LoginRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Login successful",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AuthResponse"
                }
              }
            }
          },
          "401": {
            "description": "Invalid credentials",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AuthResponse"
                }
              }
            }
          }
        }
      }
    },
    "/auth/refresh": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Refresh access token",
        "description": "Use refresh token to get a new access token",
        "operationId": "refresh",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RefreshTokenRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Token refreshed successfully",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AuthResponse"
                }
              }
            }
          },
          "401": {
            "description": "Invalid or expired refresh token",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AuthResponse"
                }
              }
            }
          }
        }
      }
    },
    "/auth/logout": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Logout",
        "description": "Revoke the refresh token",
        "operationId": "logout",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RefreshTokenRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "204": {
            "description": "Logout successful"
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/search": {
      "get": {
        "tags": [
          "Search"
        ],
        "summary": "Search notes",
        "description": "Full-text search on notes accessible to user",
        "operationId": "search",
        "parameters": [
          {
            "name": "q",
            "in": "query",
            "required": true,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "page",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 0
            }
          },
          {
            "name": "size",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 20
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Search results",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/PageResponseNoteResponse"
                }
              }
            }
          },
          "400": {
            "description": "Invalid search query",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/PageResponseNoteResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/notes/{noteId}/versions": {
      "get": {
        "tags": [
          "Versions"
        ],
        "summary": "Get note versions",
        "description": "Retrieve version history for a note",
        "operationId": "getVersions",
        "parameters": [
          {
            "name": "noteId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          },
          {
            "name": "page",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 0
            }
          },
          {
            "name": "size",
            "in": "query",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 20
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Version history retrieved",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/PageResponseNoteVersionResponse"
                }
              }
            }
          },
          "404": {
            "description": "Note not found or not accessible",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/PageResponseNoteVersionResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/notes/{noteId}/versions/{versionId}": {
      "get": {
        "tags": [
          "Versions"
        ],
        "summary": "Get note version",
        "description": "Retrieve a specific version of a note",
        "operationId": "getVersion",
        "parameters": [
          {
            "name": "noteId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          },
          {
            "name": "versionId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string",
              "format": "uuid"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Version retrieved",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteVersionResponse"
                }
              }
            }
          },
          "404": {
            "description": "Note or version not found",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/NoteVersionResponse"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/actuator": {
      "get": {
        "tags": [
          "Actuator"
        ],
        "summary": "Actuator root web endpoint",
        "operationId": "links",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/vnd.spring-boot.actuator.v3+json": {
                "schema": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object",
                    "additionalProperties": {
                      "$ref": "#/components/schemas/Link"
                    }
                  }
                }
              },
              "application/vnd.spring-boot.actuator.v2+json": {
                "schema": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object",
                    "additionalProperties": {
                      "$ref": "#/components/schemas/Link"
                    }
                  }
                }
              },
              "application/json": {
                "schema": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object",
                    "additionalProperties": {
                      "$ref": "#/components/schemas/Link"
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    "/actuator/info": {
      "get": {
        "tags": [
          "Actuator"
        ],
        "summary": "Actuator web endpoint 'info'",
        "operationId": "info",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/vnd.spring-boot.actuator.v3+json": {
                "schema": {
                  "type": "object"
                }
              },
              "application/vnd.spring-boot.actuator.v2+json": {
                "schema": {
                  "type": "object"
                }
              },
              "application/json": {
                "schema": {
                  "type": "object"
                }
              }
            }
          }
        }
      }
    },
    "/actuator/health": {
      "get": {
        "tags": [
          "Actuator"
        ],
        "summary": "Actuator web endpoint 'health'",
        "operationId": "health",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/vnd.spring-boot.actuator.v3+json": {
                "schema": {
                  "type": "object"
                }
              },
              "application/vnd.spring-boot.actuator.v2+json": {
                "schema": {
                  "type": "object"
                }
              },
              "application/json": {
                "schema": {
                  "type": "object"
                }
              }
            }
          }
        }
      }
    },
    "/about": {
      "get": {
        "tags": [
          "Meta"
        ],
        "summary": "About",
        "description": "Get application information and available features",
        "operationId": "about",
        "responses": {
          "200": {
            "description": "Application information",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object"
                  }
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "NoteRequest": {
        "type": "object",
        "properties": {
          "title": {
            "type": "string",
            "description": "Note title",
            "example": "My Important Note",
            "maxLength": 200,
            "minLength": 1
          },
          "content": {
            "type": "string",
            "description": "Note content (max 100,000 characters)",
            "example": "This is the note content with full-text search support.",
            "maxLength": 100000,
            "minLength": 0
          }
        },
        "required": [
          "title"
        ]
      },
      "NoteResponse": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string",
            "format": "uuid",
            "description": "Note unique identifier",
            "example": "550e8400-e29b-41d4-a716-446655440000"
          },
          "title": {
            "type": "string",
            "description": "Note title",
            "example": "My Important Note"
          },
          "content": {
            "type": "string",
            "description": "Note content",
            "example": "This is the note content with full-text search support."
          },
          "createdAt": {
            "type": "string",
            "format": "date-time",
            "description": "Note creation timestamp",
            "example": "2026-05-15T10:30:00Z"
          },
          "updatedAt": {
            "type": "string",
            "format": "date-time",
            "description": "Note last update timestamp",
            "example": "2026-05-15T14:20:00Z"
          },
          "ownerId": {
            "type": "string",
            "format": "uuid",
            "description": "Owner user ID",
            "example": "550e8400-e29b-41d4-a716-446655440000"
          },
          "ownerEmail": {
            "type": "string",
            "description": "Owner email address",
            "example": "owner@example.com"
          }
        }
      },
      "RegisterRequest": {
        "type": "object",
        "properties": {
          "email": {
            "type": "string",
            "format": "email",
            "description": "User email address",
            "example": "user@example.com",
            "maxLength": 254,
            "minLength": 0
          },
          "password": {
            "type": "string",
            "description": "Password (8-128 chars, at least one letter and one digit)",
            "example": "MySecurePassword123",
            "maxLength": 128,
            "minLength": 8,
            "pattern": "^(?=.*[a-zA-Z])(?=.*\\d).*$"
          }
        },
        "required": [
          "email",
          "password"
        ]
      },
      "ShareRequest": {
        "type": "object",
        "properties": {
          "email": {
            "type": "string",
            "format": "email",
            "description": "Email address of user to share note with",
            "example": "colleague@example.com"
          },
          "permission": {
            "type": "string",
            "description": "Share permission level: READ (read-only) or WRITE (read and edit)",
            "enum": [
              "READ",
              "WRITE"
            ],
            "example": "READ"
          }
        },
        "required": [
          "email",
          "permission"
        ]
      },
      "ShareResponse": {
        "type": "object",
        "properties": {
          "noteId": {
            "type": "string",
            "format": "uuid",
            "description": "Note unique identifier",
            "example": "550e8400-e29b-41d4-a716-446655440000"
          },
          "sharedWithUserId": {
            "type": "string",
            "format": "uuid",
            "description": "User ID that note is shared with",
            "example": "550e8400-e29b-41d4-a716-446655440001"
          },
          "sharedWithEmail": {
            "type": "string",
            "description": "Email address of user note is shared with",
            "example": "colleague@example.com"
          },
          "permission": {
            "type": "string",
            "description": "Share permission level: READ or WRITE",
            "enum": [
              "READ",
              "WRITE"
            ],
            "example": "READ"
          },
          "sharedAt": {
            "type": "string",
            "format": "date-time",
            "description": "Timestamp when share was created",
            "example": "2026-05-15T10:30:00Z"
          }
        }
      },
      "LoginRequest": {
        "type": "object",
        "properties": {
          "email": {
            "type": "string",
            "format": "email",
            "description": "User email address",
            "example": "user@example.com"
          },
          "password": {
            "type": "string",
            "description": "User password",
            "example": "MySecurePassword123"
          }
        },
        "required": [
          "email",
          "password"
        ]
      },
      "AuthResponse": {
        "type": "object",
        "properties": {
          "access_token": {
            "type": "string",
            "description": "JWT access token (15-minute TTL, stateless, HS256-signed)",
            "example": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          },
          "refresh_token": {
            "type": "string",
            "description": "Opaque refresh token (7-day TTL, SHA-256 hashed at rest). Used to get new access tokens. Rotated on refresh.",
            "example": "abc123def456..."
          },
          "expires_in": {
            "type": "integer",
            "format": "int64",
            "description": "Access token expiration time in seconds",
            "example": 900
          }
        }
      },
      "RefreshTokenRequest": {
        "type": "object",
        "properties": {
          "refresh_token": {
            "type": "string",
            "description": "Opaque refresh token obtained from login/register endpoint",
            "example": "abc123def456..."
          }
        },
        "required": [
          "refresh_token"
        ]
      },
      "PageResponseNoteResponse": {
        "type": "object",
        "properties": {
          "content": {
            "type": "array",
            "description": "List of items in this page",
            "items": {
              "$ref": "#/components/schemas/NoteResponse"
            }
          },
          "pageNumber": {
            "type": "integer",
            "format": "int32",
            "description": "Current page number (0-indexed)",
            "example": 0
          },
          "pageSize": {
            "type": "integer",
            "format": "int32",
            "description": "Number of items per page",
            "example": 20
          },
          "totalElements": {
            "type": "integer",
            "format": "int64",
            "description": "Total number of items across all pages",
            "example": 150
          },
          "totalPages": {
            "type": "integer",
            "format": "int32",
            "description": "Total number of pages",
            "example": 8
          },
          "last": {
            "type": "boolean",
            "description": "Whether this is the last page",
            "example": false
          }
        }
      },
      "NoteVersionResponse": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string",
            "format": "uuid",
            "description": "Version record unique identifier",
            "example": "550e8400-e29b-41d4-a716-446655440000"
          },
          "title": {
            "type": "string",
            "description": "Note title at time of this version",
            "example": "My Important Note"
          },
          "content": {
            "type": "string",
            "description": "Note content at time of this version",
            "example": "This is the note content as it was at version time."
          },
          "editedByUserId": {
            "type": "string",
            "format": "uuid",
            "description": "User ID that created this version",
            "example": "550e8400-e29b-41d4-a716-446655440000"
          },
          "editedByEmail": {
            "type": "string",
            "description": "Email of user that created this version",
            "example": "editor@example.com"
          },
          "editedAt": {
            "type": "string",
            "format": "date-time",
            "description": "When this version was created",
            "example": "2026-05-15T14:20:00Z"
          },
          "versionNumber": {
            "type": "integer",
            "format": "int32",
            "description": "Version number (incremental)",
            "example": 3
          },
          "noteId": {
            "type": "string",
            "format": "uuid",
            "description": "Note unique identifier",
            "example": "550e8400-e29b-41d4-a716-446655440000"
          }
        }
      },
      "PageResponseNoteVersionResponse": {
        "type": "object",
        "properties": {
          "content": {
            "type": "array",
            "description": "List of items in this page",
            "items": {
              "$ref": "#/components/schemas/NoteVersionResponse"
            }
          },
          "pageNumber": {
            "type": "integer",
            "format": "int32",
            "description": "Current page number (0-indexed)",
            "example": 0
          },
          "pageSize": {
            "type": "integer",
            "format": "int32",
            "description": "Number of items per page",
            "example": 20
          },
          "totalElements": {
            "type": "integer",
            "format": "int64",
            "description": "Total number of items across all pages",
            "example": 150
          },
          "totalPages": {
            "type": "integer",
            "format": "int32",
            "description": "Total number of pages",
            "example": 8
          },
          "last": {
            "type": "boolean",
            "description": "Whether this is the last page",
            "example": false
          }
        }
      },
      "Link": {
        "type": "object",
        "properties": {
          "href": {
            "type": "string"
          },
          "templated": {
            "type": "boolean"
          }
        }
      }
    },
    "securitySchemes": {
      "Bearer Authentication": {
        "type": "http",
        "description": "JWT token for API authentication",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      }
    }
  }
}
```