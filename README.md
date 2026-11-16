# Spring Boot Monitoring App

Spring Boot-Anwendung mit Prometheus und Grafana zum Überwachen von CPU, Speicher und HTTP-Anfragen.

## Architektur

Die App läuft lokal auf Port 8080. Prometheus (in Docker) fragt alle 15 Sekunden `/actuator/prometheus` ab und speichert die Daten. Grafana (in Docker) liest von Prometheus und zeigt ein Dashboard auf Port 3000.

```
+---------------------+     +---------------------+
|  Spring Boot App    |     |  Docker Desktop     |
|  Port 8080          |     |                     |
|                     |     | +-----------+       |
| /actuator/prometheus|---->| Prometheus  |       |
|                     |     | Port 9090   |       |
+---------------------+     |     |       |       |
                            |     v       |       |
                            | +-----------+       |
                            | |  Grafana    |     |
                            | | Port 3000   |     |
                            | +-------------+     |
                            +---------------------+
```

Datenfluss:

```
App (8080) → Prometheus (9090) → Grafana (3000)
```

## Projektstruktur

```
SpringBoot_Monitoring_APP_DevOps/
├── src/
│   ├── main/
│   │   ├── java/com/monitoring/
│   │   │   ├── MonitoringApplication.java
│   │   │   ├── controller/DemoController.java
│   │   │   └── config/MetricsConfig.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/index.html
│   └── test/java/com/monitoring/
│       ├── MonitoringApplicationTests.java
│       ├── controller/DemoControllerTest.java
│       └── config/ActuatorEndpointTest.java
├── docker-compose.yml
├── Dockerfile
├── prometheus/prometheus.yml
├── grafana/
│   ├── dashboards/monitoring.json
│   └── provisioning/
├── .gitignore
└── Lessons_Learned_Monitoring.md
```

## Endpunkte

| Endpunkt | Methode | Beschreibung |
|----------|---------|-------------|
| `/api/demo/hello` | GET | Begrüßung und Visit-Count |
| `/api/demo/slow` | GET | Simuliert langsame Antwort (~1 Sekunde) |
| `/api/demo/work` | POST | Simuliert rechenintensive Arbeit |
| `/api/demo/error` | GET | Wirft RuntimeException |

## Custom Metrics

Jeder Endpunkt hat einen eigenen Counter in `MetricsConfig.java`:

| Metrik | Endpunkt |
|--------|----------|
| `app.hello.count` | `/api/demo/hello` |
| `app.slow.count` | `/api/demo/slow` |
| `app.work.count` | `/api/demo/work` |
| `app.error.count` | `/api/demo/error` |

Zusätzlich: `app.custom.events` (Counter), `app.custom.gauge` (Gauge), `app.custom.timer` (Timer).

## Grafana Dashboard

Das Dashboard `monitoring.json` enthält 8 Panels: CPU Usage, JVM Memory (Heap/Non-Heap), HTTP Request Rate, HTTP Response Time, Active Threads, GC Pause Time und Custom Metrics.

## Schnellstart

### Projekt klonen

```bash
git clone https://github.com/R-Yanay/SpringBoot_Monitoring_App_DevOps.git
cd SpringBoot_Monitoring_App_DevOps
```

### App starten

In IntelliJ: `MonitoringApplication.java` öffnen und ausführen.

Oder im Terminal:

```bash
mvn spring-boot:run
```

App läuft auf `http://localhost:8080`.

### Monitoring starten

Docker Desktop muss laufen.

```powershell
docker compose up -d prometheus grafana
```

### Dashboard öffnen

1. `http://localhost:3000` im Browser öffnen
2. Login: **admin** / **admin**
3. Links: **Dashboards** → **Browse** → **Spring Boot Monitoring**

## Tests

8 Tests in 3 Klassen:

| Test-Klasse | Anzahl | Inhalt |
|-------------|--------|--------|
| `MonitoringApplicationTests` | 1 | App startet ohne Fehler |
| `DemoControllerTest` | 4 | Alle 4 Demo-Endpunkte |
| `ActuatorEndpointTest` | 3 | `/health`, `/metrics`, `/prometheus` |

### Mit Maven

```bash
mvn test
```

Ergebnis: 8/8 Tests bestanden, `BUILD SUCCESS`.

### In IntelliJ

Rechte Maustaste auf `src/test/java/com/monitoring/` → **Run 'All Tests'**.

Falls die grünen Ausführen-Pfeile fehlen: `pom.xml` öffnen und **Reload All Maven Projects** klicken.

## DevOps-Status

| Phase | Status |
|-------|--------|
| CODE | Fertig |
| BUILD | Fertig (GitHub Actions CI/CD) |
| TEST | Fertig (8 Tests) |
| DEPLOY | Teilweise (Docker Compose manuell) |
| OPERATE | Teilweise (Alerting fehlt) |
| MONITOR | Fertig (Prometheus + Grafana) |

### Offene Aufgaben

- [ ] Grafana Alerting konfigurieren (Schritt 3)
- [ ] Vollständiges Docker-Deploy testen (alle 3 Services) (Schritt 4)
