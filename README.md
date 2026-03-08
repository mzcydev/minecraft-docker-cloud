# minecraft-docker-cloud

A self-hosted Minecraft cloud system written in Java 21. Manages groups of Minecraft server instances (Paper, Velocity, BungeeCord, Fabric) across one or more nodes using Docker containers, gRPC communication, and a REST API.

---

## Table of Contents

- [Architecture](#architecture)
- [Requirements](#requirements)
- [Building](#building)
- [Running](#running)
  - [Master](#master)
  - [Node](#node)
- [Configuration](#configuration)
  - [master.yml](#masteryml)
  - [node.yml](#nodeyml)
- [REST API](#rest-api)
- [Module Overview (for Developers)](#module-overview-for-developers)
- [Developer Setup](#developer-setup)
- [Project Structure](#project-structure)

---

## Architecture

```
┌─────────────────────────────────────────────┐
│                  Master                     │
│  GroupManager · ServiceScaler · REST API    │
│  gRPC Server (port 9090)                    │
└────────────┬────────────────────────────────┘
             │ gRPC
    ┌────────┴────────┐
    │                 │
┌───▼───┐         ┌───▼───┐
│ Node  │         │ Node  │   ...
│Docker │         │Docker │
└───────┘         └───────┘
```

- The **Master** is the central controller. It stores group configurations, schedules services, exposes a REST API, and communicates with nodes over gRPC.
- Each **Node** runs Docker containers. It receives start/stop commands from the master and reports state and player counts back.
- **Services** are individual Minecraft server containers (e.g. `Lobby-1`, `BedWars-3`).
- **Groups** are blueprints that define how services are created (image, memory, min/max count, template, etc.).

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Java (JDK)  | 21+ (Amazon Corretto, Eclipse Temurin, or OpenJDK) |
| Gradle      | 8.10 (via wrapper — no installation needed) |
| Docker      | 20+ (required on each Node machine) |
| OS          | Windows, Linux, or macOS |

> **Windows users:** Use `gradlew.bat` instead of `./gradlew` in all commands below.

---

## Building

Clone or extract the project, then run from the project root:

```bash
# Build all modules
./gradlew build

# Build only specific modules (faster during development)
./gradlew :cloud-master:build
./gradlew :cloud-node:build
```

The build produces fat JARs (shadow JARs) in:

```
cloud-master/build/libs/cloud-master-1.0.0-SNAPSHOT.jar
cloud-node/build/libs/cloud-node-1.0.0-SNAPSHOT.jar
```

### First-time build note

On the first build, Gradle will download all dependencies and the protobuf compiler. This requires internet access and may take a few minutes. Subsequent builds are cached and much faster.

### Compile only (no tests, faster)

```bash
./gradlew :cloud-master:shadowJar
./gradlew :cloud-node:shadowJar
```

---

## Running

### Master

1. Copy `cloud-master/build/libs/cloud-master-1.0.0-SNAPSHOT.jar` to your server.
2. Run it once to generate the default config:

```bash
java -jar cloud-master-1.0.0-SNAPSHOT.jar
```

3. Edit the generated `master.yml` (see [Configuration](#masteryml)).
4. Start again:

```bash
java -jar cloud-master-1.0.0-SNAPSHOT.jar
```

### Node

1. Copy `cloud-node/build/libs/cloud-node-1.0.0-SNAPSHOT.jar` to each node machine.
2. Make sure Docker is running on that machine.
3. Run once to generate the default config:

```bash
java -jar cloud-node-1.0.0-SNAPSHOT.jar
```

4. Edit `node.yml` — set `master.host`, `master.authToken`, and the node's own `host` and `name`.
5. Start again:

```bash
java -jar cloud-node-1.0.0-SNAPSHOT.jar
```

The node will automatically connect to the master on startup.

---

## Configuration

### master.yml

Generated automatically on first run in the working directory.

```yaml
host: 0.0.0.0
grpcPort: 9090        # Port nodes connect to
restPort: 8080        # Port for the REST API
authToken: change-me  # Shared secret — must match all nodes
templateDir: templates/
maxNodes: 10
serviceStartTimeoutSeconds: 30
```

### node.yml

```yaml
name: node-1          # Unique name for this node
host: 192.168.1.10    # This node's IP (reachable by master)
grpcPort: 9091        # Port this node's gRPC server listens on

master:
  host: 192.168.1.1   # Master's IP or hostname
  port: 9090
  authToken: change-me  # Must match master.yml

docker:
  host: unix:///var/run/docker.sock   # or tcp://localhost:2375 on Windows

ports:
  from: 30000         # Port range allocated to Minecraft services
  to: 31000

resources:
  maxMemoryMb: 8192   # Total RAM this node offers to the cloud
  maxServices: 20     # Maximum concurrent services on this node

directories:
  templateCache: templates/   # Where template files are cached locally
  serviceWork: services/      # Where running service directories are created
```

---

## Console Commands (Master)

Once the master is running, you can type commands directly in the console:

```
group list                          - List all groups
group create <name> <type>          - Create a group (types: PAPER, VELOCITY, BUNGEECORD, FABRIC)
group info <name>                   - Show group details
group delete <name>                 - Delete a group

service list                        - List all running services
service info <name>                 - Show service details
service stop <name>                 - Stop a service

node list                           - List connected nodes
node info <name>                    - Show node details

player list                         - List online players
player info <name>                  - Show player details

clear                               - Clear the console
stop                                - Shut down the master
```

---

## REST API

The REST API is available at `http://<master-host>:<restPort>/api`.

All endpoints require a Bearer token header:
```
Authorization: Bearer <authToken>
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/groups` | List all groups |
| GET | `/api/groups/:name` | Get a group |
| POST | `/api/groups` | Create a group |
| PUT | `/api/groups/:name` | Update a group |
| DELETE | `/api/groups/:name` | Delete a group |
| GET | `/api/nodes` | List all nodes |
| GET | `/api/nodes/:name` | Get a node |
| GET | `/api/services` | List all services |
| GET | `/api/services/:id` | Get a service by ID |
| POST | `/api/services/:group/start` | Start a service for a group |
| DELETE | `/api/services/:id` | Stop a service |
| GET | `/api/players` | List all online players |
| GET | `/api/players/:uuid` | Get a player by UUID |

---

## Module Overview (for Developers)

| Module | Description |
|--------|-------------|
| `cloud-api` | Shared interfaces and model classes (`CloudService`, `ServiceGroup`, `CloudNode`, `CloudPlayer`, events) |
| `cloud-proto` | Protobuf `.proto` definitions + generated gRPC stubs |
| `cloud-networking` | gRPC server/client wrappers for all services (node, service, group, player, template) |
| `cloud-docker` | Docker Java SDK wrappers (`ContainerManager`, `PortAllocator`, `ImageManager`, etc.) |
| `cloud-template` | Template management and syncing (local, FTP, S3 storage backends) |
| `cloud-master` | Master process: group/node/service/player managers, console, REST API, service scaler |
| `cloud-node` | Node process: Docker service lifecycle, heartbeat, template receiver |
| `cloud-plugin-api` | Plugin API for Paper and Velocity plugins to connect to the cloud |
| `cloud-rest` | Javalin REST server with controllers, DTOs, and mappers |

### Key design decisions

- **gRPC** is used for all node↔master communication. The master acts as a gRPC server for nodes registering and heartbeating. Nodes also expose a gRPC server so the master can send start/stop commands back.
- **ServiceGroup** is the central configuration object. It defines platform type, memory limit, min/max service count, template name, and JVM flags.
- **ServiceScaler** runs every 10 seconds on the master and starts/stops services to maintain the configured `minServices`/`maxServices` count per group.
- **Templates** are directories of files copied into each service container before it starts. They can be stored locally, on FTP, or in S3.
- **Authentication** is token-based: every gRPC call and REST request must include the shared `authToken`.

---

## Developer Setup

### Recommended IDE: IntelliJ IDEA

1. Open IntelliJ → `File > Open` → select `settings.gradle.kts`
2. IntelliJ will auto-import the Gradle project
3. Wait for indexing and dependency download to finish
4. Run/debug configurations:
   - **Master:** main class `dev.cloud.master.MasterApplication`
   - **Node:** main class `dev.cloud.node.NodeApplication`

### Regenerating protobuf sources

Protobuf Java classes are generated automatically during `compileJava`. To regenerate manually:

```bash
./gradlew :cloud-proto:generateProto
```

Generated sources are placed in `cloud-proto/build/generated/source/proto/`.

### Adding a new group via REST (example)

```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Authorization: Bearer change-me" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lobby",
    "type": "PAPER",
    "templateName": "lobby",
    "memory": 512,
    "maxPlayers": 100,
    "minServices": 1,
    "maxServices": 5,
    "jvmFlags": "-Xmx512M -Xms512M",
    "static": false
  }'
```

### Module dependency graph

```
cloud-api
   └── cloud-proto
         └── cloud-networking
               ├── cloud-master
               │     └── cloud-rest
               ├── cloud-node
               │     ├── cloud-docker
               │     └── cloud-template
               └── cloud-plugin-api
```

---

## Project Structure

```
minecraft-cloud/
├── build.gradle.kts          Root build config (Java 21, shared deps)
├── settings.gradle.kts       Module declarations
├── cloud-api/                Shared API interfaces and models
├── cloud-proto/              Protobuf definitions
│   └── src/main/proto/
│       ├── common.proto
│       ├── node.proto
│       ├── service.proto
│       ├── group.proto
│       ├── player.proto
│       └── template.proto
├── cloud-networking/         gRPC client/server wrappers
├── cloud-docker/             Docker integration
├── cloud-template/           Template storage and sync
├── cloud-master/             Master process (fat JAR)
├── cloud-node/               Node process (fat JAR)
├── cloud-rest/               REST API (embedded in master)
└── cloud-plugin-api/         Minecraft plugin integration
```
