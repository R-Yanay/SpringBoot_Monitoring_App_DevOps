# Spring Boot Monitoring App mit Prometheus und Grafana

Eine Spring Boot-Anwendung mit integriertem Monitoring durch Prometheus und Grafana. Dieses Projekt demonstriert die **DevOps-Praxis** von Code über Tests bis hin zu Monitoring und Docker-Deploy.

---

## Architektur

```
┌──────────────────────────────────────────────────────────────┐
│  DEIN COMPUTER                                               │
│                                                              │
│  ┌──────────────┐   ┌────────────────────────────────────┐  │
│  │ IntelliJ IDEA│   │  Docker Desktop (Container-Box)    │  │
│  │ (dein Editor) │   │                                    │  │
│  │              │   │  ┌────────────┐  ┌──────────────┐  │  │
│  │ - Schreibst  │   │  │Prometheus  │  │   Grafana    │  │  │
│  │   Java-Code  │   │  │(sammelt    │  │  (zeigt      │  │  │
│  │ - Startest   │   │  │  die Daten)│  │   Grafiken)  │  │  │
│  │   die App    │   │  └─────┬──────┘  └──────┬───────┘  │  │
│  └──────────────┘   └────────┼────────────────┼──────────┘  │
│         │                    │                 │            │
│         │ App läuft hier     │ Fragt ab        │ Liest von  │
│         ▼ Port 8080          ▼                ▼            │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  Deine Spring Boot App (läuft direkt auf dem Computer)  ││
│  │  - Liefert Metriken an /actuator/prometheus             ││
│  └─────────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────────┘

Datenfluss:

  Spring Boot App (Port 8080)
        │
        │  /actuator/prometheus liefert Metriken
        ▼
  Prometheus (Port 9090)
        │
        │  Sammelt alle 15 Sekunden
        ▼
  Grafana (Port 3000)
        │
        │  Zeigt Dashboard im Browser
        ▼
  Du siehst die Grafiken!
```

## Projektstruktur

```
SpringBoot_Monitoring_APP_DevOps/
├── src/
│   ├── main/
│   │   ├── java/com/monitoring/
│   │   │   ├── MonitoringApplication.java    # Hauptklasse
│   │   │   ├── controller/
│   │   │   │   └── DemoController.java       # 4 Demo-Endpunkte
│   │   │   └── config/
│   │   │       └── MetricsConfig.java        # Custom Metrics
│   │   └── resources/
│   │       ├── application.properties        # Actuator-Konfiguration
│   │       └── static/
│   │           └── index.html                # Startseite mit Endpunkte-Übersicht
│   └── test/
│       └── java/com/monitoring/
│           ├── MonitoringApplicationTests.java
│           ├── controller/
│           │   └── DemoControllerTest.java
│           └── config/
│               └── ActuatorEndpointTest.java
├── docker-compose.yml          # Docker Compose: App + Prometheus + Grafana
├── Dockerfile                  # Multi-Stage Build für Spring Boot App
├── prometheus/
│   └── prometheus.yml          # Prometheus Scraping-Konfiguration
├── grafana/
│   ├── dashboards/
│   │   └── monitoring.json     # Grafana Dashboard (8 Panels)
│   └── provisioning/
│       ├── dashboards/
│       │   └── monitoring.yml  # Auto-Import des Dashboards
│       └── datasources/
│           └── prometheus.yml  # Auto-Import von Prometheus als DataSource
├── .gitignore                  # Ignorierte Dateien (target/, .junie/, etc.)
└── Lessons_Learned_Monitoring.md # Aus dieser Sitzung
```

## Features

### Demo-Endpunkte

| Endpunkt | Methode | Beschreibung |
|----------|---------|-------------|
| `/api/demo/hello` | GET | Liefert "Hallo" und Visit-Count |
| `/api/demo/slow` | GET | Simuliert langsame Antwort (~1 Sekunde) |
| `/api/demo/work` | POST | Simuliert rechenintensive Arbeit |
| `/api/demo/error` | GET | Wirft absichtlich eine RuntimeException |

### Custom Metrics

Die Anwendung registriert benutzerdefinierte Metriken in `MetricsConfig.java`:

#### Per-URL Counter (neu)

Jeder Demo-Endpunkt hat einen eigenen Counter, um Aufrufe separat zu verfolgen:

| Metrik | Typ | Endpunkt |
|--------|-----|----------|
| `app.hello.count` | Gauge | `GET /api/demo/hello` |
| `app.slow.count` | Gauge | `GET /api/demo/slow` |
| `app.work.count` | Gauge | `POST /api/demo/work` |
| `app.error.count` | Gauge | `GET /api/demo/error` |

#### Legacy Metrics

| Metrik | Typ | Beschreibung |
|--------|-----|-------------|
| `app.custom.events` | Counter | Zählt benutzerdefinierte Ereignisse (gesamt) |
| `app.custom.gauge` | Gauge | Misst einen aktuellen Wert |
| `app.custom.timer` | Timer | Misst die Dauer von Operationen |

### Monitoring-Dashboard (Grafana)

Das Dashboard `monitoring.json` enthält 8 Panels:

