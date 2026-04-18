# Guion de Video YouTube (Máximo 10 minutos)

## Datos generales

- **Duración objetivo:** 8:30 min
- **Integrantes sugeridos:** 4 personas
- **Formato:** demostración técnica + explicación de arquitectura + resultados
- **Apoyo para evidencias:** `docs/checklist-capturas.md`

## Distribución del tiempo

1. **00:00 - 00:45 (Integrante 1): Presentación general**
2. **00:45 - 02:30 (Integrante 2): Arquitectura y plataformas JADE**
3. **02:30 - 05:30 (Integrante 3): Demo en vivo (ejecución + mensajes ACL)**
4. **05:30 - 07:00 (Integrante 4): DF, colaboración y trazabilidad**
5. **07:00 - 08:30 (Todos): conclusiones y cierre**

---

## Guion detallado por segmento

### Segmento 1 - Introducción (00:00 - 00:45)

**Responsable: Integrante 1**

Texto sugerido:

> “En esta presentación mostramos la Tarea SMA JADE. Implementamos un sistema multi-agente distribuido con dos plataformas independientes, cinco agentes y comunicación ACL con descubrimiento de servicios por DF. La lógica de negocio corresponde a la gestión colaborativa de pedidos en una cadena de restaurantes.”

Mostrar en pantalla:
- Portada del informe.
- Estructura del repositorio en VS Code.

### Segmento 2 - Arquitectura (00:45 - 02:30)

**Responsable: Integrante 2**

Puntos a explicar:
- P1: `restaurante`, `logistica`, `supervisor`.
- P2: `cliente`, `pagos`.
- Uso de `-name`, `-container`, `-host`, `-port`, `-mtp`.
- Comunicación interplataforma por red.

Mostrar en pantalla:
- Diagrama Mermaid de arquitectura (`docs/informe.md`).
- `docker-compose.yml`.

Frase clave:

> “Cada agente publica su servicio en DF y consume servicios de otros agentes; el flujo es realmente colaborativo y no secuencial rígido.”

### Segmento 3 - Demostración técnica (02:30 - 05:30)

**Responsable: Integrante 3**

#### Parte A: Despliegue

Ejecutar:

```powershell
docker compose build
docker compose up
```

o en local con GUI para evidencias:

```powershell
./scripts/start-p1-main.ps1
./scripts/start-p1-c1.ps1
./scripts/start-p1-c2.ps1
./scripts/start-p2-main.ps1
./scripts/start-p2-c1.ps1
```

#### Parte B: Verificación funcional

Mostrar logs donde aparezcan:
- `CFP` enviado por `cliente@P2`
- `PROPOSE` de `restaurante@P1`
- `REQUEST/INFORM` con `logistica@P1`
- `REQUEST/CONFIRM` con `pagos@P2`
- `INFORM` a `supervisor@P1`

Comando sugerido para mostrar todo en una sola toma:

```powershell
docker compose logs --tail=200 p2-c1 p1-main p1-c1 p2-main p1-c2
```

Frase clave:

> “Aquí se evidencia que la decisión del cliente depende de información obtenida de múltiples agentes distribuidos.”

### Segmento 4 - DF y colaboración real (05:30 - 07:00)

**Responsable: Integrante 4**

Explicar:
- Registro de servicios en DF por todos los agentes.
- Búsquedas periódicas de servicios (health checks de descubrimiento).
- Reutilización de resultados: cotización + logística + pago + supervisión.

Mostrar en pantalla:
- Captura de DF (servicios registrados).
- Fragmentos de consola con mensajes ACL.

### Segmento 5 - Cierre (07:00 - 08:30)

**Responsables: Todos (20-25s cada uno)**

Cada integrante dice una conclusión breve:
- Integrante 1: aprendizaje sobre JADE.
- Integrante 2: aprendizaje sobre despliegue distribuido.
- Integrante 3: aprendizaje sobre protocolos ACL.
- Integrante 4: aprendizaje sobre DF y colaboración emergente.

Cierre final sugerido:

> “Concluimos que JADE permite implementar sistemas multi-agente escalables y trazables, y que Docker facilita su despliegue reproducible en entornos académicos y profesionales.”

---

## Checklist para grabación

- [ ] Todos los integrantes aparecen en video.
- [ ] Se muestran ambas plataformas (P1 y P2).
- [ ] Se observa registro en DF.
- [ ] Se observan mensajes ACL en tiempo real.
- [ ] Se evidencia colaboración real entre agentes.
- [ ] El video no supera 10 minutos.
- [ ] Se comparte link público de YouTube en la carátula del informe.
