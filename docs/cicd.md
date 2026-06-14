# CI/CD

## Pull Request Validation

- Builds the application
- Runs unit and integration tests
- Generates coverage via JaCoCo

## Image Build and Push

- Builds the application jar
- Produces a multi-stage Docker image
- Tags by git SHA
- Pushes to ECR

## Deployment

- Reads the existing ECS task definition
- Re-registers a new revision with the new image tag
- Updates the ECS service
- Waits for stability
- Validates the ALB health endpoints
