# SRE & Operations Debugging Playbook

This playbook outlines step-by-step instructions for troubleshooting common failures in the **InfraPilot** cloud environment.

---

## 1. Scenario: ECS Task Keeps Restarting (Exit Code 1 / 137)

### Symptoms:
Tasks display status `STOPPED` shortly after changing to `RUNNING`.

### Potential Causes & Investigation Steps:
1. **Retrieve the Stopped Reason:**
   Run this AWS CLI command to see why the task was stopped:
   ```bash
   aws ecs describe-tasks \
     --cluster infrapilot-prod \
     --tasks <TASK_ID> \
     --query "tasks[0].{Reason:stoppedReason,Code:containers[0].exitCode}"
   ```
2. **Read Container logs:**
   Since your image is **distroless**, you cannot SSH/exec into the container. You must read logs from CloudWatch:
   ```bash
   aws logs get-log-events \
     --log-group-name "/ecs/infrapilot-prod" \
     --log-stream-name "app/infrapilot/<TASK_ID>" \
     --limit 100
   ```
3. **If Out-of-Memory (Exit Code 137):**
   Fargate terminated the task because it exceeded its memory allocation. 
   * **Fix:** Increase the `task_memory` default in `variables.tf` (e.g. from `2048` to `4096`).

---

## 2. Scenario: Deployment Stuck / Tasks Constantly Replacing

### Symptoms:
Your GitHub deployment step waits indefinitely, and ECS continuously boots new tasks and kills them.

### Potential Causes & Investigation Steps:
1. **Check ALB Health Status:**
   Go to the EC2 Console -> Target Groups -> `infrapilot-prod-tg`. Look at the **Targets** list.
2. **If targets are marked `unhealthy` with HTTP code 503:**
   Spring Actuator's `/actuator/health/readiness` is failing. Look at the application logs. It is likely that:
   * The database sidecar is crashing (check `postgres` container log stream).
   * The Redis sidecar is crashing (check `redis` container log stream).
3. **If targets are marked `unhealthy` with timeout/connection refused:**
   The application is taking longer than 120 seconds to boot up, and the ALB kills it before it finishes starting.
   * **Fix:** Increase the `health_check_grace_period_seconds` in [ecs.tf](file:///c:/Users/ASUS/Desktop/Sumit/vscode/infra-pilot/terraform/ecs.tf).

---

## 3. Scenario: Database Connection Errors

### Symptoms:
Logs show `Connection to localhost:5432 refused` or `HikariPool - Connection is not available`.

### Potential Causes & Investigation Steps:
1. **Verify Connection URI:**
   Since PostgreSQL is running as a sidecar inside the same task, Spring Boot must connect to `localhost:5432` (not `postgres:5432` or an external URL). Verify the environment variable in [ecs.tf](file:///c:/Users/ASUS/Desktop/Sumit/vscode/infra-pilot/terraform/ecs.tf):
   ```hcl
   { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://localhost:5432/infrapilot" }
   ```
2. **Check Database Container Initialization:**
   Inspect the logs for the `postgres` container. Verify it successfully initialized the database and user named `infrapilot`.

---

## 4. Scenario: ALB Returns 502 Bad Gateway

### Symptoms:
Visiting your ALB DNS returns an HTTP 502 page.

### Potential Causes & Investigation Steps:
1. **Verify Application Port:**
   The Application Load Balancer Target Group forwards traffic to port `8080`.
   * Check if your application container is running on a different port.
   * Verify that the security group `aws_security_group.ecs` allows inbound traffic on port `8080` from `aws_security_group.alb`.
