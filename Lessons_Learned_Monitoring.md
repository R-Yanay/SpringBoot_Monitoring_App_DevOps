# Lessons Learned: Monitoring mit Docker, Prometheus und Grafana

## Die drei Komponenten

| Komponente | Aufgabe |
|------------|---------|
| **Docker Desktop** | Bietet die Laufumgebung für Prometheus und Grafana als Container |
| **Prometheus** | Fragt alle 15 Sekunden die App ab und speichert die Metriken |
| **Grafana** | Liest die Daten von Prometheus und visualisiert sie als Dashboard |

## Datenfluss

1. Spring Boot App läuft lokal auf Port 8080
2. Prometheus (in Docker) fragt `http://host.docker.internal:8080/actuator/prometheus` alle 15 Sekunden ab
3. Die App antwortet mit CPU, Speicher, HTTP-Anfragen und Custom Metrics
4. Prometheus speichert die Werte
5. Grafana (in Docker) liest von Prometheus und zeigt das Dashboard auf Port 3000

IntelliJ startet nur die App. Docker, Prometheus und Grafana laufen unabhängig davon.

## Wichtige Erkenntnisse

### 1. `host.docker.internal` statt `app:8080`

Prometheus sucht die App standardmäßig unter `app:8080` (Container-Name). Da die App lokal läuft, muss in `prometheus.yml` der Target auf `host.docker.internal:8080` gesetzt werden. Das ist die interne Adresse, über die ein Container auf den Host-Computer zugreift.

### 2. Docker Desktop muss laufen

Ohne Docker Desktop starten Prometheus und Grafana nicht. Der Status im Taskleistensymbol muss "Docker Desktop Running" sein.

### 3. HTTP-Metriken brauchen Zeit

CPU und JVM Memory erscheinen sofort (Gauge-Werte). HTTP Request Rate und Response Time verwenden `rate()[5m]` und brauchen 5–10 Minuten an Traffic, bevor sie Daten anzeigen.

### 4. IntelliJ hat keine Verbindung zu Docker

IntelliJ startet nur die Java-App. Prometheus und Grafana laufen separat in Docker.

## Container aktualisieren nach Änderungen

Container haben beim Start eine Kopie der Dateien. Änderungen im Projektordner werden nicht automatisch übernommen.

### `monitoring.json` geändert

Das Volume `./grafana/dashboards:/var/lib/grafana/dashboards` in der `docker-compose.yml` bindet den Ordner direkt ein. Ein Neustart reicht:

```powershell
docker compose restart grafana
```

### `prometheus.yml` oder `docker-compose.yml` geändert

Container neu erstellen:

```powershell
docker compose down
docker compose up -d
```

### Nur Java-Code geändert

App in IntelliJ stoppen und neu starten. Kein Docker-Befehl nötig.

## Befehle

### Container starten

```powershell
cd C:\Users\roiya\IdeaProjects\ProjectIdeas\SpringBoot_Monitoring_APP_DevOps
docker compose up -d prometheus grafana
```

### Container stoppen

```powershell
docker compose down
```

## URLs

| Service | URL | Login |
|---------|-----|-------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | kein Login |
| Spring Boot App | http://localhost:8080 | kein Login |
| Metriken | http://localhost:8080/actuator/prometheus | Rohdaten |

## Schnellstart

1. Docker Desktop starten
2. `docker compose up -d prometheus grafana` ausführen
3. Spring Boot App in IntelliJ starten
4. http://localhost:3000 öffnen, Dashboard auswählen
