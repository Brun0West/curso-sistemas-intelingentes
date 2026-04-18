# Checklist de Entrega Final (Coordinador)

## A. Código y repositorio

- [ ] El repositorio contiene `src/`, `docker-compose.yml`, `Dockerfile`, `pom.xml`, `README.md`, `docs/`.
- [ ] Se verificó ejecución con:

```powershell
docker compose up -d --build
docker compose ps
```

- [ ] Se validó colaboración en logs ACL.

## B. Informe PDF

- [ ] `docs/informe.md` contiene exactamente la estructura solicitada:
  - Carátula
  - Introducción
  - Objetivos
  - Descripción de la Propuesta de la Solución
  - Contenido adicional
  - Conclusiones
  - Referencias
- [ ] Se insertaron capturas reales en las secciones correspondientes.
- [ ] Se completaron datos de carátula (curso, docente, integrantes, links).
- [ ] Se exportó PDF:

```powershell
./scripts/export-informe-pdf.ps1
```

- [ ] Archivo final generado: `docs/informe.pdf`.

## C. Video YouTube (máximo 10 minutos)

- [ ] Se siguió `docs/guion-video-youtube.md`.
- [ ] Participan todos los integrantes en cámara.
- [ ] Se muestra ejecución de ambas plataformas.
- [ ] Se evidencia DF + ACL + colaboración real.
- [ ] Video publicado en YouTube con enlace público.

## D. Entrega al aula

- [ ] Solo el coordinador sube el informe PDF final.
- [ ] El PDF incluye link al repositorio GitHub.
- [ ] El PDF incluye link al video YouTube.
- [ ] Se verificó apertura de links desde el PDF.
