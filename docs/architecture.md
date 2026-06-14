# Architecture

InfraPilot is structured as a small Spring Boot API backed by PostgreSQL and Redis, wrapped with observability and deployed through ECS Fargate.

## Principles

- Minimal business logic
- Clear layer boundaries
- Environment-driven configuration
- Operational visibility by default
- Container-first and cloud-first delivery

## Runtime Dependencies

- PostgreSQL stores the `system_event` table used by the database test endpoint.
- Redis stores a timestamp used by the cache test endpoint.
- Actuator exposes readiness, liveness, metrics, and Prometheus scraping.
- Grafana visualizes JVM, memory, CPU, HTTP request, and error rate metrics.
