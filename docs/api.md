# HTTP API v0.1

Base path: `/api`. JSON request/response bodies; enum values are uppercase. Timestamps are ISO-8601 UTC. Java is available through the frontend proxy at `http://localhost:4200/api` in Compose.

## Endpoints

| Method | Path | Result |
| --- | --- | --- |
| POST | `/requests` | 201 + response DTO + `Location` header |
| GET | `/requests?status=NEW&search=printer&page=0&size=10` | 200 paginated result |
| GET | `/requests/{uuid}` | 200 response DTO, or 404 |
| PATCH | `/requests/{uuid}/status` | 200 updated DTO, or 404/409 |

### Create

```json
{"title":"Reception printer offline","description":"The printer stopped responding this morning.","priority":"HIGH"}
```

Title and description must have non-whitespace text, with limits of 120 and 4,000 characters respectively. They are trimmed before persistence. Priority is required: `LOW`, `MEDIUM`, or `HIGH`. The server assigns `NEW`, UUID, and timestamps.

### Response

```json
{
  "id": "3b18d64d-90e8-4c78-b744-f4d4e98ee80d",
  "title": "Reception printer offline",
  "description": "The printer stopped responding this morning.",
  "priority": "HIGH",
  "status": "NEW",
  "createdAt": "2026-10-06T12:00:00Z",
  "updatedAt": "2026-10-06T12:00:00Z"
}
```

Examples are illustrative, not seeded database records.

### List

Optional `status`: `NEW`, `IN_PROGRESS`, `RESOLVED`. Optional `search`: case-insensitive literal title substring, maximum 120 characters. `page`: zero-based, minimum 0; default 0. `size`: 1–100; default 10. Sort is creation time descending, then UUID descending.

```json
{"content":[],"page":0,"size":10,"totalElements":0,"totalPages":0}
```

### Change status

```json
{"status":"IN_PROGRESS"}
```

All transitions are currently accepted, including reopening. Repeating the current status is a no-op and preserves `updatedAt`.

## Error contract

Errors use Spring Problem Details (`application/problem+json`). Invalid bodies, unsupported enum values, malformed UUIDs, or invalid pagination return 400. Missing requests return 404. Concurrent database updates return 409 with guidance to refresh. Field-by-field validation messages are a future UX improvement.

## PowerShell example

```powershell
$apiBase = 'http://localhost:4200/api/requests'
$payload = @{ title = 'Printer offline'; description = 'Reception printer does not respond'; priority = 'HIGH' } | ConvertTo-Json
$createdRequest = Invoke-RestMethod -Method Post -Uri $apiBase -ContentType 'application/json' -Body $payload
Invoke-RestMethod -Uri "$apiBase/$($createdRequest.id)"
$statusPayload = @{ status = 'IN_PROGRESS' } | ConvertTo-Json
Invoke-RestMethod -Method Patch -Uri "$apiBase/$($createdRequest.id)/status" -ContentType 'application/json' -Body $statusPayload
Invoke-RestMethod -Uri "${apiBase}?status=IN_PROGRESS&search=Printer"
```

## Health

`GET /actuator/health` is available inside the backend container (or port 8080 in the development override). It includes database connectivity. Nginx does not publish this endpoint.
