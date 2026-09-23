# Cyber-Shield

CyberShield is an educational and demonstration-oriented cybersecurity application that simulates network attacks, detects threats in real time, and visualizes traffic through an interactive JavaFX dashboard. The project combines core security concepts: intrusion detection (IDS), firewall rules, threat intelligence, authentication, and multi-threaded packet processing.

## Features

- **Real-time dashboard (JavaFX)** — live charts, incident table, traffic sparkline, top source IPs, and per-incident detail windows
- **Traffic sources** — built-in traffic generators (benign, suspect, malicious) for demos, plus live packet capture via pcap4j
- **Packet processing** — packet decoding (TCP flags, protocols), flow tracking (`FlowTable`), and per-host state tracking
- **Detection engines**
  - SYN flood detector
  - Port scan detector
  - Signature engine driven by `src/main/resources/config/payload-signatures.json` (HTTP admin probes, WordPress login probes, PHPUnit RCE probes, SQL injection patterns, ...)
- **Threat intelligence**
  - [AbuseIPDB](https://www.abuseipdb.com/) REST API integration — abuse confidence score, attack categories, ISP/country info shown in the incident details window
  - Emerging Threats IP blocklist downloader (`threat-intelligence/`)
- **Persistence (PostgreSQL)** — users, incidents, attack types, and firewall rules; pcap file storage for captured traffic
- **Authentication** — login/registration with bcrypt password hashing, user roles, and login-attempt tracking
- **UDP alert channel** — alert client/server for pushing detections to a remote listener
- **Internationalization** — English and Croatian (`messages_en.properties`, `messages_hr.properties`)
- **Logging** — SLF4J + Logback (`logback.xml`)

## Tech stack

| Area | Technology |
|---|---|
| Language / runtime | Java 21 |
| UI | JavaFX 21 (FXML, ControlsFX, FontAwesomeFX) |
| Build | Maven (wrapper included) |
| Database | PostgreSQL (JDBC) |
| Packet capture | pcap4j |
| Serialization | Gson, Jackson, SnakeYAML |
| Security | jBCrypt / favre bcrypt |
| Logging | SLF4J + Logback |

## Getting started

### Prerequisites

- JDK 21+
- PostgreSQL running locally (default: `jdbc:postgresql://localhost:5432/cyber_db`)
- For live capture (optional): libpcap (Linux/macOS) or Npcap (Windows)

### 1. Clone

```bash
git clone https://github.com/Cvijo-Jankelic/Cyber-Shield.git
cd Cyber-Shield
```

### 2. Configure the database

Create `database-properties/database.properties` (the folder is gitignored — credentials never go into the repo):

```properties
databaseUrl=jdbc:postgresql://localhost:5432/cyber_db
db.username=your_username
db.password=your_password
```

The application uses the tables `users`, `incidents`, `attack_types`, and `firewall_rules`. Apply the SQL migrations from the `sql/` folder to bring an existing schema up to date:

```bash
psql -d cyber_db -f sql/2026-09-23_add_email_and_pcap_data.sql
```

### 3. Configure the AbuseIPDB API key

Threat-intelligence lookups need an [AbuseIPDB API key](https://www.abuseipdb.com/account/api) (free tier is enough). Provide it **one** of two ways:

- environment variable:

  ```bash
  export ABUSEIPDB_API_KEY=your_api_key
  ```

- or a local properties file `api-properties/api.properties` (the folder is gitignored):

  ```properties
  abuseipdb.api.key=your_api_key
  ```

> **Never commit API keys or passwords.** Both config folders are listed in `.gitignore` on purpose; without a key the app still runs, only IP reputation lookups are disabled.

### 4. Run

```bash
./mvnw clean javafx:run
```

## Project structure

```
src/main/java/com/project/cybershield/
├── api/          AbuseIPDB REST client + threat intelligence report model
├── auth/         Password hashing (bcrypt)
├── controllers/  JavaFX controllers (dashboard, login, registration)
├── db/           Database connection configuration
├── decode/       Packet decoding (TCP flags, protocols)
├── detect/       SYN flood & port scan detectors
├── entities/     Domain model (Incident, User, FirewallRule, AttackType, ...)
├── flow/         Flow tracking (flow keys, flow table)
├── host/         Per-host state tracking
├── ids/          IDS engine + signature matching
├── network/      Traffic generators & packet sources (simulated + live)
├── pcapStorage/  Pcap file persistence
├── repository/   PostgreSQL repositories
├── server/       UDP alert server
├── client/       UDP alert client
├── services/     Login/registration services, attempt tracking
├── threads/      IDS worker threads
├── ui/           Incident details window, progress popups
└── util/         Threat feed downloader, locale manager, navigation
```

## Security notes

- Secrets (DB credentials, API keys) live **outside** the repository in gitignored folders (`database-properties/`, `api-properties/`) or environment variables.
- Passwords are stored as bcrypt hashes, never in plain text.
- This is an educational project — the simulated attacks and signatures are for learning and demonstration purposes only.
