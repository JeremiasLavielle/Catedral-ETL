# Catedral-ETL

Servicio web que convierte PDFs de **Liquidaciones Primarias de Granos (LPG)** en archivos de texto con el formato posicional requerido por el régimen de información de comprobantes emitidos y recibidos de **ARCA/AFIP** (RG 3685 — CITI Compras/Ventas).

El usuario sube el PDF original de la liquidación y descarga un ZIP con `CABECERA.txt` y `DETALLE.txt`, listos para importar en el aplicativo correspondiente.

---

## Tabla de contenidos

- [Motivación](#motivación)
- [Cómo funciona](#cómo-funciona)
- [Stack técnico](#stack-técnico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Endpoints](#endpoints)
- [Cómo correrlo localmente](#cómo-correrlo-localmente)
- [Frontend](#frontend)
- [Formato de salida](#formato-de-salida)
- [Roadmap](#roadmap)
- [Limitaciones conocidas](#limitaciones-conocidas)
- [Licencia](#licencia)

---

## Motivación

La carga manual de liquidaciones de granos en el régimen de información de AFIP/ARCA es lenta y propensa a errores: cada comprobante implica transcribir CUITs, importes, deducciones, IVA y armar un registro posicional fijo de más de 200 caracteres. Para un estudio contable con varios productores agropecuarios como clientes, esto representa horas de trabajo repetitivo al cierre de cada período.

Catedral-ETL automatiza el proceso: el contador sube el PDF que entrega la corredora, y obtiene los TXT listos para importar.

## Cómo funciona

```
┌──────────────┐    PDF     ┌──────────────────┐    DTO     ┌─────────────────┐
│   Cliente    │ ─────────► │  LpgParser       │ ─────────► │ GenerationService│
│ (browser/curl)│           │  (PDFBox)        │            │  (Cabecera +    │
└──────────────┘            └──────────────────┘            │   Detalle)      │
       ▲                                                    └────────┬────────┘
       │                                                             │
       │                    ┌──────────────────┐    TXT              │
       │       ZIP          │  ExportService   │ ◄───────────────────┘
       └────────────────────┤  (ZIP en memoria)│
                            └──────────────────┘
```

1. **Parsing**: `LpgParserService` extrae el texto del PDF con Apache PDFBox y mapea cada liquidación a un `LpgDocumentDTO` con sus liquidaciones y deducciones.
2. **Generación**: `GenerationService` arma dos artefactos:
   - **Cabecera**: una línea tipo 1 por cada comprobante + una línea tipo 2 con totales del período.
   - **Detalle**: una línea por comprobante con el desglose de alícuotas de IVA.
3. **Export**: `ExportService` empaqueta ambos TXT en un ZIP en memoria y lo devuelve al cliente.

## Stack técnico

- **Java 21**
- **Spring Boot 4.0.6** (web MVC + validation)
- **Apache PDFBox 3.0.3** para parsing de PDF
- **Apache Commons Lang3** para padding y manipulación de strings
- **Lombok** para reducir boilerplate
- **Maven** como build tool
- **JUnit 5** + **Spring Boot Test** para testing

## Estructura del proyecto

```
src/main/java/com/catedral/catedraletl/
├── api/
│   ├── LpgController.java          # endpoint REST POST /api/generar
│   └── GlobalExceptionHandler.java # manejo centralizado de errores
├── parsing/
│   ├── LpgParserService.java       # extracción de datos del PDF
│   ├── LpgDocumentDTO.java         # documento parseado completo
│   ├── LiquidacionDTO.java         # una liquidación individual
│   └── DeduccionDTO.java           # deducción aplicada a una liquidación
├── generation/
│   ├── GenerationService.java      # orquesta la generación del TXT
│   ├── CabeceraBuilder.java        # arma líneas tipo 1 y tipo 2
│   ├── DetalleBuilder.java         # arma línea de detalle por comprobante
│   ├── LpgCalculator.java          # cálculos: neto, IVA, total, deducciones
│   ├── TxtFormatter.java           # formateo posicional (padding, importes)
│   └── TxtResultDTO.java           # cabecera + detalle generados
├── export/
│   ├── ExportService.java          # empaqueta los TXT en un ZIP
│   └── ZipResultDTO.java
└── exception/
    ├── LpgParseException.java      # errores durante el parsing
    └── GenerationException.java    # errores durante la generación
```

La organización sigue un esquema por **capas verticales por dominio** (parsing / generation / export), no por capa horizontal (controllers / services / repositories). Cada paquete agrupa lo necesario para una etapa del pipeline, lo que facilita extraer módulos en el futuro si fuera necesario.

## Endpoints

### `POST /api/generar`

Recibe un PDF de liquidación y devuelve un ZIP con los TXT generados.

**Request**

```
Content-Type: multipart/form-data

file: <archivo PDF>
```

**Response**

```
200 OK
Content-Type: application/octet-stream
Content-Disposition: attachment; filename=liquidaciones.zip

<binario del ZIP>
```

El ZIP contiene:
- `CABECERA.txt` — líneas de cabecera (una tipo 1 por comprobante + una tipo 2 con totales).
- `DETALLE.txt` — líneas de detalle (una por comprobante).

**Ejemplo con curl**

```bash
curl -X POST http://localhost:8080/api/generar \
  -F "file=@liquidacion.pdf" \
  --output liquidaciones.zip
```

## Cómo correrlo localmente

### Prerrequisitos

- JDK 21 instalado (`java -version` debería mostrar 21).
- Maven instalado, o usar el wrapper incluido (`./mvnw`).

### Pasos

```bash
git clone https://github.com/<tu-usuario>/Catedral-ETL.git
cd Catedral-ETL
./mvnw spring-boot:run
```

La app queda corriendo en `http://localhost:8080`.

### Build de un JAR ejecutable

```bash
./mvnw clean package
java -jar target/Catedral-ETL-0.0.1-SNAPSHOT.jar
```

### Configuración

Variables relevantes en `application.properties`:

```properties
# Puerto (usa PORT si está definido como variable de entorno, útil para deploy)
server.port=${PORT:8080}

# Límite de tamaño de PDF de entrada
spring.servlet.multipart.max-file-size=15MB
spring.servlet.multipart.max-request-size=15MB
```

## Frontend

La aplicación incluye una landing en `src/main/resources/static/index.html` que permite arrastrar y soltar el PDF en el navegador para descargar el ZIP, sin necesidad de cliente HTTP externo. Se sirve automáticamente en la raíz del backend:

```
http://localhost:8080/
```

Al servir frontend y API desde el mismo origen no hay configuración de CORS involucrada.

## Formato de salida

Los TXT siguen el formato posicional fijo de **registración de comprobantes** definido por AFIP/ARCA. Cada campo tiene una posición y longitud específica, con padding de ceros (para números) o espacios (para texto).

### Cabecera tipo 1 (una línea por comprobante)

Incluye, entre otros campos: fecha, tipo de comprobante, punto de venta, número, CUIT del comprador, razón social, importe total, neto gravado, IVA, moneda y tipo de cambio.

### Cabecera tipo 2 (una línea por período, al final)

Resumen del período: cantidad de comprobantes, CUIT del vendedor, totales acumulados, neto y IVA.

### Detalle (una línea por comprobante)

Desglose por alícuota: el sistema actualmente emite todo bajo alícuota **10,5%** (código `0105`), que es la aplicable a granos en el régimen general.

> Los valores `tipoComprobante=01`, `concepto=LIQUIDACION PRIMARIA DE GRANOS`, alícuota fija al 10,5% y otros literales están hardcodeados en los builders. Esto refleja las reglas actuales del dominio; ver [Limitaciones conocidas](#limitaciones-conocidas).

## Limitaciones

- **Formato de PDF acotado**: el parser está calibrado para el formato actual de liquidaciones de las corredoras con las que se trabaja. Cambios en el layout del PDF pueden romper la extracción.
- **Alícuota única 10,5%**: hoy todos los detalles se emiten con esa alícuota. Si en el futuro aparecen liquidaciones con otra alícuota, hay que extender `DetalleBuilder`.
- **`GlobalExceptionHandler` aún vacío**: las excepciones bubble-up con el comportamiento por defecto de Spring. Esto se va a completar antes del deploy productivo.
- **Sin autenticación**: el servicio asume uso público controlado por rate limiting (pendiente) y validación de entrada. No expone datos persistidos porque no persiste nada — los PDFs y TXT viven solo en memoria durante la request.
