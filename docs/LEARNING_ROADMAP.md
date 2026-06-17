# InfraPilot Study & Hands-On Learning Roadmap

This guide provides a structured pathway to master the DevOps, Cloud, and SRE concepts implemented in **InfraPilot**, and maps out how to study future enterprise cloud technologies.

---

## Phase 1: Theoretical Foundation (The Reading Order)

To build a solid mental model of cloud operations, read the documentation files in the following sequence:

```
┌───────────────────────────┐
│ 1. docs/PROJECT_CONTEXT   │ ◄── High-level overview of what the application does
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ 2. docs/architecture      │ ◄── VPC, Sidecars vs. Managed Databases, ECS vs. EKS
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ 3. docs/cicd              │ ◄── GitHub Actions, Path filtering, Push vs. GitOps Pull
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ 4. docs/terraform         │ ◄── State management, dependency graphs, HCL rules
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ 5. docs/troubleshooting   │ ◄── AWS CLI commands, container logs, exit codes
└───────────────────────────┘
```

---

## Phase 2: Hands-on Practice (The Activity Order)

After reading, execute these activities directly in your repository to translate theory into practical skills:

### Activity 1: Verify Trigger Path Filtering (Fast Feedback Loops)
* **What you do:** Make isolated commits to `README.md` (should run 0 checks), `terraform/ecs.tf` (should run only Terraform checks), and `application/` (should run only PR validation checks).
* **What you learn:** How path-filtering optimizes resource usage and prevents build times from bloating.

### Activity 2: Simulate Database State Loss (Ephemeral Storage)
* **What you do:** Call the `/api/v1/db-test` endpoint to save data, stop the ECS Fargate task in the AWS Console, let ECS restart it, and query the endpoint again.
* **What you learn:** You will see firsthand why containers have ephemeral storage and why production applications require separate databases (like AWS RDS).

### Activity 3: CLI Troubleshooting drills (SRE Operations)
* **What you do:** Use the AWS CLI in your local terminal to query your ECS task status, fetch container log streams from CloudWatch Logs, and inspect the Application Load Balancer health target outputs.
* **What you learn:** How to navigate cloud state and debug application crashes without having shell access (SSH) to your container.

---

## Phase 3: Future Technology Roadmap (Kubernetes & GitOps)

Once you master your current ECS Fargate setup, here is how you should progress to learn advanced enterprise tooling:

### Step 1: Run Kubernetes (K8s) Locally
* **Why:** You must learn raw Kubernetes concepts before deploying to cloud-hosted services like AWS EKS.
* **Activity:** Install **Minikube** or **Kind** on your local machine.
* **Milestone:** Deploy your Spring Boot app, Postgres database, and Redis cache as local Kubernetes **Pods** and **Services** in Minikube.

### Step 2: Learn Package Management with Helm
* **Why:** Writing raw Kubernetes YAML manifests for multiple environments creates duplication.
* **Activity:** Package your manifests into a **Helm Chart**. Use variables to easily toggle parameters between your Dev, Stage, and Prod setups.

### Step 3: Implement GitOps with ArgoCD
* **Why:** Moving from push-based pipelines to pull-based GitOps is the industry gold standard for security.
* **Activity:** 
  1. Install **ArgoCD** inside your local Minikube cluster.
  2. Create a separate Git repository for your Helm charts.
  3. Connect ArgoCD to your Git repository.
  4. Modify the image tag in Git, and watch ArgoCD automatically detect the change and upgrade your running containers.
