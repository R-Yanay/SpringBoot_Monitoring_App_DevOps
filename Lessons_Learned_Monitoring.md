# Lessons Learned: Monitoring mit Docker, Prometheus und Grafana

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

## Datenfluss

```
Spring Boot App (localhost:8080)
        |
        |  /actuator/prometheus liefert Metriken
        v
+---------------------+
|     Prometheus      |
|   (localhost:9090)  |
|                     |
|  - Scraping alle 15s|
|  - Zeitreihenspeiche|
|  - PromQL-Abfragen   |
+----------+----------+
           |
           |  Speichert Daten für Grafana
           v
+---------------------+
|      Grafana         |
|   (localhost:3000)   |
|                      |
|  - Dashboards        |
|  - Alert-Status      |
|  - Interaktive UI    |
+----------+-----------+
           |
           |  Browser-Zugriff
           v
      Dein Browser
```

1. Spring Boot App läuft lokal auf Port 8080 und stellt `/actuator/prometheus` bereit
2. Prometheus fragt alle 15 Sekunden die Metriken ab und speichert sie
3. Grafana liest auf Anfrage Daten von Prometheus und rendert das Dashboard
4. Du greifst über `http://localhost:3000` auf Grafana zu

IntelliJ startet nur die Java-App. Prometheus und Grafana laufen unabhängig in Docker-Containern.

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
