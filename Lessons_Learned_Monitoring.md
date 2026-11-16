# Lessons Learned: Spring Boot Monitoring mit Docker, Prometheus & Grafana

## Die drei Programme auf einen Blick

| Programm | Rolle | Analogie |
|----------|-------|----------|
| **Docker Desktop** | Bietet isolierte Container-Boxen für Prometheus und Grafana | Die Garage, in der die Werkzeuge stehen |
| **Prometheus** | Fragt alle 15 Sekunden die App ab und speichert die Daten | Der Mechaniker, der misst |
| **Grafana** | Liest die Daten von Prometheus und zeigt sie als Grafiken | Das Armaturenbrett im Auto |

---

## So funktioniert der Datenfluss

```
┌────────────────────────────────────────────────────────────────┐
│  DEIN COMPUTER                                                 │
│                                                                │
│  ┌──────────────┐   ┌──────────────────────────────────────┐  │
│  │ IntelliJ IDEA│   │  Docker Desktop (Container-Box)      │  │
│  │ (Code Editor) │   │                                      │  │
│  │              │   │  ┌────────────┐  ┌────────────────┐  │  │
│  │ - Schreibst  │   │  │ Prometheus │  │   Grafana      │  │  │
│  │   Java-Code  │   │  │ (sammelt   │  │   (zeigt       │  │  │
│  │ - Startest   │   │  │  die Daten)│  │   Grafiken)    │  │  │
│  │   die App    │   │  └─────┬──────┘  └──────┬─────────┘  │  │
│  └──────────────┘   └────────┼────────────────┼────────────┘  │
│         │                    │                 │              │
│         │ App läuft hier     │ Fragt ab        │ Liest von    │
│         ▼ Port 8080          ▼                ▼              │
│  ┌───────────────────────────────────────────────────────────┐│
│  │  Deine Spring Boot App (läuft lokal auf dem Computer)     ││
│  │  - Liefert Metriken an /actuator/prometheus               ││
│  └───────────────────────────────────────────────────────────┘│
└────────────────────────────────────────────────────────────────┘
```

### Schritt für Schritt

1. **Du schreibst Code** in IntelliJ IDEA
2. **IntelliJ startet** deine Spring Boot App → läuft auf `localhost:8080`
3. **Prometheus** (in Docker) fragt alle 15 Sekunden `http://host.docker.internal:8080/actuator/prometheus` ab
4. **Deine App antwortet** mit Zahlen (CPU, Speicher, HTTP-Anfragen, etc.)
5. **Prometheus speichert** alle Zahlen in seiner Datenbank
6. **Grafana** (in Docker) liest die Zahlen von Prometheus
7. **Du siehst** im Browser unter `localhost:3000` die live Grafiken

---

## Wichtige Lektionen

### 1. `host.docker.internal` statt `app:8080`
- **Problem:** Prometheus suchte die App unter `app:8080` (Docker-Container-Name), aber die App läuft lokal.
- **Lösung:** In `prometheus.yml`.targets auf `host.docker.internal:8080` ändern.
- **Warum:** `host.docker.internal` ist die spezielle Adresse, über die ein Docker-Container auf den Host-Computer zugreifen kann.

### 2. Docker Desktop muss laufen
- Ohne Docker Desktop können Prometheus und Grafana nicht starten.
- Status prüfen: Docker-Icon in der Taskleiste muss **"Docker Desktop Running"** anzeigen.

### 3. Container starten
```powershell
cd C:\Users\roiya\IdeaProjects\ProjectIdeas\SpringBoot_Monitoring_APP_DevOps
docker compose up -d prometheus grafana
```

### 4. HTTP-Metriken brauchen Zeit
- **CPU, JVM Memory** → sofort sichtbar (Momentaufnahme/Gauge)
- **HTTP Request Rate, Response Time** → brauchen 5–10 Minuten Traffic, weil `rate()[5m]` einen Durchschnitt berechnet

### 5. IntelliJ hat keine Verbindung zu Docker/Prometheus/Grafana
- IntelliJ startet nur die Java-App
- Der Rest läuft komplett automatisch und unabhängig

---

## Container aktualisieren nach Änderungen

Ein Docker-Container hat beim Start eine **Kopie** deiner Dateien. Änderungen im Projektordner sieht der Container **nicht automatisch**.

### Fall 1: Nur `monitoring.json` geändert (Dashboard-Grafiken)

**Schritt 1:** Datei in den Container kopieren
```powershell
cd C:\Users\roiya\IdeaProjects\ProjectIdeas\SpringBoot_Monitoring_APP_DevOps
docker cp grafana\dashboards\monitoring.json grafana:/var/lib/grafana/dashboards/monitoring.json
```
> **`docker cp`** = "copy" — kopiert eine Datei von deinem Computer in den laufenden Container.

**Schritt 2:** Grafana neu starten
```powershell
docker compose restart grafana
```
> **`docker compose restart`** = startet den Container neu, damit er die geänderte Datei lädt.

### Fall 2: `prometheus.yml` oder `docker-compose.yml` geändert

```powershell
cd C:\Users\roiya\IdeaProjects\ProjectIdeas\SpringBoot_Monitoring_APP_DevOps
docker compose down
docker compose up -d
```
> **`docker compose down`** = stoppt und löscht alle Container.
> **`docker compose up -d`** = startet **neue** Container mit der aktuellen Konfiguration.

### Fall 3: Nur Java-Code geändert

> **Kein Docker-Befehl nötig!** Die App in IntelliJ stoppen und neu starten.

### Schnellübersicht

| Was hast du geändert? | Befehl |
|----------------------|--------|
| `monitoring.json` (Dashboard) | `docker cp ...` → `docker compose restart grafana` |
| `prometheus.yml` (Prometheus) | `docker compose down` → `docker compose up -d` |
| `docker-compose.yml` | `docker compose down` → `docker compose up -d` |
| Java-Code (.java-Dateien) | App in IntelliJ neu starten |

---

## Nützliche URLs

| Service | URL | Login |
|---------|-----|-------|
| **Grafana** | `http://localhost:3000` | admin / admin |
| **Prometheus** | `http://localhost:9090` | kein Login |
| **Spring Boot App** | `http://localhost:8080` | kein Login |
| **Metriken-Endpoint** | `http://localhost:8080/actuator/prometheus` | Rohdaten |

---

## Schnellstart-Zusammenfassung

1. ✅ Docker Desktop starten
2. ✅ `docker compose up -d prometheus grafana` ausführen
3. ✅ Spring Boot App in IntelliJ starten
4. ✅ Browser öffnen → `localhost:3000` → Dashboard genießen
