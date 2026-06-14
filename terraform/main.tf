terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = merge(var.tags, {
      Environment = var.environment
      Service     = var.project_name
    })
  }
}

data "aws_caller_identity" "current" {}

data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

resource "aws_secretsmanager_secret" "application_metadata" {
  name_prefix = "${local.name_prefix}/application"
}

resource "aws_secretsmanager_secret" "database_password" {
  name_prefix = "${local.name_prefix}/database-password"
}

resource "aws_secretsmanager_secret" "redis_password" {
  name_prefix = "${local.name_prefix}/redis-password"
}
