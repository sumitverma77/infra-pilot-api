# CI/CD & GitOps Evolution Guide

This guide explains the continuous integration and deployment (CI/CD) pipelines of **InfraPilot**, how they are structured, and how they evolve into advanced GitOps practices using tools like **ArgoCD**.

---

## 1. Push-Based vs. Pull-Based Deployments (GitHub Actions vs. ArgoCD)

There are two major paradigms for deploying code in the cloud: **Push-Based** and **Pull-Based**.

### Push-Based Deployment (Current Setup: GitHub Actions)
In a push-based model, your CI/CD runner has direct access to your target cloud environment. 
* **How it works:** GitHub Actions compiles the code, builds the container, pushes it to ECR, and then calls the AWS API (`aws ecs update-service`) to **push** the update directly into ECS.
* **Pros:** Simple to set up, fast, and does not require any agents running inside your cluster.
* **Cons:** 
  * **Security Risk:** You must store highly privileged AWS credentials (`AWS_ACCESS_KEY_ID` or OIDC roles) inside GitHub secrets. If your GitHub account is compromised, the attacker has access to your AWS account.
  * **Configuration Drift:** If an engineer manually modifies the ECS service in the AWS Console, GitHub Actions does not know about it. The live environment drifts from your codebase.

---

### Pull-Based Deployment (GitOps with ArgoCD)
In a pull-based model, you run a GitOps controller (like **ArgoCD** or **Flux**) *inside* your Kubernetes cluster.

```
                  ┌──────────────────────┐
                  │   GitHub Manifests   │ (Defines target state)
                  └──────────┬───────────┘
                             │
                             ▼
 ┌────────────────── Kubernetes Cluster ──────────────────┐
 │                                                        │
 │   ┌──────────────┐             Compare &               │
 │   │    ArgoCD    │◄────────────────────────────────┐   │
 │   │  Controller  │                                 │   │
 │   └──────┬───────┘                                 │   │
 │          │                                         │   │
 │          │ Pull config                             │   │
 │          ▼                                         ▼   │
 │   ┌──────────────┐                        ┌──────────┐ │
 │   │ Application  │                        │   Live   │ │
 │   │     Pods     │                        │  State   │ │
 │   └──────────────┘                        └──────────┘ │
 └────────────────────────────────────────────────────────┘
```

* **How it works:** 
  1. You store your Kubernetes YAML configurations (or Helm charts) in a dedicated repository.
  2. ArgoCD runs as an agent inside the cluster, continuously polling that Git repository.
  3. When you push a change to Git (e.g. changing the image tag to `v1.2.0`), ArgoCD detects the difference, pulls the new configuration, and applies it locally.
* **Pros:**
  * **No Credentials in GitHub:** GitHub does not need AWS credentials. The cluster pulls configuration from Git; it never receives pushes from the outside.
  * **Self-Healing (Zero Drift):** If someone manually changes a setting in the AWS Console, ArgoCD detects that the live cluster state deviates from the Git repository. It instantly overwrites the manual changes and reverts them back to what is defined in Git.
* **Cons:** Requires Kubernetes (cannot easily run on raw ECS Fargate) and introduces operational agent overhead.

---

## 2. Path-Filtering & Branch Environments

We optimized your pipelines to use industry best practices for efficiency and cost reduction:

* **Path Filtering:** 
  By configuring `paths:` filters in the workflow triggers, we prevent GitHub Actions from running costly Java compile/test sequences when you are only editing documentation, Terraform, or monitoring dashboards.
* **Branch-to-Environment Mapping:**
  Pushes to the `main` branch map to the `prod` environment context, pulling `PROD_ECS_CLUSTER` variables. Pushes to the `stage` branch map to `stage` variables, enabling clean environment promotion.

---

## 3. Hands-on CI/CD Activities to Try

Perform these activities inside this repository to practice CI/CD operations:

### Activity 1: Trigger Path-Filtering Checks
* **Goal:** Verify that path filtering is working correctly.
* **Steps:**
  1. Create a new branch: `git checkout -b test/path-filters`.
  2. Edit `README.md` (add a mock comment). Commit and push the branch.
  3. Open a Pull Request on GitHub. Notice that **no checks run**.
  4. Now, edit a file in the `terraform/` directory (e.g., add a comment in `outputs.tf`). Push the commit. Notice that **only the `Terraform Check` runs**.
  5. Finally, edit a file in `application/src/main/resources/application.yml`. Push the commit. Notice that the **`PR Validation` runs**.
* **Learning Outcome:** You will see how path filtering prevents redundant builds and keeps validation feedback loops fast.

### Activity 2: Simulate an Automated Rollout
* **Goal:** Practice GitOps-style branch merges.
* **Steps:**
  1. Make a minor code change in your Java application.
  2. Push the change to the `stage` branch.
  3. Verify that the automated CI/CD pipeline builds the docker image, pushes it to ECR, and deploys it to your staging environment (which will resolve to your staging cluster/ALB variables).
  4. Merge `stage` into `main` and watch the automated pipeline deploy the exact same change to production.
