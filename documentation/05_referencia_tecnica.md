# Referencia Técnica y API

Este documento proporciona una referencia técnica de las clases principales, métodos, tipos de datos y configuraciones necesarias para extender y mantener el sistema.

## Patrones de Diseño Utilizados

Durante el análisis del código fuente, se identificaron los siguientes patrones:

1.  **Singleton / Static Utility Pattern:** La clase `DfUtils` utiliza métodos estáticos y un constructor privado para evitar instanciación, proporcionando un punto de acceso global y único a la lógica de registro y búsqueda de servicios en el Directory Facilitator. `ServiceNames` sigue el mismo patrón para almacenar constantes.
2.  **Observer (Implícito por paso de mensajes):** El sistema de comunicación de JADE, basado en `MessageTemplate` y `CyclicBehaviour`, implementa un modelo de eventos *Publish-Subscribe* u *Observer* a nivel arquitectónico. Los agentes se suscriben a tipos de mensajes específicos y reaccionan cuando llegan.
3.  **State Pattern (Implícito):** El comportamiento del sistema cambia según su estado interno. Por ejemplo, el `FloristaAgent` cambia su respuesta (INFORM vs REFUSE) basándose en la variable de estado `siguienteOrdenPorAtender`.

## Clases Principales y Métodos

### `edu.sma.agents.NovioAgent`
Agente cliente que consulta periódicamente por su pedido.

*   **Configuración Inicial:** Espera argumentos en el constructor/setup `[String turno, String nombre]`. Por defecto: `turno=1`, `nombre="Luis"`.
*   **Métodos Principales:**
    *   `protected void setup()`: Inicializa el agente y busca al recepcionista.
    *   `private void iniciarComportamientos()`: Registra el `TickerBehaviour` (consulta) y el `CyclicBehaviour` (escucha de respuestas).
    *   `private AID contactarRecepcionista()`:
        *   *Entrada:* Ninguna.
        *   *Salida:* `jade.core.AID` (Identificador del agente recepcionista) o `null` si falla.

### `edu.sma.agents.RecepcionistaAgent`
Agente intermediario que enruta mensajes y mantiene el estado temporal.

*   **Variables de Estado:** `Map<Integer, AID> mapaNovios`
*   **Métodos Principales:**
    *   `protected void setup()`: Registra el servicio `recepcionista-service` y lanza comportamientos.
    *   `protected void atenderConsultas()`: Inicializa el *listener* principal para mensajes `QUERY_IF`.
    *   `private void escucharNotificacionesFlorista()`: Inicializa el *listener* para mensajes asíncronos del florista.
*   **Clase Interna `PreguntarEstadoPedido` (Hereda de `AchieveREInitiator`)**:
    *   *Constructor:* `PreguntarEstadoPedido(Agent a, ACLMessage consultaCliente, Integer orden, String nombre)`
    *   *Método `handleInform(ACLMessage inform)`*: Procesa respuesta afirmativa del florista.
    *   *Método `handleRefuse(ACLMessage refuse)`*: Procesa respuesta negativa del florista.

### `edu.sma.agents.FloristaAgent`
Agente productor autónomo que atiende consultas concurrentemente.

*   **Constantes:** `TIEMPO_TRABAJO_MIN=10000`, `TIEMPO_TRABAJO_MAX=15000`, `MAX_ORDENES=3`.
*   **Métodos Principales:**
    *   `protected void setup()`: Inicializa respuestas a `REQUEST` y el simulador de producción `TickerBehaviour`.
    *   `public int obtenerOrden(String mensaje)`:
        *   *Entrada:* `String` (ej. "pedido-1- esta listo?").
        *   *Salida:* `int` (el número de orden extraído, o -1 si hay error de formato).

### `edu.sma.common.DfUtils`
Clase de utilidades para interactuar con el Directory Facilitator.

*   **Métodos Principales:**
    *   `public static void registerService(Agent agent, String serviceType, String serviceName)`: Registra a un agente.
    *   `public static Optional<DFAgentDescription> searchFirst(Agent agent, String serviceType)`: Busca el primer agente que provea un servicio específico.
    *   `public static int countServices(Agent agent, String serviceType)`: Cuenta cuántos agentes proveen un servicio.

## Variables de Entorno y Configuración

El proyecto se despliega utilizando Docker y requiere la siguiente estructura de red y configuración, de acuerdo al análisis del `README.md` original y los scripts de ejecución.

1.  **Red Docker:** Se requiere una red compartida, típicamente creada con `docker network create jade-net`.
2.  **JADE Main Container:**
    *   Debe inicializarse con el parámetro `-gui` (opcional para depuración visual) y el nombre de plataforma especificado.
3.  **JADE Peripheral Container:**
    *   Debe inicializarse apuntando al Main Container utilizando los parámetros de JADE `-host` y `-port`.
4.  **Argumentos de Agentes:**
    *   Los `NovioAgent` requieren pasar argumentos en tiempo de creación a través de los scripts de lanzamiento o configuración Docker (ej. `Novio1:edu.sma.agents.NovioAgent(1, Carlos)`).

*Nota: Todas estas configuraciones están abstraídas en los archivos `docker-compose.yml` y los scripts dentro de la carpeta `/scripts` del repositorio.*
