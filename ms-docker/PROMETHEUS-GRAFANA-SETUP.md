# Monitoreo con Prometheus y Grafana - ms-clientes

## Descripción
Este setup permite monitorear el microservicio `ms-clientes` utilizando Prometheus para recolectar métricas de Spring Boot Actuator en formato Prometheus, y Grafana para visualizarlas.

## Componentes Configurados

### 1. Spring Boot Actuator (ms-clientes)
- **Dependencias Maven**:
  - `spring-boot-starter-actuator`: Expone endpoints de monitoreo
  - `micrometer-registry-prometheus`: Registra métricas en formato Prometheus

- **Endpoints Disponibles**:
  - Health: `http://localhost:6080/actuator/health`
  - Métricas Prometheus: `http://localhost:6080/actuator/prometheus`
  - Todas las métricas: `http://localhost:6080/actuator/metrics`

### 2. Prometheus
- **Puerto**: 6590
- **URL**: http://localhost:6590
- **Configuración**: `ms-docker/cfg/prometheus.yml`
- **Scrape Targets**: 
  - ms-clientes-svc-1:8080
  - ms-clientes-svc-2:8080
  - ms-clientes-svc-3:8080
- **Scrape Interval**: 5 segundos

### 3. Grafana
- **Puerto**: 6591
- **URL**: http://localhost:6591
- **Credenciales**:
  - Usuario: `admin`
  - Contraseña: `admin`
- **Datasource**: Prometheus (pre-configurado)

## Pasos para Levantar el Sistema

### 1. Crear la red backend-net (si no existe)
```powershell
docker network create backend-net
```

### 2. Construir y levantar ms-clientes
```powershell
cd ms-clientes
docker-compose up -d --build
```

### 3. Levantar Prometheus y Grafana
```powershell
cd ..\ms-docker
docker-compose -f docker-compose-perf.yml up -d
```

### 4. Verificar que todo está corriendo
```powershell
docker ps
```

Deberías ver corriendo:
- mysql-db
- phpmyadmin-container
- ms-clientes-svc-1
- ms-clientes-svc-2
- ms-clientes-svc-3
- prometheus
- grafana

## Verificación del Sistema

### 1. Verificar Actuator de ms-clientes
```powershell
# Health check
curl http://localhost:6080/actuator/health

# Métricas Prometheus (muestra texto plano con todas las métricas)
curl http://localhost:6080/actuator/prometheus
```

### 2. Verificar Prometheus
1. Abrir http://localhost:6590
2. Ir a Status → Targets
3. Verificar que los 3 targets de ms-clientes estén "UP" (en verde)
4. Ir a Graph y ejecutar queries como:
   - `http_server_requests_seconds_count`
   - `jvm_memory_used_bytes`
   - `system_cpu_usage`

### 3. Configurar Grafana

#### Primera vez (Login):
1. Abrir http://localhost:6591
2. Login con `admin` / `admin`
3. (Opcional) Cambiar la contraseña

#### Verificar Datasource:
1. Ir a **Configuration** (⚙️) → **Data sources**
2. Deberías ver "Prometheus" ya configurado
3. Click en "Test" para verificar la conexión

#### Crear tu primer Dashboard:

**Opción A: Importar Dashboard pre-hecho de Spring Boot**
1. Click en **+** → **Import Dashboard**
2. Ingresar ID: `11378` (Spring Boot 2.1 System Monitor)
3. O ID: `4701` (JVM Micrometer)
4. Click en **Load**
5. Seleccionar datasource "Prometheus"
6. Click en **Import**

**Opción B: Crear Dashboard manual**
1. Click en **+** → **Create Dashboard**
2. Click en **Add new panel**
3. En "Query", seleccionar datasource "Prometheus"
4. Agregar queries ejemplo:
   - **Request Rate**: `rate(http_server_requests_seconds_count[1m])`
   - **Memory Usage**: `jvm_memory_used_bytes{area="heap"}`
   - **CPU Usage**: `system_cpu_usage`
   - **Response Time (p99)**: `http_server_requests_seconds{quantile="0.99"}`
5. Personalizar visualización (Graph, Stat, Gauge, etc.)
6. Click en **Apply** y luego **Save dashboard**

## Métricas Importantes Disponibles

### HTTP Requests
- `http_server_requests_seconds_count`: Total de requests
- `http_server_requests_seconds_sum`: Tiempo total de procesamiento
- `http_server_requests_seconds_max`: Tiempo máximo de request

### JVM
- `jvm_memory_used_bytes`: Memoria utilizada
- `jvm_memory_max_bytes`: Memoria máxima disponible
- `jvm_threads_live`: Threads activos
- `jvm_gc_pause_seconds_count`: Conteo de garbage collections

### Sistema
- `system_cpu_usage`: Uso de CPU del sistema
- `process_cpu_usage`: Uso de CPU del proceso
- `system_load_average_1m`: Load average del sistema

### Base de Datos (HikariCP)
- `hikaricp_connections_active`: Conexiones activas
- `hikaricp_connections_idle`: Conexiones idle
- `hikaricp_connections_pending`: Conexiones pendientes

## Queries Útiles en Prometheus/Grafana

### Request Rate por endpoint
```promql
rate(http_server_requests_seconds_count{job="ms-clientes"}[1m])
```

### Request Rate total
```promql
sum(rate(http_server_requests_seconds_count{job="ms-clientes"}[1m]))
```

### Response Time promedio
```promql
rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])
```

### Requests por segundo (todas las instancias)
```promql
sum(rate(http_server_requests_seconds_count{job="ms-clientes"}[1m])) by (instance)
```

### Uso de memoria heap
```promql
jvm_memory_used_bytes{job="ms-clientes", area="heap"}
```

### Tasa de error (status 4xx y 5xx)
```promql
sum(rate(http_server_requests_seconds_count{job="ms-clientes", status=~"4..|5.."}[1m]))
```

## Troubleshooting

### Prometheus no encuentra los targets
- Verificar que ms-clientes esté corriendo: `docker ps`
- Verificar que están en la misma red: `docker network inspect backend-net`
- Verificar los logs de Prometheus: `docker logs <prometheus-container-id>`

### Grafana no puede conectar con Prometheus
- Verificar que ambos contenedores estén en la misma red
- Verificar que Prometheus esté respondiendo: `curl http://localhost:6590/-/healthy`
- En Grafana, usar la URL interna: `http://prometheus:9090`

### No aparecen métricas
- Verificar que el endpoint de actuator funcione: `curl http://localhost:6080/actuator/prometheus`
- Hacer algunas requests al servicio para generar métricas
- Verificar en Prometheus que el target esté "UP"

## Parar los Servicios

```powershell
# Parar ms-clientes
cd ms-clientes
docker-compose down

# Parar Prometheus y Grafana
cd ..\ms-docker
docker-compose -f docker-compose-perf.yml down

# Si quieres eliminar los volúmenes también (se perderá data)
docker-compose down -v
```

## Notas Adicionales

- Los datos de Prometheus se persisten en un volumen Docker (`prometheus-data`)
- Los dashboards de Grafana se persisten en el volumen del contenedor
- El scrape interval está configurado en 5 segundos para ms-clientes
- Todas las instancias de ms-clientes (1, 2 y 3) están siendo monitoreadas
