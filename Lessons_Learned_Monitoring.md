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
Spring Boot App (Port 8080)
        |
        |  /actuator/prometheus liefert Metriken
        v
+---------------------+
|     Prometheus      |
|   (Port 9090)       |
|                     |
|  - Scraping alle 15s|
|  - Zeitreihenspeicher|
|  - PromQL-Abfragen   |
+----------+----------+
           |
           |  Speichert Daten für Grafana
           v
+---------------------+
|      Grafana         |
|   (Port 3000)        |
|                      |
|  - Dashboards        |
|  - Alert-Status      |
|  - Interaktive UI    |
+----------+-----------+
           |
           |  Browser-Zugriff
           v
       Browser
```

1. Spring Boot App stellt `/actuator/prometheus` bereit
2. Prometheus fragt alle 15 Sekunden die Metriken ab und speichert sie
3. Grafana liest auf Anfrage Daten von Prometheus und rendert das Dashboard
4. Zugriff über `http://localhost:3000` im Browser

## Zwei Betriebsmodi

### Vollständiges Docker-Deploy (Empfohlen)

Alle drei Services — App, Prometheus und Grafana — laufen in Docker-Containern im selben Netzwerk. Prometheus erreicht die App über den Container-Namen `app:8080`.

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
| Spring Boot App | http://localhost:8080 | kein Login |
| Metriken | http://localhost:8080/actuator/prometheus | Rohdaten |

## Schnellstart

1. Docker Desktop starten
2. `docker compose up -d` im Projektordner ausführen
3. `http://localhost:3000` öffnen, Dashboard auswählen
