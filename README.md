# Sistema Multi-Agente JADE - Gestión de Pedidos de Floristería

Proyecto académico que implementa un **sistema distribuido de gestión de pedidos de floristería** usando **JADE (Java Agent Development Framework)**, Maven y Docker. Demuestra la comunicación coordinada entre múltiples agentes en dos plataformas JADE distintas.

## 1. Descripción Técnica Detallada

### Visión General del Sistema

Plataforma JADE única distribuida en **dos contenedores Docker** (en la misma máquina Windows):

- **Main Container**: Aloja al **Recepcionista** y al **Florista**
- **Peripheral Container**: Aloja a los **tres Novios**

**Directory Facilitator (DF)**: Solo existe en el Main Container. Los agentes del contenedor peripheral se registran en él mediante el mecanismo estándar de JADE (usando `-host` y `-port`). El acceso al DF es transparente porque todos pertenecen a la misma plataforma.

**Comunicación**: Mensajes ACL con performativas FIPA estándar (QUERY-IF, REQUEST, INFORM, REFUSE, FAILURE).

### 2. Especificación de Agentes

#### 2.1. Novios (3 agentes)

**Parámetros de creación:**
- `nombre`: String (ej. "Carlos", "Miguel", "Javier")
- `orden`: Integer (1, 2 o 3) → determina el turno de atención

**Comportamiento:**

1. Al iniciar, el novio espera un tiempo aleatorio entre **5 y 8 segundos** (uniforme, en milisegundos).
2. Luego envía un mensaje `QUERY-IF` al recepcionista con contenido: `"miPedidoEstaListo? orden=X"`.
3. Después de enviar, vuelve a esperar el mismo rango aleatorio y repite el ciclo.
4. El ciclo se detiene cuando el novio recibe un `INFORM` del recepcionista indicando: `"Tu pedido está listo"`.
5. En ese momento, el novio responde con un mensaje `INFORM` de vuelta confirmando: `"ya voy a recogerlo"`.
6. Luego el novio termina su ejecución.

**Implementación JADE:**
- Atributos: `nombre`, `orden`, `pedidoListo` (booleano)
- `Setup()`: Registra el servicio en DF y busca el recepcionista
- `TickerBehaviour` (5-8s): Envía `QUERY-IF` mientras `pedidoListo == false`
- `CyclicBehaviour`: Recibe respuestas del recepcionista

#### 2.2. Recepcionista (1 agente)

**Parámetros**: Ninguno especial

**Comportamiento:**

1. Escucha continuamente mensajes entrantes.
2. **Si recibe `QUERY-IF` de un novio:**
   - Extrae la orden del contenido
   - Envía un mensaje `REQUEST` al florista: `"estaListo? orden=X"`
   - Espera la respuesta del florista (bloqueante, timeout 3s)
   - Si el florista responde `INFORM` con "SI": envía `INFORM` al novio: `"Tu pedido está listo"`
   - Si el florista responde `REFUSE` con "NO": envía `INFORM` al novio: `"Aún no está listo"`
   - Si timeout/FAILURE: reintenta o notifica al novio

3. **Si recibe `INFORM` del florista** con "Pedido listo para orden X":
   - Busca el AID del novio correspondiente (por orden)
   - Envía `INFORM`: `"Tu pedido está listo"`

4. **Si recibe `INFORM` del novio** con "ya voy a recogerlo":
   - Registra el evento en log

**Implementación JADE:**
- `Setup()`: Registra en DF y busca al florista
- Mantiene un mapa `orden → AID` del novio
- `CyclicBehaviour`: Atiende todos los tipos de mensajes

#### 2.3. Florista (1 agente)

**Parámetros**: Ninguno especial

**Atributos internos:**
- `siguienteOrdenPorAtender`: comienza en 1
- `tiempoTrabajoMin`: 10 segundos
- `tiempoTrabajoMax`: 15 segundos

**Comportamiento:**

1. **Atender `REQUEST` del recepcionista** (`CyclicBehaviour`):
   - Extrae la orden del contenido: `"estaListo? orden=X"`
   - Si `X < siguienteOrdenPorAtender`: responde `INFORM` con "SI" (ya terminado)
   - Si `X >= siguienteOrdenPorAtender`: responde `REFUSE` con "NO" (aún no)
   - **Nota**: El florista no comienza a trabajar por una REQUEST; trabaja autónomamente

2. **Producción autónoma** (`TickerBehaviour` con periodo aleatorio 10-15s):
   - Cada tick, si `siguienteOrdenPorAtender ≤ 3`:
     - Marca el arreglo como listo
     - Envía `INFORM` al recepcionista: `"Pedido listo para orden X"`
     - Incrementa `siguienteOrdenPorAtender`
   - Si `siguienteOrdenPorAtender > 3`: detiene el comportamiento (fin)

3. **Sincronización**: El florista atiende REQUEST concurrentemente con el trabajo de fondo gracias a los comportamientos independientes

**Implementación JADE:**
- `Setup()`: Registra en DF y busca al recepcionista
- `CyclicBehaviour`: Responde REQUEST
- `TickerBehaviour`: Ejecuta producción con periodo aleatorio

### 3. Flujo de Mensajes Detallado

