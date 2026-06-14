# InfraPilot - Project Context

## Purpose

InfraPilot is NOT a business application.

The goal is to learn and demonstrate production-grade backend engineering, DevOps, cloud infrastructure, deployment strategies, observability, CI/CD, containerization, and AWS architecture.

This project exists primarily for learning and interview preparation.

Business functionality should remain intentionally simple.

## Primary Learning Objectives

Learn and demonstrate:

* Spring Boot Production Practices
* Docker
* Containerization
* Docker Networking
* PostgreSQL Integration
* Redis Integration
* Health Checks
* Readiness and Liveness Probes
* Observability
* Prometheus
* Grafana
* Structured Logging
* Correlation IDs
* GitHub Actions
* CI/CD Pipelines
* Image Registries (ECR)
* ECS Fargate
* Application Load Balancer
* AWS Secrets Manager
* CloudWatch
* Deployment Strategies

  * Rolling Deployment
  * Blue/Green Deployment
  * Canary Deployment
* Infrastructure as Code
* Terraform
* Production Architecture

## Learning Philosophy

Infrastructure should be introduced gradually.

The project should never become so complex that understanding is lost.

The objective is not to build a feature-rich application.

The objective is to understand why production systems are built the way they are.

Every technology introduced must solve a real problem.

Examples:

Docker:

* Solves "works on my machine" problems.

Redis:

* Solves caching and performance problems.

PostgreSQL:

* Solves persistence requirements.

Prometheus:

* Solves metrics collection.

Grafana:

* Solves metrics visualization.

GitHub Actions:

* Solves build and deployment automation.

ECR:

* Solves image storage and distribution.

ECS:

* Solves container orchestration.

ALB:

* Solves traffic routing and health-based load balancing.

Terraform:

* Solves repeatable infrastructure provisioning.

Secrets Manager:

* Solves secret management.

## Project Scope

Business functionality should remain minimal.

Only enough functionality should exist to validate infrastructure.

Endpoints may include:

* Health Endpoint
* Version Endpoint
* Environment Information Endpoint
* Redis Connectivity Test
* Database Connectivity Test

Avoid:

* User Management
* Authentication Systems
* Shopping Carts
* Payments
* Blogs
* Complex CRUD Systems
* Large Business Domains

## Expected Final Architecture

User
↓
Application Load Balancer
↓
ECS Service
↓
Spring Boot Application

Spring Boot Application
↓
PostgreSQL

Spring Boot Application
↓
Redis

Prometheus
↓
Scrapes Metrics

Grafana
↓
Reads Metrics From Prometheus

GitHub
↓
GitHub Actions
↓
Build Docker Image
↓
Push To ECR
↓
Deploy To ECS

Terraform
↓
Creates AWS Infrastructure

## Repository Expectations

The repository should showcase:

* Clean Code
* Production Readiness
* Observability
* Deployment Automation
* Infrastructure as Code
* Cloud-Native Architecture

The repository should look like something a backend engineer or platform engineer would present during interviews.

## Important Rule

Always explain WHY a technology is being introduced.

Do not add technologies simply because they are commonly used.

Every component should solve a clearly defined problem and contribute to the learning objectives of this project.