| Panel | Metrik | Was zeigt es? |
|-------|--------|--------------|
| **CPU Usage** | `system_cpu_usage` | Prozessor-Auslastung der App |
| **JVM Memory (Heap)** | `jvm_memory_used_bytes` | Heap-Speicherverbrauch |
| **JVM Memory (Non-Heap)** | `jvm_memory_used_bytes{area="nonheap"}` | Non-Heap-Speicher |
| **HTTP Request Rate** | `rate(http_server_requests_seconds_count[5m])` | Anfragen pro Sekunde |
| **HTTP Response Time** | `rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])` | Durchschnittliche Antwortzeit |
| **Active Threads** | `jvm_threads_live` | Aktive JVM-Threads |
| **GC Pause Time** | `rate(jvm_gc_pause_seconds_sum[5m])` | Garbage Collection Pausen |
| **Custom Metrics** | `app.*` | Deine eigenen Metriken |

## Schnellstart

### 1. Projekt klonen

```bash
git clone https://github.com/R-Yanay/SpringBoot_Monitoring_App_DevOps.git
cd SpringBoot_Monitoring_App_DevOps
```

### 2. Anwendung starten

**In IntelliJ IDEA:**
1. Projekt öffnen
2. `MonitoringApplication.java` öffnen
3. Grünen ▶️ "Run"-Knopf klicken

**Über Terminal:**
```bash
mvn spring-boot:run
```

Die App läuft dann auf `http://localhost:8080`.

### 3. Monitoring starten

Stelle sicher, dass **Docker Desktop** läuft.

```powershell
docker compose up -d prometheus grafana
```

### 4. Dashboard öffnen

1. Öffne `http://localhost:3000` im Browser
2. Melde dich an: **admin** / **admin**
3. Klicke links auf **Dashboards** → **Browse** → **Spring Boot Monitoring**

## Tests

Das Projekt enthält 8 Tests in 3 Test-Klassen, die die **TEST-Phase** des DevOps-Kreislaufs abdecken.

### Test-Übersicht

| Test-Klasse | Anzahl | Was wird getestet? |
|-------------|--------|-------------------|
| `MonitoringApplicationTests` | 1 | Spring Boot App startet ohne Fehler |
| `DemoControllerTest` | 4 | Alle 4 Demo-Endpunkte (`/hello`, `/slow`, `/work`, `/error`) |
| `ActuatorEndpointTest` | 3 | Actuator-Endpunkte (`/health`, `/metrics`, `/prometheus`) |

### Tests mit Maven ausführen

```bash
mvn test
```

Erwartetes Ergebnis: **8/8 Tests bestanden** → `BUILD SUCCESS`

### Tests in IntelliJ IDEA ausführen

**Alle Tests gleichzeitig:**
1. Öffne die **Project**-Ansicht links in IntelliJ
2. Navigiere zu `src/test/java/com/monitoring/`
3. Rechte Maustaste auf den Ordner `com.monitoring` → **Run 'All Tests'** (`Shift + F10`)
4. Das Test-Runner-Fenster unten zeigt alle 8 Tests mit grünen Haken ✅

**Eine Test-Klasse einzeln:**
1. Öffne z. B. `DemoControllerTest.java`
2. Klicke auf den grünen Pfeil ▶️ neben `class DemoControllerTest`
3. Wähle **Run 'DemoControllerTest'**

**Falls die grünen Pfeile fehlen:**
- Öffne die `pom.xml` und klicke auf 🔄 **Reload All Maven Projects**
- Dann erkennt IntelliJ die Tests automatisch

---

## DevOps-Fortschritt

| Phase | Status | Beschreibung |
|-------|--------|-------------|
| **CODE** | ✅ | Spring Boot App mit Java-Code in IntelliJ |
| **BUILD** | ⚠️ | Maven kompiliert, Dockerfile vorhanden — CI/CD fehlt noch |
| **TEST** | ✅ | 8 Tests in 3 Klassen, alle bestanden |
| **DEPLOY** | ⚠️ | Docker Compose manuell — automatisches Deploy fehlt noch |
| **OPERATE** | ⚠️ | Docker hält Container am Laufen — Alerting fehlt noch |
| **MONITOR** | ✅ | Prometheus + Grafana sammeln und zeigen Metriken |

### Abgeschlossene Aufgaben

- [x] Spring Boot Actuator mit Custom Metrics konfigurieren
- [x] Docker Compose mit Prometheus und Grafana einrichten
- [x] Grafana-Dashboard mit vorkonfigurierten Panels erstellen
- [x] Startseite (index.html) mit Endpunkte-Übersicht hinzufügen
- [x] Unit- und Integration-Tests schreiben (8 Tests)
- [x] Per-URL Counter für Custom Metrics implementieren
- [x] Projekt nach `ProjectIdeas` umziehen

### Offene Aufgaben

- [ ] GitHub Actions CI/CD Pipeline einrichten (Schritt 2)
- [ ] Grafana Alerting konfigurieren (Schritt 3)
- [ ] Vollständiges Docker-Deploy testen (alle 3 Services) (Schritt 4)

---

## Autor

Dieses Projekt wurde als DevOps-Lernprojekt erstellt.