| Paso | Emisor → Receptor | Performativa | Contenido | Propósito |
|------|-------------------|--------------|----------|-----------|
| 1 | Novio1 → Recepcionista | QUERY-IF | `miPedidoEstaListo? orden=1` | Preguntar si su pedido está listo |
| 2 | Recepcionista → Florista | REQUEST | `estaListo? orden=1` | Consultar al florista |
| 3 | Florista → Recepcionista | REFUSE | `NO` | Indicar que aún no está listo |
| 4 | Recepcionista → Novio1 | INFORM | `Aún no está listo` | Responder al novio |
| (Repite 1-4 cada 5-8 segundos) | | | | |
| 5 (paralelo) | Florista (autónomo) → Recepcionista | INFORM | `Pedido listo para orden 1` | Notificar finalización |
| 6 | Recepcionista → Novio1 | INFORM | `Tu pedido está listo` | Avisar al novio |
| 7 | Novio1 → Recepcionista | INFORM | `ya voy a recogerlo` | Confirmar recogida → Fin del novio |

**Luego** el florista produce orden 2 y 3 con los mismos pasos.

### Notas del Protocolo

- Si el florista recibe REQUEST para orden **ya terminada**: responde `INFORM` con "SI"
- Si el florista recibe REQUEST para orden **futura**: responde `REFUSE` con "NO"

## 4. Estructura del Repositorio

```
.
├── src/main/java/edu/sma/
│   ├── agents/
│   │   ├── NovioAgent.java           (3 instancias)
│   │   ├── RecepcionistaAgent.java
│   │   └── FloristaAgent.java
│   └── common/
│       ├── DfUtils.java              (utilidades DF)
│       ├── ServiceNames.java         (constantes de servicios)
│       ├── PayloadCodec.java         (codificación de mensajes)
│       └── RemoteAidFactory.java     (factory para AID remotos)
├── docker-compose.yml                (orquestación de 2 contenedores)
├── Dockerfile                        (multi-stage build)
├── pom.xml                           (Java 17, JADE 4.5.0, Maven)
├── scripts/
│   ├── start-main-local.ps1          (Main Container local)
│   └── start-peripheral-local.ps1    (Peripheral Container local)
├── docs/
│   ├── informe.md
│   └── arquitectura.puml
├── jade.jar                          (librería JADE como dependencia de sistema)
└── README.md                         (este archivo)
```

## 5. Requisitos

- **Java 17+** (LTS)
- **Maven 3.9+**
- **Docker Desktop + Docker Compose** (para modo distribuido)
- **Windows PowerShell** (`pwsh`) para scripts locales

## 6. Ejecución con Docker (Recomendado - Entorno Distribuido)

### 6.1. Crear la red Docker

```powershell
docker network create jade-net
```

### 6.2. Compilar y levantar

```powershell
docker compose build
docker compose up
```

Esto levanta automáticamente:
- **main-container** (Recepcionista + Florista)
- **peripheral-container** (3 Novios)

### 6.3. Detener

```powershell
docker compose down
```

## 7. Ejecución Local con GUI JADE (para desarrollo/capturas)

Abrir **2 terminales PowerShell** en la raíz del proyecto:

**Terminal 1 - Main Container (con GUI):**
```powershell
./scripts/start-main-local.ps1
```

**Terminal 2 - Peripheral Container:**
```powershell
./scripts/start-peripheral-local.ps1
```

Esto permite ver en tiempo real en la GUI JADE:
- Los tres novios enviando QUERY-IF cada 5-8 segundos
- El florista produciendo cada 10-15 segundos
- El recepcionista coordinando las comunicaciones

## 8. Flujo de Ejecución Esperado

1. **Segundos 0-10**: Los 3 novios inician, esperan 5-8s aleatorios
2. **Segundo ~8**: Novio1 (Carlos) envía QUERY-IF → Recepcionista pregunta al Florista → Florista responde "NO" → Recepcionista avisa "Aún no está listo"
3. **Segundo ~15**: Florista completa orden 1, notifica al Recepcionista → Recepcionista avisa a Novio1 "Tu pedido está listo" → Novio1 responde "ya voy a recogerlo" → **Novio1 finaliza**
4. **Segundo ~20**: Novio2 (Miguel) comienza a preguntar
5. **Segundo ~28**: Florista completa orden 2 → Novio2 recibe notificación → **Novio2 finaliza**
6. **Segundo ~30**: Novio3 (Javier) comienza a preguntar
7. **Segundo ~42**: Florista completa orden 3 → Novio3 recibe notificación → **Novio3 finaliza**
8. **Fin**: Florista se detiene (máximo de órdenes alcanzado)

## 9. Condiciones de Finalización

- **Demo termina** cuando los **3 novios** han confirmado que van a recoger su pedido
- El **Recepcionista** y **Florista** pueden seguir ejecutándose sin más interacciones

## 10. Notas de Desarrollo

- **Sincronización**: Los comportamientos `CyclicBehaviour` y `TickerBehaviour` se ejecutan concurrentemente sin bloqueos
- **DF (Directory Facilitator)**: Todos los agentes se registran y buscan dinámicamente el servicio correspondiente
- **Timeouts**: El Recepcionista espera respuesta del Florista con timeout de 3 segundos
- **Intervalo de consulta**: Los Novios consultan con intervalo aleatorio 5-8 segundos
- **Tiempo de producción**: El Florista trabaja cada 10-15 segundos (aleatorio)

## 11. Debugging

Para ver los logs detallados:

```powershell
# Ver logs del main-container
docker logs -f main-container

# Ver logs del peripheral-container
docker logs -f peripheral-container
```

O en modo local, la salida se imprime directamente en el terminal.
