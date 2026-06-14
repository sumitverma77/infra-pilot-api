# Terraform Guide

This guide explains how the Terraform stack in InfraPilot works, how to connect it to AWS, and the exact command flow to provision the environment.

## What Terraform Does Here

Terraform is used to provision the AWS infrastructure around the application so the repository demonstrates real platform engineering rather than only application code.

The current stack creates:

- VPC, public subnets, private subnets, route tables, internet gateway, and NAT gateway
- Security groups for the ALB and ECS tasks
- Amazon ECR repository for the Docker image
- ECS cluster and ECS Fargate service
- Application Load Balancer and target group
- CloudWatch log group
- IAM roles for ECS task execution and task runtime
- AWS Secrets Manager secret containers

## How It Works

Terraform reads the `.tf` files in the `terraform/` directory, compares your desired infrastructure to what exists in AWS, and then creates an execution plan.

The typical flow is:

1. `terraform init` downloads the AWS provider and prepares the working directory.
2. `terraform plan` shows the changes Terraform will make.
3. `terraform apply` creates or updates the AWS resources.
4. `terraform destroy` removes the resources.

Terraform is declarative. You describe the target state, and Terraform figures out the API calls and dependency order.

## AWS CLI vs Terraform

AWS CLI and Terraform solve different problems:

- AWS CLI is for direct manual actions against AWS.
- Terraform is for repeatable infrastructure management.

You do not “connect” Terraform to AWS CLI in the same way you connect an application to a database.

Instead, Terraform authenticates to AWS using the same AWS credentials that the AWS CLI can use.

That means you can authenticate once with `aws configure` or environment variables, and both AWS CLI and Terraform can reuse those credentials.

## Prerequisites

Install:

- AWS CLI v2
- Terraform 1.6 or later
- Docker, if you plan to build and push the application image

Verify your AWS identity first:

```bash
aws sts get-caller-identity
```

If that command works, Terraform can usually use the same credentials.

## Authenticate AWS

### Option 1: AWS CLI profile

```bash
aws configure
```

Provide:

- AWS access key ID
- AWS secret access key
- Default region
- Default output format

Then verify:

```bash
aws sts get-caller-identity
```

### Option 2: Environment variables

```bash
set AWS_ACCESS_KEY_ID=your-access-key
set AWS_SECRET_ACCESS_KEY=your-secret-key
set AWS_SESSION_TOKEN=your-session-token
set AWS_REGION=us-east-1
```

Terraform will automatically read these values.

## Initialize Terraform

Run Terraform from the `terraform/` directory:

```bash
cd terraform
terraform init
```

What this does:

- Downloads the AWS provider
- Creates the `.terraform/` working directory
- Generates or updates the lock file

If you change provider versions later, use:

```bash
terraform init -upgrade
```

## Plan the Infrastructure

The stack requires several input values, because the application running in ECS needs environment-specific metadata and external dependency locations.

Example:

```bash
terraform plan \
  -var="container_image=123456789012.dkr.ecr.us-east-1.amazonaws.com/infrapilot:latest" \
  -var="app_version=1.0.0" \
  -var="git_commit_sha=abc123" \
  -var="build_timestamp=2026-06-14T00:00:00Z" \
  -var="hostname=infrapilot-prod" \
  -var="runtime_environment=prod" \
  -var="db_url=jdbc:postgresql://your-postgres.example.com:5432/infrapilot" \
  -var="db_username=infrapilot" \
  -var="redis_host=your-redis.example.com"
```

What these variables mean:

- `container_image` is the Docker image ECS will run.
- `app_version`, `git_commit_sha`, and `build_timestamp` are exposed through the version endpoint.
- `hostname` and `runtime_environment` are exposed through the info endpoint.
- `db_url`, `db_username`, and `redis_host` wire the runtime to its dependencies.

## Apply the Infrastructure

When the plan looks correct:

```bash
terraform apply \
  -var="container_image=123456789012.dkr.ecr.us-east-1.amazonaws.com/infrapilot:latest" \
  -var="app_version=1.0.0" \
  -var="git_commit_sha=abc123" \
  -var="build_timestamp=2026-06-14T00:00:00Z" \
  -var="hostname=infrapilot-prod" \
  -var="runtime_environment=prod" \
  -var="db_url=jdbc:postgresql://your-postgres.example.com:5432/infrapilot" \
  -var="db_username=infrapilot" \
  -var="redis_host=your-redis.example.com"
```

Terraform will create resources in dependency order.

## Recommended Deployment Flow

Because ECS needs a real container image, use this sequence:

1. Run `terraform apply` once to create the ECR repository and the rest of the base infrastructure.
2. Build the application image locally or in GitHub Actions.
3. Push the image to ECR.
4. Re-run `terraform apply` or trigger the deployment workflow with the new image tag.

This is the same reason the repository has both Terraform and GitHub Actions: Terraform creates the platform, while the workflow promotes new application versions.

## Useful Outputs

After apply, Terraform exposes outputs such as:

- ALB DNS name
- ECR repository URL
- ECS cluster name
- ECS service name
- Secret ARNs

Use them with:

```bash
terraform output
```

## Destroy the Stack

If you want to remove everything Terraform created:

```bash
terraform destroy \
  -var="container_image=123456789012.dkr.ecr.us-east-1.amazonaws.com/infrapilot:latest" \
  -var="app_version=1.0.0" \
  -var="git_commit_sha=abc123" \
  -var="build_timestamp=2026-06-14T00:00:00Z" \
  -var="hostname=infrapilot-prod" \
  -var="runtime_environment=prod" \
  -var="db_url=jdbc:postgresql://your-postgres.example.com:5432/infrapilot" \
  -var="db_username=infrapilot" \
  -var="redis_host=your-redis.example.com"
```

## What Each Terraform File Does

- [main.tf](../terraform/main.tf) defines the provider, AWS region, and Secrets Manager resources.
- [vpc.tf](../terraform/vpc.tf) creates the network layer.
- [security-groups.tf](../terraform/security-groups.tf) controls inbound and outbound traffic.
- [ecr.tf](../terraform/ecr.tf) creates the image repository.
- [alb.tf](../terraform/alb.tf) creates the load balancer and health-check target group.
- [iam.tf](../terraform/iam.tf) defines the ECS roles and permissions.
- [ecs.tf](../terraform/ecs.tf) creates the cluster, task definition, service, and CloudWatch logging.
- [outputs.tf](../terraform/outputs.tf) exposes the values you need after deployment.

## Important Notes

- Terraform creates the AWS infrastructure, but it does not create PostgreSQL or Redis servers in this repository. Those are expected to be managed services or separate dependencies.
- The ECS task definition injects runtime environment variables and Secrets Manager values into the container.
- The ALB health check uses `/actuator/health/readiness` so the service only receives traffic when PostgreSQL and Redis are healthy.

## Common Mistakes

- Running `terraform apply` without AWS credentials.
- Forgetting to provide `container_image`.
- Pushing the image to ECR after the ECS service has already been updated.
- Using the wrong AWS region, which makes the ECR URI and ALB outputs look incorrect.

## Short Version

If you only want the minimum command sequence:

```bash
aws sts get-caller-identity
cd terraform
terraform init
terraform plan -var="..."
terraform apply -var="..."
```

Then build and push the image to ECR, and update the ECS deployment with the new image tag.