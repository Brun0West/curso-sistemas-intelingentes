# Tarea SMA JADE - Sistema Multi-Agente Distribuido

Proyecto académico implementado con **JADE + Maven + Docker** para demostrar coordinación distribuida entre dos plataformas de agentes.

## 1) Lógica de negocio implementada

Se implementó un sistema distribuido de gestión de pedidos de restaurantes con colaboración real entre agentes:

- **P1 (Plataforma 1)**
  - `restaurante@P1`: ofrece cotizaciones de menú mediante protocolo Contract Net.
  - `logistica@P1`: calcula ETA y costo de entrega.
  - `supervisor@P1`: monitorea eventos y genera reporte periódico.
- **P2 (Plataforma 2)**
  - `pagos@P2`: valida pagos y confirma/rechaza transacciones.
  - `cliente@P2`: inicia pedidos, selecciona propuesta y coordina flujo extremo a extremo.

### Comunicación y estándares

- Registro y descubrimiento de servicios por **DF (Directory Facilitator)**.
- Intercambio de mensajes **FIPA-ACL** con performativas: `CFP`, `PROPOSE`, `ACCEPT_PROPOSAL`, `REQUEST`, `INFORM`, `CONFIRM`, `FAILURE`.
- Comunicación interplataforma usando **HTTP MTP** (`jade.mtp.http.MessageTransportProtocol`).

## 2) Estructura del repositorio

```text
.
├── src/main/java/edu/sma
│   ├── agents
│   │   ├── ClientAgent.java
│   │   ├── LogisticsAgent.java
│   │   ├── PaymentAgent.java
│   │   ├── RestaurantAgent.java
│   │   └── SupervisorAgent.java
│   ├── behaviours
│   │   ├── OrderCycleBehaviour.java
│   │   ├── RestaurantContractNetResponder.java
│   │   └── SupervisorMonitorBehaviour.java
│   └── common
│       ├── DfUtils.java
│       ├── PayloadCodec.java
│       ├── RemoteAidFactory.java
│       └── ServiceNames.java
├── docs
│   ├── informe.md
│   ├── guion-video-youtube.md
│   └── capturas/
├── scripts
│   ├── start-p1-main.ps1
│   ├── start-p1-c1.ps1
│   ├── start-p1-c2.ps1
│   ├── start-p2-main.ps1
│   └── start-p2-c1.ps1
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── .gitignore
```

## 3) Requisitos

- Java 17+
- Maven 3.9+
- Docker Desktop + Docker Compose
- Windows PowerShell (`pwsh`) para scripts locales

## 4) Ejecución con Docker (entorno distribuido recomendado)

```bash
docker compose build
docker compose up
```

Para detener:

```bash
docker compose down
```

## 5) Ejecución local con GUI JADE (para capturas del informe)

Abrir 5 terminales en la raíz del proyecto y ejecutar:

```powershell
./scripts/start-p1-main.ps1
```

```powershell
./scripts/start-p1-c1.ps1
```

```powershell
./scripts/start-p1-c2.ps1
```

```powershell
./scripts/start-p2-main.ps1
```

```powershell
./scripts/start-p2-c1.ps1
```

## 6) Flujo colaborativo esperado

1. `cliente@P2` envía `CFP` a `restaurante@P1`.
2. `restaurante@P1` responde `PROPOSE` (precio y tiempo de preparación).
3. `cliente@P2` acepta propuesta (`ACCEPT_PROPOSAL`).
4. `cliente@P2` solicita a `logistica@P1` (`REQUEST`) ETA/costo.
5. `logistica@P1` responde `INFORM`.
6. `cliente@P2` solicita validación a `pagos@P2` (`REQUEST`).
7. `pagos@P2` responde `CONFIRM` o `FAILURE`.
8. `cliente@P2` notifica a `restaurante@P1` y `supervisor@P1` (`INFORM`).

## 7) Entregables adicionales

- Informe académico completo: `docs/informe.md`
- Guion de video (≤10 min): `docs/guion-video-youtube.md`

Generar PDF del informe (coordinador):

```powershell
./scripts/export-informe-pdf.ps1
```

## 8) Publicación en GitHub

```bash
git init
git add .
git commit -m "Entrega Tarea SMA JADE distribuido"
git branch -M main
git remote add origin https://github.com/<usuario>/<repositorio>.git
git push -u origin main
```

## 9) Nota académica

El diseño está alineado con los requisitos mínimos: 2 plataformas, 5 agentes, registro DF, mensajes ACL y colaboración efectiva entre agentes distribuidos.
