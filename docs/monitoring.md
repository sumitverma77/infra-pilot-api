# Monitoring

## Metrics

InfraPilot exposes:

- JVM metrics
- Memory metrics
- HTTP request metrics
- Database metrics
- Redis metrics

## Dashboards

Grafana dashboards are provisioned automatically from the `monitoring/grafana` directory.

## Prometheus

Prometheus scrapes `/actuator/prometheus` from the application service.
