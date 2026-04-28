# Flujos de Trabajo (Workflows)

Este documento detalla los flujos de trabajo críticos de los agentes, desglosando la lógica interna de toma de decisiones y procesamiento.

## Flujo de Razonamiento del Agente Recepcionista

El `RecepcionistaAgent` actúa como un orquestador o *proxy* entre los clientes (`NovioAgent`) y el productor (`FloristaAgent`). El flujo principal de razonamiento ocurre cuando recibe una consulta de un cliente.

```mermaid
flowchart TD
    A["Inicio: Mensaje entrante recibido"] --> B{"¿Cumple Plantilla (MessageTemplate)?<br>Performative: QUERY_IF<br>Protocol: consultar-pedido"}

    B -- No --> C["Ignorar o Bloquear (block)"]

    B -- Sí --> D["Leer contenido del mensaje:<br>pedido turno-{X}-de-{Nombre}-esta listo?"]
    D --> E["Procesar NLP Simple (Split de cadena)"]
    E --> F["Extraer 'orden' (int) y 'nombre' (String)"]

    F --> G["Guardar Estado Interno:<br>mapaNovios.put(orden, remitente AID)"]

    G --> H["Instanciar Comportamiento Dinámico:<br>PreguntarEstadoPedido (AchieveREInitiator)"]
    H --> I["Crear mensaje REQUEST al Florista:<br>pedido-{orden}- esta listo?"]
    I --> J["Enviar REQUEST y Esperar Respuesta (Async)"]

    J --> K{"¿Tipo de respuesta del Florista?"}

    K -- INFORM (Contenido 'SI') --> L["handleInform()"]
    L --> M["Crear respuesta INFORM al Cliente"]
    M --> N["Contenido: '{Nombre}, su pedido ya esta listo'"]
    N --> O["Enviar Mensaje al Novio"]
    O --> Z["Fin de este hilo de razonamiento"]

    K -- REFUSE (Contenido 'NO') --> P["handleRefuse()"]
    P --> Q["Crear respuesta INFORM al Cliente"]
    Q --> R["Contenido: 'El pedido todavia no esta listo'"]
    R --> S["Enviar Mensaje al Novio"]
    S --> Z
```

## Flujo de Producción y Sincronización del Agente Florista

El `FloristaAgent` posee un flujo de trabajo concurrente. Por un lado, mantiene un proceso autónomo de "fabricación" simulada. Por otro, responde proactivamente a las interrupciones del Recepcionista basándose en el estado de su fabricación.

### Flujo de Producción Autónoma (TickerBehaviour)

Este es el pipeline principal de trabajo en segundo plano.

```mermaid
flowchart TD
    A(("Inicio Ticker")) --> B{"¿siguienteOrdenPorAtender <= MAX_ORDENES (3)?"}

    B -- No --> C["Imprimir log: Todas las órdenes completadas"]
    C --> D["Detener Comportamiento: stop()"]
    D --> E(("Fin del Ticker"))

    B -- Sí --> F["producirArreglo()"]
    F --> G["Imprimir log: Comenzando trabajo en orden X"]

    G --> H["Simulación: El trabajo se completa instantáneamente<br>en este tick por diseño reactivo"]

    H --> I["Imprimir log: Orden X completada"]

    I --> J["notificarRecepcionista(X)"]
    J --> K["Crear mensaje INFORM:<br>Pedido listo para orden X"]
    K --> L["Enviar a Recepcionista (AID predefinido)"]

    L --> M["Incrementar: siguienteOrdenPorAtender++"]

    M --> N["Generar nuevo intervalo aleatorio:<br>10s a 15s"]
    N --> O["Reiniciar Ticker (reset) con nuevo intervalo"]
    O --> E
```

### Flujo de Evaluación de Estado (Respuesta a REQUEST)

Cuando el Florista recibe una interrupción (REQUEST) del Recepcionista, no detiene su producción. Simplemente evalúa su estado actual y responde.

```mermaid
flowchart LR
    A["Recibir REQUEST de Recepcionista"] --> B["Extraer número de orden solicitada (X)"]
    B --> C{"¿X < siguienteOrdenPorAtender?"}

    C -- Sí --> D["Decisión: El pedido ya fue producido en el pasado"]
    D --> E["Crear respuesta INFORM con 'SI'"]
    E --> F["Enviar respuesta al Recepcionista"]

    C -- No --> G["Decisión: El pedido está en producción o en cola"]
    G --> H["Crear respuesta REFUSE con 'NO'"]
    H --> F
```
