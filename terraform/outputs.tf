output "vpc_id" {
  value = aws_vpc.this.id
}

output "alb_dns_name" {
  value = aws_lb.this.dns_name
}

output "ecr_repository_url" {
  value = aws_ecr_repository.this.repository_url
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.this.name
}

output "ecs_service_name" {
  value = aws_ecs_service.app.name
}

output "application_secret_arn" {
  value = aws_secretsmanager_secret.application_metadata.arn
}

output "database_password_secret_arn" {
  value = aws_secretsmanager_secret.database_password.arn
}

output "redis_password_secret_arn" {
  value = aws_secretsmanager_secret.redis_password.arn
}
