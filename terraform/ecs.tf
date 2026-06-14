resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.name_prefix}"
  retention_in_days = 30
}

resource "aws_ecs_cluster" "this" {
  name = local.name_prefix

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_ecs_task_definition" "app" {
  family                   = local.name_prefix
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = var.project_name
      image     = var.container_image
      essential = true
      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
        { name = "SERVER_PORT", value = tostring(var.container_port) },
        { name = "INFRAPILOT_APP_NAME", value = var.app_name },
        { name = "INFRAPILOT_APP_VERSION", value = var.app_version },
        { name = "INFRAPILOT_GIT_COMMIT_SHA", value = var.git_commit_sha },
        { name = "INFRAPILOT_BUILD_TIMESTAMP", value = var.build_timestamp },
        { name = "INFRAPILOT_HOSTNAME", value = var.hostname },
        { name = "INFRAPILOT_ENVIRONMENT", value = var.runtime_environment },
        { name = "SPRING_DATASOURCE_URL", value = var.db_url },
        { name = "SPRING_DATASOURCE_USERNAME", value = var.db_username },
        { name = "SPRING_REDIS_HOST", value = var.redis_host },
        { name = "SPRING_REDIS_PORT", value = tostring(var.redis_port) }
      ]
      secrets = [
        { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = aws_secretsmanager_secret.database_password.arn },
        { name = "SPRING_REDIS_PASSWORD", valueFrom = aws_secretsmanager_secret.redis_password.arn }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "app"
        }
      }
      healthCheck = {
        command     = ["CMD-SHELL", "exit 0"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])
}

resource "aws_ecs_service" "app" {
  name                               = local.name_prefix
  cluster                            = aws_ecs_cluster.this.id
  task_definition                    = aws_ecs_task_definition.app.arn
  desired_count                      = var.desired_count
  launch_type                        = "FARGATE"
  platform_version                   = "LATEST"
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200
  enable_execute_command             = true
  health_check_grace_period_seconds   = 60
  propagate_tags                     = "SERVICE"

  network_configuration {
    subnets         = aws_subnet.private[*].id
    security_groups = [aws_security_group.ecs.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name    = var.project_name
    container_port    = var.container_port
  }

  depends_on = [
    aws_lb_listener.http
  ]
}
