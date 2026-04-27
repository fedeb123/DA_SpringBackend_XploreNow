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

### 2.3 Popular base con datos de prueba (sin psql local)

Este proyecto incluye el script de seed en:

- `scripts/seed-postgres.sql`

Como no se asume instalacion local de `psql`, ejecutar el seed directamente en el contenedor:

```bash
docker exec -i xplorenow psql -U xplorenow -d xplorenow < scripts/seed-postgres.sql
```

Notas:

- El script limpia e inserta datos de prueba (roles, usuarios, destinos, actividades, schedules, imagenes, preferencias y reservas).
- Se puede re-ejecutar para resetear datos de demo.

### 2.4 Levantar API

```bash
mvn spring-boot:run
```

### 2.5 Flujo rapido recomendado para entorno local

```bash
docker compose up -d
mvn spring-boot:run
docker exec -i xplorenow psql -U xplorenow -d xplorenow < scripts/seed-postgres.sql
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
- Endpoint: `/api/v1/activities/{activityId}/schedules`
- Auth requerida: Si

Query params:

- `date` (opcional, formato `yyyy-MM-dd`)

Reglas de negocio:

- Devuelve solo schedules futuros.
- Devuelve solo schedules con `availableSpots > 0`.
- Si `date` se envia, filtra por ese dia.

Ejemplo:

```http
GET /api/v1/activities/10/schedules?date=2026-05-10
```

Response `200 OK`:

```json
[
  {
    "scheduleId": 5,
    "date": "2026-05-10",
    "time": "10:00",
    "availableSpots": 12,
    "totalSpots": 20
  },
  {
    "scheduleId": 6,
    "date": "2026-05-10",
    "time": "15:00",
    "availableSpots": 5,
    "totalSpots": 20
  }
]
```

## 5.4 Actividades destacadas

- Metodo: `GET`
- Endpoint: `/api/v1/activities/featured`
- Auth requerida: Si

Query params:

- `userId` (requerido)
- `page`, `size`, `sortBy`, `direction` (opcionales)

Response: `Page<ActivitySummaryDto>`.

## 6. Perfil del viajero

Todos los endpoints de perfil requieren JWT y usan el usuario autenticado del token.

- `GET /api/v1/profile`: obtiene datos del perfil + resumen de reservas.
- `PUT /api/v1/profile`: actualiza `firstName`, `lastName`, `phone`, `profilePictureUrl`.
- `PUT /api/v1/profile/preferences`: reemplaza completamente preferencias de viaje.

Request ejemplo para preferencias:

```json
{
  "preferences": ["ADVENTURE", "CULTURE", "RELAX"]
}
```

Valores posibles de preferencias:

- `ADVENTURE`
- `CULTURE`
- `GASTRONOMY`
- `NATURE`
- `RELAX`

## 7. Reservas

Todos los endpoints de reservas requieren JWT y siempre operan sobre el usuario autenticado.

- `POST /api/v1/reservations`
- `DELETE /api/v1/reservations/{reservationId}`
- `GET /api/v1/reservations/my?status=&page=&size=&sortBy=&direction=`
- `GET /api/v1/reservations/{reservationId}`

Request ejemplo para crear reserva:

```json
{
  "activityId": 10,
  "scheduleId": 5,
  "participantsCount": 2
}
```

Flujo recomendado para crear reserva:

1. `GET /api/v1/activities/{activityId}/schedules` (opcionalmente con `?date=`)
2. Elegir un `scheduleId` disponible de la respuesta
3. `POST /api/v1/reservations` con `activityId`, `scheduleId`, `participantsCount`

Notas:

- Si no hay cupos suficientes, responde `409 Conflict`.
- Cancelar reserva devuelve cupos y registra evento de cambio.
- Estados posibles: `CONFIRMED`, `CANCELLED`, `COMPLETED`.
- Existe un job horario que transiciona `CONFIRMED -> COMPLETED` cuando el `endDateTime` del schedule ya paso.

## 8. Historial

El historial corresponde a reservas con estado `COMPLETED`.

- `GET /api/v1/history?fromDate=&toDate=&destinationId=&page=&size=`
- `GET /api/v1/history/{reservationId}`

## 9. Ratings

- `POST /api/v1/ratings`
- `GET /api/v1/ratings/pending`

Reglas principales:

- Solo reservas `COMPLETED` del usuario autenticado.
- Ventana de 48 horas desde finalizacion del schedule.
- Una sola calificacion por reserva (`409` si ya existe).

Request ejemplo:

```json
{
  "reservationId": 50,
  "activityStars": 5,
  "guideStars": 4,
  "comment": "Excelente experiencia, el guia fue muy ameno."
}
```

## 10. Swagger y contrato de errores

Todos los endpoints nuevos estan documentados en Swagger/OpenAPI:

- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`

Formato estandar de error mantenido por `GlobalExceptionHandler`.

## 11. Modelo de errores

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
