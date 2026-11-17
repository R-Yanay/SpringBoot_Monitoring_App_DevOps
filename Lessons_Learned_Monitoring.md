# Lessons Learned: Monitoring und Logging mit Docker, Prometheus, Grafana und Loki

## Architektur: Prometheus und Grafana

Das Monitoring besteht aus zwei Komponenten mit klar getrennten Aufgaben:

**Prometheus** — Datensammlung und -speicherung
- Fragt in festem Intervall (15 Sekunden) den `/actuator/prometheus` Endpoint der App ab
- Speichert alle Metriken als Zeitreihen in einer eigenen Datenbank
- Bietet PromQL als Abfragesprache für Metriken-Analyse
- Prüft Alert-Regeln und meldet Statusänderungen

**Grafana** — Visualisierung und Alerting-Oberfläche
- Verbindet sich mit Prometheus als Datenquelle
- Zeigt Metriken als Dashboards mit Grafiken, Diagrammen und Zählern
- Verwaltet Alert-Regeln und zeigt den aktuellen Status (Normal/Firing)
- Bietet interaktive Filter, Zeitraum-Auswahl und Vergleichsfunktionen

Prometheus speichert die Daten, Grafana visualisiert sie. Beide benötigen einander: Prometheus ohne Grafana bietet nur eine Textkonsole, Grafana ohne Prometheus hat keine Datenquelle.

**Loki und Promtail** — Log-Sammlung
- Loki speichert Container-Logs (ähnlich wie Prometheus Metriken)
- Promtail liest die Logs von Docker-Containern und sendet sie an Loki
- In Grafana Explore: Datenquelle **Loki** wählen → Logs durchsuchen

## Datenfluss

**Metriken:**
```
App (8080) → Prometheus (9090) → Grafana (3000)
```
1. Spring Boot App stellt `/actuator/prometheus` bereit
2. Prometheus fragt alle 15 Sekunden die Metriken ab und speichert sie
3. Grafana liest auf Anfrage Daten von Prometheus und rendert das Dashboard

**Logs:**
```
App, Prometheus, Grafana (stdout) → Promtail → Loki (3100) → Grafana (3000)
```
1. Container schreiben Logs auf `stdout`
2. Promtail liest die Logs über die Docker-API und sendet sie an Loki
3. Grafana Explore zeigt die Logs über LogQL-Abfragen

## Zwei Betriebsmodi

### Vollständiges Docker-Deploy (Empfohlen)

Alle fünf Services — App, Prometheus, Grafana, Loki und Promtail — laufen in Docker-Containern im selben Netzwerk. Prometheus erreicht die App über den Container-Namen `app:8080`.

```bash
docker compose up -d
```

### App lokal entwickeln

Die App läuft lokal (z. B. über die IDE), nur Prometheus und Grafana in Docker. In diesem Fall muss `prometheus/prometheus.yml` auf `host.docker.internal:8080` zeigen — die spezielle Adresse, über die ein Container auf den Host-Computer zugreift.

```bash
docker compose up -d prometheus grafana
```

## Wichtige Erkenntnisse

### 1. Container-Kommunikation

Im Docker-Netzwerk erreichen sich Container über ihre Namen (`app:8080`, `prometheus:9090`). Vom Host aus ist nur `localhost:PORT` erreichbar. Beim Wechsel zwischen den Betriebsmodi muss `prometheus.yml` entsprechend angepasst werden.

### 2. Docker Desktop muss laufen

Ohne Docker Desktop starten Prometheus und Grafana nicht. Der Status muss "Running" sein.

### 3. HTTP-Metriken brauchen Zeit

CPU und JVM Memory erscheinen sofort (Gauge-Werte). HTTP Request Rate und Response Time verwenden `rate()[5m]` und brauchen 5–10 Minuten an Traffic, bevor sie Daten anzeigen.

### 4. Volume-Mounts statt `docker cp`

Die `docker-compose.yml` bindet Konfigurationsordner als Volumes ein. Änderungen an `monitoring.json` oder `rules.yml` werden nach einem `docker compose restart` automatisch übernommen — kein manuelles Kopieren in den Container nötig.

### 5. Logging mit Loki

Jeder Endpunkt in `DemoController.java` loggt eine INFO-Nachricht über `Logger`. Tomcat Access Logs werden nicht verwendet, da sie in Dateien statt auf `stdout` geschrieben werden und Promtail sie nicht sammeln kann. Die Log-Nachrichten enthalten Endpunkt, Parameter und Status (z. B. `GET /api/demo/hello - visits: 1`).

