# Lógica de Agentes

Este documento detalla la interacción, los estados y el uso de herramientas (como la búsqueda en el Directory Facilitator) por parte de los agentes del sistema.

## Diagrama de Secuencia de Interacción

El siguiente diagrama ilustra cómo los agentes se comunican entre sí utilizando mensajes FIPA-ACL para coordinar el estado de un pedido.

```mermaid
sequenceDiagram
    participant N as NovioAgent
    participant R as RecepcionistaAgent
    participant F as FloristaAgent

    Note over N: Inicia comportamiento (TickerBehaviour)

    loop Consulta Periódica (Cada X ms)
        N->>R: ACLMessage(QUERY_IF) [protocol: "consultar-pedido"]<br/>"pedido turno-{X}-de-{Nombre}-esta listo?"

        Note over R: Inicia PreguntarEstadoPedido (AchieveREInitiator)
        R->>F: ACLMessage(REQUEST) [protocol: "consultar-pedido"]<br/>"pedido-{X}- esta listo?"

        alt Pedido Aún No Listo
            Note over F: Verifica orden < siguienteOrdenPorAtender
            F-->>R: ACLMessage(REFUSE)<br/>"NO"
            Note over R: handleRefuse()
            R-->>N: ACLMessage(INFORM)<br/>"El pedido todavia no esta listo"
            Note over N: Recibe respuesta y espera al siguiente ciclo.
        else Pedido Ya Listo
            Note over F: Verifica orden >= siguienteOrdenPorAtender
            F-->>R: ACLMessage(INFORM)<br/>"SI"
            Note over R: handleInform()
            R-->>N: ACLMessage(INFORM)<br/>"{Nombre}, su pedido ya esta listo"
        end
    end

    Note over F: Proceso Autónomo (TickerBehaviour)
    Note over F: Produce arreglo de Orden {X}
    F->>R: ACLMessage(INFORM)<br/>"Pedido listo para orden {X}"

    Note over R: Busca Novio correspondiente en mapaNovios
    R->>N: ACLMessage(INFORM)<br/>"Tu pedido está listo"

    Note over N: Recibe notificación asíncrona
    N->>R: ACLMessage(CONFIRM)<br/>"Gracias, ya estoy yendo a recogerlo."
    Note over N: doDelete() - Finaliza el Agente
```

## Máquina de Estados de los Agentes

Aunque JADE se basa en comportamientos (`Behaviours`) que se planifican cooperativamente, podemos modelar el ciclo de vida de los agentes mapeándolo a una máquina de estados general de un sistema basado en agentes.

```mermaid
stateDiagram-v2
    [*] --> Init: Agent Startup

    state Init {
        [*] --> Setup
        Setup --> DF_Interaction: Registrar/Buscar Servicios
    }

    Init --> Idle: Configuración Completada

    state Idle {
        [*] --> WaitingForMessages: CyclicBehaviour
        [*] --> WaitingForTick: TickerBehaviour
    }

    state Action {
        state "Envío de Mensajes" as Action_Send
        state "Producción (Simulada)" as Action_Produce
    }

    state Reasoning {
        state "Procesamiento de Reglas/Lógica" as Reason_Logic
        state "Manejo de Protocolos (ej. AchieveREInitiator)" as Reason_Protocol
    }

    Idle --> Reasoning: Mensaje Recibido o Tick Alcanzado
    Reasoning --> Action: Decisión Tomada
    Action --> Idle: Acción Completada (ej. Mensaje Enviado)

    Action --> Success: Tarea Finalizada (ej. Novio recoge pedido, Florista termina max órdenes)
    Reasoning --> Error: Falla en procesamiento o timeout
    Error --> Idle: Recuperación

    Success --> [*]: doDelete()
```

## Flujo de Herramientas (Tool Use): Interacción con el Directory Facilitator (DF)

En este sistema, la principal "herramienta" que utilizan los agentes es el servicio de Páginas Amarillas proporcionado por el Directory Facilitator (DF) de JADE. Este diagrama explica el flujo de cómo un agente consulta el DF para encontrar a otro agente con el que necesita comunicarse.

```mermaid
flowchart TD
    A["Inicio: NovioAgent necesita contactar al Recepcionista"] --> B{"¿Recepcionista AID es null?"}
    B -- Sí --> C["Preparar Plantilla de Búsqueda"]
    C --> D["Crear DFAgentDescription"]
    D --> E["Crear ServiceDescription con type='recepcionista-service'"]
    E --> F["Añadir ServiceDescription a DFAgentDescription"]

    F --> G["Llamada a Herramienta Externa:<br> DFService.search(this, modelo)"]

    G --> H{"¿Excepción FIPAException?"}
    H -- Sí --> I["Capturar Error e Imprimir Traza"]
    I --> J["Retornar null"]

    H -- No --> K{"¿Resultados > 0?"}
    K -- No --> L["Retornar null"]
    K -- Sí --> M["Extraer AID del primer resultado:<br> results[0].getName()"]

    M --> N["Actualizar Estado Interno:<br> recepcionista = AID"]
    N --> O["Detener TickerBehaviour de búsqueda"]
    O --> P["Iniciar Comportamientos Principales de Comunicación"]

    B -- No --> P
    L --> Q["Esperar próximo Tick para reintentar"]
    J --> Q
    Q --> A
```
