"""Exercise the full HTTP path and persistence after Docker Compose starts."""
import json
import os
import urllib.request

base = f"http://127.0.0.1:{os.environ.get('WEB_PORT', '4200')}"

def call(path, method="GET", data=None):
    payload = json.dumps(data).encode() if data is not None else None
    request = urllib.request.Request(base + path, data=payload, method=method,
                                     headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(request, timeout=15) as response:
        return response.status, json.load(response)

with urllib.request.urlopen(base, timeout=15) as response:
    assert response.status == 200
    assert b"<app-root>" in response.read()

status, created = call("/api/requests", "POST", {
    "title": "Smoke test printer", "description": "Verify end-to-end request flow", "priority": "HIGH"
})
assert status == 201 and created["status"] == "NEW"
path = "/api/requests/" + created["id"]
assert call(path)[1]["title"] == "Smoke test printer"
assert call(path + "/status", "PATCH", {"status": "RESOLVED"})[1]["status"] == "RESOLVED"
assert call(path)[1]["status"] == "RESOLVED"
assert call("/api/requests?status=RESOLVED&search=Smoke%20test")[1]["totalElements"] >= 1
print("PASS: frontend served; request created, read, updated, persisted and filtered through Nginx.")