### 6. LogQL-Abfragen

In Grafana Explore: `{job="spring-boot-app"}` zeigt alle App-Logs. Mit `|="/api/demo"` nur Benutzer-Anfragen filtern. Den Zeitbereich auf "Last 1 hour" stellen, wenn keine frischen Logs erscheinen. Alte Logs mit `job = true` sind von der ersten Promtail-Konfiguration und verschwinden automatisch, wenn Loki sie löscht.

## Container aktualisieren nach Änderungen

### Dashboard oder Alert-Regeln geändert

```bash
docker compose restart grafana
```

### Prometheus-Konfiguration geändert

```bash
docker compose restart prometheus
```

### Docker-Compose oder Dockerfile geändert

```bash
docker compose down
docker compose up -d --build
```

### Nur Java-Code geändert

Bei vollem Docker-Deploy: `docker compose up -d --build` neu bauen.
Bei lokaler Entwicklung: App in der IDE neu starten.

## Nützliche URLs

| Service | URL | Login |
|---------|-----|-------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | kein Login |
| Loki | http://localhost:3100 | kein Login |
| Spring Boot App | http://localhost:8080 | kein Login |
| Metriken | http://localhost:8080/actuator/prometheus | Rohdaten |

## Schnellstart

1. Docker Desktop starten
2. `docker compose up -d` im Projektordner ausführen
3. `http://localhost:3000` öffnen, Dashboard auswählen

## YAML-Konfigurationsdateien im Überblick

Das Projekt verwendet 8 YAML-Dateien. Jede definiert die Konfiguration für einen spezifischen Service oder eine bestimmte Funktion.

### `docker-compose.yml` — Master-Plan für alle Container

Definiert alle 5 Docker-Container und wie sie zusammenarbeiten: App, Prometheus, Grafana, Loki und Promtail. Enthält Volume-Mounts, Health-Checks, das gemeinsame Netzwerk und Logging-Labels.

### `prometheus/prometheus.yml` — Metriken-Quellen

Sagt Prometheus, welche Apps er alle 15 Sekunden abfragen soll. Enthält den Job-Namen, das Target (`app:8080`) und den Endpoint (`/actuator/prometheus`).

### `loki-config.yml` — Log-Speicherung

Konfiguriert Loki: Port 3100, Boltdb-Speicher auf der Festplatte, Retention (alte Logs automatisch löschen) und keine Authentifizierung.

### `promtail-config.yml` — Log-Sammlung

Sagt Promtail, welche Container-Logs gesammelt werden. Filtert nach dem Label `logging=true`, entdeckt neue Container alle 5 Sekunden und sendet die Logs an Loki.

### `grafana/provisioning/datasources/prometheus.yml` — Prometheus als Datenquelle

Registriert Prometheus automatisch als Datenquelle in Grafana. Ohne diese Datei müsste Prometheus manuell in der Grafana-Oberfläche hinzugefügt werden.

### `grafana/provisioning/datasources/loki.yml` — Loki als Datenquelle

Registriert Loki automatisch als Datenquelle in Grafana. Ermöglicht das Anzeigen von Logs unter **Explore** → **Loki**.

### `grafana/provisioning/dashboards/monitoring.yml` — Dashboard automatisch laden

Sagt Grafana, dass es die `monitoring.json` automatisch beim Start laden soll. Das Dashboard erscheint dann unter **Dashboards** → **Browse**.

### `grafana/provisioning/alerting/rules.yml` — Alert-Regeln

Definiert die 4 Alert-Regeln: CPU Warning (>80%), Memory Warning (>85% Heap), Error Rate Critical (>5% Fehler), App Down Critical (nicht erreichbar). Alle zeigen "Normal" bei fehlenden Daten (`noDataState: OK`).

### Zusammenfassung

| Datei | Service | Aufgabe |
|-------|---------|---------|
| `docker-compose.yml` | Alle | Container definieren, starten, vernetzen |
| `prometheus/prometheus.yml` | Prometheus | Wo sind die Apps zum Scrapen? |
| `loki-config.yml` | Loki | Wie Logs speichern? |
| `promtail-config.yml` | Promtail | Welche Container-Logs sammeln? |
| `grafana/.../datasources/prometheus.yml` | Grafana | Prometheus als Datenquelle registrieren |
| `grafana/.../datasources/loki.yml` | Grafana | Loki als Datenquelle registrieren |
| `grafana/.../dashboards/monitoring.yml` | Grafana | Dashboard automatisch laden |
| `grafana/.../alerting/rules.yml` | Grafana | Alert-Regeln definieren |
