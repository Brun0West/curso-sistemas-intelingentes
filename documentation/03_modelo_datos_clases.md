# Estructura de Datos y Modelos

Este documento describe la estructura de clases del proyecto, sus relaciones de herencia y cómo se maneja el estado de los datos, dado que el sistema no utiliza persistencia externa (base de datos).

## Diagrama de Clases (UML)

El sistema está construido extendiendo la clase base `Agent` proporcionada por el framework JADE. Además, emplea clases de utilidad para centralizar constantes y lógicas repetitivas.

```mermaid
classDiagram
    class Agent {
        <<JADE Framework>>
        +setup()
        +addBehaviour(Behaviour)
        +send(ACLMessage)
        +receive() : ACLMessage
        +doDelete()
    }

    class NovioAgent {
        -int turno
        -String nombre
        -AID recepcionista
        +setup()
        -iniciarComportamientos()
        -contactarRecepcionista() : AID
    }

    class RecepcionistaAgent {
        -Map~Integer, AID~ mapaNovios
        +setup()
        #atenderConsultas()
        -escucharNotificacionesFlorista()
    }

    class FloristaAgent {
        -static int TIEMPO_TRABAJO_MIN
        -static int TIEMPO_TRABAJO_MAX
        -static int MAX_ORDENES
        -Integer siguienteOrdenPorAtender
        +setup()
        +obtenerOrden(String) : int
    }

    class PreguntarEstadoPedido {
        <<Inner Class>>
        -ACLMessage consultaOriginal
        -String nombre
        -Integer orden
        +handleInform(ACLMessage)
        +handleRefuse(ACLMessage)
    }

    class AchieveREInitiator {
        <<JADE Protocol>>
    }

    class DfUtils {
        <<Utility>>
        +registerService(Agent, String, String)$
        +searchFirst(Agent, String)$ Optional~DFAgentDescription~
        +countServices(Agent, String)$ int
    }

    class ServiceNames {
        <<Constants>>
        +String RECEPCIONISTA_SERVICE$
        +String FLORISTA_SERVICE$
    }

    Agent <|-- NovioAgent
    Agent <|-- RecepcionistaAgent
    Agent <|-- FloristaAgent
    AchieveREInitiator <|-- PreguntarEstadoPedido
    RecepcionistaAgent +-- PreguntarEstadoPedido : Contiene

    NovioAgent ..> ServiceNames : usa
    RecepcionistaAgent ..> ServiceNames : usa
    RecepcionistaAgent ..> DfUtils : usa
```

## Estructura de Datos y Persistencia

El proyecto **no maneja persistencia externa** (como una base de datos relacional o NoSQL). Todo el estado necesario para el funcionamiento del sistema se mantiene en la memoria interna de cada agente durante su ciclo de vida.

### Gestión de Estado en `RecepcionistaAgent`

La estructura de datos más relevante del sistema reside en el `RecepcionistaAgent` para realizar un seguimiento de a quién pertenece cada pedido.

*   **Modelo de Datos en Memoria:** `Map<Integer, jade.core.AID> mapaNovios`
*   **Propósito:** Cuando el `RecepcionistaAgent` recibe una consulta (QUERY_IF) de un `NovioAgent`, extrae el número de orden del contenido del mensaje y guarda la relación entre el número de orden y el Identificador de Agente (AID) del remitente.
*   **Uso:** Cuando el `FloristaAgent` notifica asíncronamente que un pedido está listo (mediante un mensaje INFORM "Pedido listo para orden X"), el `RecepcionistaAgent` consulta este mapa utilizando la orden `X` para recuperar el `AID` del novio correcto y enviarle la notificación final.
*   **Volatilidad:** Si el contenedor JADE que aloja al `RecepcionistaAgent` se reinicia, este mapa se pierde y el agente pierde la capacidad de enrutar notificaciones de pedidos previos.

### Gestión de Estado en `FloristaAgent`

*   **Atributo:** `Integer siguienteOrdenPorAtender`
*   **Propósito:** Actúa como un contador secuencial y una máquina de estado implícita del progreso de producción.
*   **Lógica:** Comienza en 1. Cada vez que se completa un ciclo de trabajo (`TickerBehaviour` simulando producción), se incrementa. Se utiliza para determinar qué responder al `RecepcionistaAgent` (SI ya terminó la orden solicitada, o NO si la orden solicitada es mayor o igual al contador).