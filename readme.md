# XploreNow Backend API 

Backend de SpringBoot para la construccion de una API REST con el objetivo de ser consumida por Android Native y React Native. Desarrollo resultado de la materia "Desarollo de Aplicaciones I" en UADE

## 1. Resumen Rapido

- Base URL local: `http://localhost:8080`
- Version de API: `v1`
- Formato: JSON
- Auth: Bearer JWT
- Documentacion interactiva: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 2. Como levantar el backend en local

### 2.1 Requisitos

- Java 17
- Maven 3.9+
- Docker Desktop (para PostgreSQL local)

### 2.2 Levantar base de datos

```bash
docker compose up -d
```

Esto crea PostgreSQL con:

- DB: `xplorenow`
- User: `xplorenow`
- Password: `xplorenow`
- Puerto: `5432`

### 2.3 Levantar API

```bash
mvn spring-boot:run
```

## 3. Autenticacion y Sesion

La API usa JWT Bearer para endpoints protegidos.

Header requerido en endpoints protegidos:

```http
Authorization: Bearer <token>
```

Duracion actual del token: `120 minutos` (`expiresInSeconds = 7200`).

## 4. Flujos de autenticacion

## 4.1 Registro clasico

- Metodo: `POST`
- Endpoint: `/api/v1/auth/register`
- Auth requerida: No

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123",
  "firstName": "Ana",
  "lastName": "Gomez",
  "phone": "+5491112345678"
}
```

Reglas:

- `email`: requerido, formato email
- `password`: requerido, 8 a 72 chars
- `firstName`: requerido, max 80
- `lastName`: requerido, max 80
- `phone`: opcional, max 30

Response `201 Created`:

```json
{
  "token": "<jwt>",
  "expiresInSeconds": 7200,
  "tokenType": "Bearer",
  "email": "user@example.com",
  "fullName": "Ana Gomez"
}
```

## 4.2 Login clasico

- Metodo: `POST`
- Endpoint: `/api/v1/auth/login`
- Auth requerida: No

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

Response `200 OK`: mismo formato que `AuthResponse`.

## 4.3 Solicitar OTP

- Metodo: `POST`
- Endpoint: `/api/v1/auth/otp/request`
- Auth requerida: No

Request:

```json
{
  "email": "user@example.com",
  "purpose": "LOGIN"
}
```

`purpose` posibles:

- `LOGIN`
- `ACCESS_RECOVERY`

Response `200 OK`:

```json
{
  "email": "user@example.com",
  "purpose": "LOGIN",
  "expiresInSeconds": 600,
  "message": "OTP sent successfully"
}
```

## 4.4 Reenviar OTP

- Metodo: `POST`
- Endpoint: `/api/v1/auth/otp/resend`
- Auth requerida: No

Request: igual a `otp/request`.

Response: igual a `OtpChallengeResponse`.

## 4.5 Verificar OTP

- Metodo: `POST`
- Endpoint: `/api/v1/auth/otp/verify`
- Auth requerida: No

Request:

```json
{
  "email": "user@example.com",
  "code": "123456",
  "purpose": "LOGIN"
}
```

Reglas:

- `code`: 6 digitos (`\\d{6}`)

Response `200 OK`: mismo formato que `AuthResponse`.

## 5. Catalogo de actividades

Todos los endpoints de actividades requieren JWT.

## 5.1 Listado paginado (Home)

- Metodo: `GET`
- Endpoint: `/api/v1/activities`
- Auth requerida: Si

Query params opcionales de filtro:

- `destinationId` (long)
- `category` (enum)
- `date` (`yyyy-MM-dd`)
- `minPrice` (decimal)
- `maxPrice` (decimal)

Query params de paginacion:

- `page` (default `0`)
- `size` (default `10`)
- `sortBy` (default `id`)
- `direction` (`asc` o `desc`, default `asc`)

Categorias disponibles:

- `FREE_TOUR`
- `GUIDED_TOUR`
- `DAY_TRIP`
- `GASTRONOMIC_EXPERIENCE`
- `ADVENTURE`

Ejemplo:

```http
GET /api/v1/activities?destinationId=1&category=FREE_TOUR&date=2026-04-01&minPrice=10&maxPrice=50&page=0&size=20
```

Response: Spring `Page<ActivitySummaryDto>`.

Ejemplo de `content[]`:

```json
{
  "activityId": 10,
  "image": "https://cdn.xplorenow.com/img/10-main.jpg",
  "name": "Free Tour Centro Historico",
  "destination": "Buenos Aires",
  "category": "FREE_TOUR",
  "durationMinutes": 120,
  "price": 0,
  "availableSpots": 25
}
```

## 5.2 Detalle de actividad

- Metodo: `GET`
- Endpoint: `/api/v1/activities/{activityId}`
- Auth requerida: Si

Response `200 OK` (`ActivityDetailDto`):

```json
{
  "activityId": 10,
  "name": "Free Tour Centro Historico",
  "category": "FREE_TOUR",
  "shortDescription": "Recorrido por casco historico",
  "fullDescription": "Descripcion completa...",
  "destination": "Buenos Aires",
  "guideName": "Maria Perez",
  "durationMinutes": 120,
  "language": "SPANISH",
  "meetingPoint": "Plaza de Mayo",
  "inclusions": "Guia local",
  "cancellationPolicy": "Cancelacion sin costo hasta 24h",
  "price": 0,
  "currency": "ARS",
  "availableSpots": 25,
  "gallery": [
    "https://cdn.xplorenow.com/img/10-1.jpg",
    "https://cdn.xplorenow.com/img/10-2.jpg"
  ]
}
```

## 5.3 Actividades destacadas

- Metodo: `GET`
- Endpoint: `/api/v1/activities/featured`
- Auth requerida: Si

Query params:

- `userId` (requerido)
- `page`, `size`, `sortBy`, `direction` (opcionales)

Response: `Page<ActivitySummaryDto>`.

## 6. Modelo de errores

Formato estandar de error:

```json
{
  "timestamp": "2026-03-18T13:05:20.210",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/auth/register",
  "details": [
    "email: must be a well-formed email address"
  ]
}
```

Codigos frecuentes:

- `400`: validacion, OTP invalido, OTP expirado
- `401`: credenciales invalidas o token faltante/invalido
- `404`: usuario o actividad no encontrada
- `409`: email ya registrado
- `500`: error interno

## 7. Contratos clave para mobile

- Guardar `token` y reenviarlo en cada request protegido.
- Si reciben `401`, pedir login nuevamente o refrescar flujo en app.
- En OTP, usar exactamente codigo de 6 digitos.
- Para filtros de fecha, usar siempre formato `yyyy-MM-dd`.
- El backend responde listas paginadas con estructura Spring `Page`.

## 8. Swagger para QA y desarrollo

- UI: `http://localhost:8080/swagger-ui.html`
- JSON schema: `http://localhost:8080/v3/api-docs`

Desde Swagger se pueden probar todos los endpoints y copiar ejemplos de requests.

## 9. Estado actual funcional

Implementado y disponible:

- Registro y login clasico
- Flujo OTP (request, resend, verify)
- Catalogo paginado con filtros combinados
- Detalle completo de actividad
- Actividades destacadas por preferencias
- Seguridad JWT y documentacion OpenAPI

## 10. Recomendaciones para integracion mobile

- Centralizar el manejo de `Authorization` en interceptor HTTP.
- Definir modelos tipados en mobile equivalentes a DTOs de esta guia.
- Implementar manejo uniforme de `ApiErrorResponse` para mostrar mensajes amigables.
- Preparar estrategia de expiracion de sesion basada en `expiresInSeconds`.
