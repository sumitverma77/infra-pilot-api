# Cloud Architecture & Evolution Guide

This guide explains the architectural decisions of **InfraPilot**, why it is built this way, how it compares to enterprise-grade Kubernetes setups, and what challenges you will face as this application scales.

---

## 1. The Current Architecture: ECS Fargate with Local Sidecars

The current setup utilizes a **serverless container approach** with local databases running adjacent to your code inside a single Fargate task.

```
       [ Client Request ]
               │
               ▼
   [ Application Load Balancer ]
               │
               ▼
┌──────────────── Fargate Task ────────────────┐
│  (Shares localhost loopback interface)       │
│                                              │
│  ┌──────────────┐      jdbc:localhost:5432   │
│  │ Spring Boot  ├─────────────────────────┐  │
│  │  Container   │                         │  │
│  └──────┬───────┘                         ▼  │
│         │                          ┌──────────┐
│         │  localhost:6379          │ Postgres │
│         └─────────────────────────►│ Sidecar  │
│                                    └──────────┘
│                                    ┌──────────┐
│                                    │  Redis   │
│                                    │ Sidecar  │
│                                    └──────────┘
└──────────────────────────────────────────────┘
```

### Why this is excellent for Development & Learning:
* **Zero Database Base Cost:** Running PostgreSQL and Redis inside the container CPU/Memory space means you pay **$0** for database hosting. If you used AWS RDS (managed database) and ElastiCache (managed Redis), you would pay a minimum of **$30-$40/month** even if no traffic was hitting them.
* **No NAT Gateway Cost:** To connect to a database securely in AWS, the database must be placed in a private subnet, and your containers must connect using a **NAT Gateway**. A NAT Gateway has a baseline cost of **$32/month** plus data processing charges. Running sidecars in public subnets with security group blocks avoids this cost entirely.
* **Simplicity:** All services spin up together in a single task definition.

### The Limitations & Production Risks:
* **Ephemeral Data:** Because the Postgres container runs inside the Fargate task, the database storage is **non-persistent**. If the task restarts (due to an update, auto-scaling, or AWS hardware rotation), all database entries are wiped.
* **Single Point of Failure / Split-Brain:** If you scale to `desired_count = 2`, Fargate starts a second task. This second task has its own independent database sidecar. Users routed to Task 1 will write data that users routed to Task 2 cannot see.

---

## 2. ECS Fargate vs. Kubernetes (EKS)

In the industry, when an application grows, engineers must decide whether to host it on **AWS ECS** or migrate to **Kubernetes (AWS EKS)**.

### Comparison Table

| Feature | AWS ECS (Fargate) | AWS EKS (Kubernetes) |
| :--- | :--- | :--- |
| **Operational Effort** | **Low:** Serverless, AWS manages the control plane and infrastructure scaling. | **High:** You must manage cluster configuration, ingress, namespaces, and node patching. |
| **Baseline Cost** | **$0 / month** for the cluster. You only pay for active running task resources. | **~$73 / month** base fee just to keep the control plane running, plus VM worker nodes. |
| **Resource Footprint** | Extremely lightweight. | Heavy. Requires system containers (CoreDNS, kube-proxy, AWS CNI) running on every node. |
| **Portability** | Locked to AWS-specific resources (task definitions, ECS service APIs). | Fully portable. Any YAML config can run on Google Cloud (GKE) or Microsoft Azure (AKS). |
| **Ecosystem Size** | Smaller, AWS-specific. | Huge. Supports Helm, Prometheus Operators, ArgoCD, service meshes (Istio), etc. |

### When is ECS the better choice?
* For small to medium applications, startups, or single-product companies.
* When you want your engineers focused on building the application code rather than managing complex cluster networking.
* When you want to minimize monthly infrastructure baseline fees.

### When is EKS (Kubernetes) required?
* **Multi-Cloud Portability:** If your company has a strategy to run on both AWS and Google Cloud to prevent cloud lock-in.
* **Complex Microservices:** If you have 50+ microservices that need to communicate with each other using a service mesh (like Istio or Linkerd) for fine-grained routing, mutual TLS (mTLS), and traffic shaping.
* **Bin Packing (Cost Optimization at Scale):** If you run thousands of containers, you can pack them tightly on large EC2 instances, making it cheaper per container than Fargate's flat-rate pricing.

---

## 3. Hands-on Architecture Activities to Try

To help you learn and practice these cloud architect concepts, here are some activities you can perform in this repository:

### Activity 1: Simulate a Database "Crash" (Loss of Ephemeral State)
* **Goal:** Understand the difference between ephemeral container storage and persistent storage.
* **Steps:**
  1. Access your running application and write some test entries (call `/api/v1/db-test` several times).
  2. Log in to the AWS Console, select your ECS Cluster, find your running Task, and click **Stop**.
  3. ECS will automatically detect that the task died and spin up a new task to maintain your `desired_count = 1`.
  4. Call the health/db endpoints on the new task. Notice that the old database data is completely gone.
* **Learning Outcome:** You will see firsthand why production databases cannot run as ephemeral sidecars and require detached managed services like RDS.

### Activity 2: Evolve to a Production Database Setup (Architecture Sketch)
* **Goal:** Design the transition from sidecars to managed RDS.
* **Concept:** Sketch out how you would change [ecs.tf](file:///c:/Users/ASUS/Desktop/Sumit/vscode/infra-pilot/terraform/ecs.tf) to:
  1. Remove the `postgres` block from the container definitions list.
  2. Create an `aws_db_instance` (RDS Postgres) in your Terraform configurations.
  3. Pass the database endpoint (`aws_db_instance.this.endpoint`) to your Spring Boot container's environment variables.
