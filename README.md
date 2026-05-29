# DropBucket

DropBucket is a basic S3-style object storage API built with Java Spring Boot. It stores object metadata in SQLite and stores uploaded file bytes on the local filesystem.

This is a single-instance learning/demo project. It does not implement S3 protocol compatibility, authentication, authorization, object versioning, or bucket management.

## Tech Stack

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- SQLite
- Docker / Docker Compose

## Storage Model

When running in Docker, DropBucket uses a persistent volume mounted at `/data`.

- SQLite database: `/data/dropbucket.sqlite`
- Object bytes: `/data/objects/{bucketName}/{uuid}`

Buckets are logical namespaces in the request path. There is no separate bucket table in v1.

## API

Base URL:

```text
http://localhost:8080/api/storage
```

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/storage/{bucketName}` | Upload a file to a bucket |
| `GET` | `/api/storage/{bucketName}/{id}` | Download a stored file |
| `GET` | `/api/storage/{bucketName}/{id}/info` | Read metadata only |
| `DELETE` | `/api/storage/{bucketName}/{id}` | Delete file bytes and metadata |

### Upload

```bash
curl -i -F "file=@notes.txt" http://localhost:8080/api/storage/docs
```

Response:

Returns a `201 Created` status with an empty body and the location of the new resource in the `Location` header.

```text
HTTP/1.1 201 Created
Location: http://localhost:8080/api/storage/docs/generated-object-uuid
Content-Length: 0
```

### Get Metadata

```bash
curl http://localhost:8080/api/storage/docs/{id}/info
```

Response:

```json
{
  "id": "generated-object-uuid",
  "bucketName": "docs",
  "originalFileName": "notes.txt",
  "contentType": "text/plain",
  "sizeBytes": 1700,
  "createdAt": "2026-05-22T22:43:54.156Z"
}
```

### Download

```bash
curl -o downloaded-notes.txt http://localhost:8080/api/storage/docs/{id}
```

### Delete

```bash
curl -X DELETE http://localhost:8080/api/storage/docs/{id}
```

Successful deletes return `204 No Content`.

## Error Responses

DropBucket returns Spring `ProblemDetail` JSON for handled errors.

| Status | Cause |
| --- | --- |
| `400 Bad Request` | Invalid bucket name, missing `file` part, or empty upload |
| `404 Not Found` | Metadata or stored file is missing |
| `500 Internal Server Error` | Unexpected storage or database failure |

## Run With Docker

Build and start the application:

```bash
docker compose up --build
```

Run in the background:

```bash
docker compose up -d --build
```

Stop the application:

```bash
docker compose down
```

Stop and remove the persisted SQLite database and stored objects:

```bash
docker compose down -v
```

## Run Tests

If Java 21 and Maven are installed locally:

```bash
mvn test
```

If you only have Docker:

```bash
docker run --rm \
  -v "$PWD:/workspace" \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-21 \
  mvn -B test
```

## Configuration

The app can be configured with environment variables.

| Variable | Default | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `DROPBUCKET_DB_PATH` | `/data/dropbucket.sqlite` | SQLite database path |
| `DROPBUCKET_OBJECT_DIR` | `/data/objects` | Directory for object bytes |
| `DROPBUCKET_MAX_FILE_SIZE` | `100MB` | Max uploaded file size |
| `DROPBUCKET_MAX_REQUEST_SIZE` | `100MB` | Max multipart request size |

## Development Notes

- Object IDs are generated server-side as UUIDs.
- Bucket names must be path-safe and cannot contain `..`.
- Delete removes the file from disk first, then deletes the SQLite metadata row.
- Download responses set `Content-Type` from metadata and use the original file name in `Content-Disposition`.
