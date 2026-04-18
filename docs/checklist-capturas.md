# Checklist de Capturas para el Informe (SMA JADE)

> Objetivo: obtener evidencia visual suficiente para cumplir la rúbrica sin omisiones.

## 1) Captura de plataformas activas (Docker)

- **Qué mostrar:** contenedores `p1-main`, `p1-c1`, `p1-c2`, `p2-main`, `p2-c1` en estado `Up`.
- **Comando sugerido:**

```powershell
docker compose ps
```

- **Nombre de archivo sugerido:** `docs/capturas/01-docker-ps.png`

## 2) Captura de arranque de P1

- **Qué mostrar:** logs de `p1-main`, `p1-c1`, `p1-c2` con mensajes:
  - `restaurante iniciado y registrado en DF`
  - `logistica iniciado y registrado en DF`
  - `supervisor iniciado y registrado en DF`
- **Comando sugerido:**

```powershell
docker compose logs --tail=120 p1-main p1-c1 p1-c2
```

- **Nombre de archivo sugerido:** `docs/capturas/02-p1-df.png`

## 3) Captura de arranque de P2

- **Qué mostrar:** logs de `p2-main`, `p2-c1` con mensajes:
  - `pagos iniciado y registrado en DF`
  - `cliente iniciado y registrado en DF`
- **Comando sugerido:**

```powershell
docker compose logs --tail=120 p2-main p2-c1
```

- **Nombre de archivo sugerido:** `docs/capturas/03-p2-df.png`

## 4) Captura de colaboración ACL (cliente)

- **Qué mostrar:** líneas de `cliente` con:
  - `propuesta recibida`
  - `pedido completado`
- **Comando sugerido:**

```powershell
docker compose logs --tail=200 p2-c1
```

- **Nombre de archivo sugerido:** `docs/capturas/04-cliente-acl.png`

## 5) Captura de colaboración ACL (logística y pagos)

- **Qué mostrar:**
  - `logistica >> cálculo entrega enviado`
  - `pagos >> validación pago enviada`
- **Comando sugerido:**

```powershell
docker compose logs --tail=200 p1-c1 p2-main
```

- **Nombre de archivo sugerido:** `docs/capturas/05-logistica-pagos.png`

## 6) Captura de supervisión y cierre colaborativo

- **Qué mostrar:**
  - `supervisor AUDIT >> ... ORDER_COMPLETED`
  - `restaurante >> pedido confirmado`
- **Comando sugerido:**

```powershell
docker compose logs --tail=200 p1-c2 p1-main
```

- **Nombre de archivo sugerido:** `docs/capturas/06-supervisor-restaurante.png`

## 7) Captura de configuración del entorno (VM/Docker)

- **Qué mostrar:**
  - `docker --version`
  - `java -version`
- **Comando sugerido:**

```powershell
docker --version
java -version
```

- **Nombre de archivo sugerido:** `docs/capturas/07-entorno-versiones.png`

## Recomendación de formato

- Tomar capturas en resolución completa.
- No recortar las líneas clave de evidencia.
- Insertar pie de figura en el informe indicando: fecha, comando y resultado observado.

## Nota sobre `docker compose logs -f`

- Si usas `-f` (follow) y detienes con `Ctrl + C`, PowerShell puede mostrar código de salida distinto de `0`.
- Eso **no implica fallo** del sistema; para capturas del informe conviene usar comandos sin `-f` y con `--tail`.
