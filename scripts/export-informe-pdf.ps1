# Requiere pandoc instalado: https://pandoc.org/installing.html
# Ejecutar desde la raíz del proyecto.

$ErrorActionPreference = "Stop"

try {
	pandoc "docs/informe.md" -o "docs/informe.pdf"
	if (Test-Path "docs/informe.pdf") {
		Write-Host "PDF generado en docs/informe.pdf"
		exit 0
	}
	throw "No se generó el archivo PDF."
}
catch {
	Write-Warning "No fue posible generar PDF (normalmente falta pdflatex)."
	Write-Host "Generando versión HTML como respaldo..."
	pandoc "docs/informe.md" -o "docs/informe.html"
	Write-Host "Respaldo generado en docs/informe.html"
	Write-Host "Sugerencia: abre docs/informe.html en el navegador e imprime a PDF."
}
