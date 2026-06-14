# Troubleshooting

## Startup failures

- Confirm all `INFRAPILOT_*` variables are set.
- Confirm PostgreSQL and Redis are reachable.
- Check Flyway migration output for schema mismatches.

## Readiness failures

- Inspect `/actuator/health/readiness`.
- PostgreSQL or Redis failures will fail readiness intentionally.

## Deployment failures

- Confirm the ECS task can pull the image from ECR.
- Confirm Secrets Manager values exist for database and Redis passwords.
- Confirm the ALB health check path matches `/actuator/health/readiness`.
