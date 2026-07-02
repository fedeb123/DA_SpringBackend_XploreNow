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

## 12. Voucher digital y check-in por QR

Este backend expone el voucher digital de una reserva y el flujo de check-in por QR para validar asistencia en el punto de encuentro.

### 12.1 Obtener voucher digital

- Metodo: `GET`
- Endpoint: `/api/v1/reservations/{reservationId}/voucher`
- Auth requerida: Si

Reglas:

- La reserva debe pertenecer al usuario autenticado.
- Solo las reservas `CONFIRMED` o `COMPLETED` tienen voucher valido.

Response `200 OK` (`VoucherDto`):

```json
{
  "reservationId": 50,
  "activityName": "Free Tour Centro Historico",
  "date": "2026-05-10",
  "time": "10:00",
  "meetingPoint": "Plaza de Mayo",
  "guideName": "Maria Perez",
  "participantsCount": 2,
  "reservationStatus": "CONFIRMED",
  "checkedIn": false
}
```

### 12.2 Generar código QR de check-in

- Metodo: `GET`
- Endpoint: `/api/v1/schedules/{scheduleId}/checkin-code`
- Auth requerida: Si

El QR se firma con HMAC y expira segun `checkin.qr.expiration-minutes`.

Response `200 OK`:

```json
{
  "scheduleId": 5,
  "qrContent": "eyJzY2hlZHVsZUlkIjo1fQ.c2lnbmF0dXJl",
  "expiresAt": "2026-05-10T13:00:00"
}
```

### 12.3 Escanear QR y confirmar asistencia

- Metodo: `POST`
- Endpoint: `/api/v1/checkin/scan`
- Auth requerida: Si

Request:

```json
{
  "reservationId": 50,
  "qrContent": "eyJzY2hlZHVsZUlkIjo1fQ.c2lnbmF0dXJl"
}
```

Response `200 OK`:

```json
{
  "status": "CONFIRMED",
  "reservationId": 50,
  "activityName": "Free Tour Centro Historico",
  "scannedAt": "2026-05-10T10:02:15",
  "message": "Asistencia confirmada"
}
```

Errores relevantes:

- `422` si el QR es invalido o expiro.
- `403` si la reserva no pertenece al usuario autenticado.
- `409` si la reserva no esta confirmada o si la asistencia ya fue registrada.

Nota: el punto de notificaciones push del TP se resuelve del lado de la app nativa usando la fecha/hora del voucher; no requiere backend adicional.

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

## 5.5 Validar estado de actividades favoritas (Batch Check)

- Metodo: `GET`
- Endpoint: `/api/v1/activities/saved-activities/batch`
- Auth requerida: Si (Bearer JWT)

**Purpose**: Endpoint diseñado para que el frontend consulte periódicamente el estado actual (precio y cupos disponibles) de un batch de actividades guardadas como favoritas en el cliente. El frontend compara este estado con su cache local para detectar cambios y mostrar un indicador visual de novedad.

Query params:

- `ids` (requerido, string con IDs separados por coma): `ids=10,15,22`

Ejemplo:

```http
GET /api/v1/activities/saved-activities/batch?ids=10,15,22
Authorization: Bearer <token>
```

Response `200 OK`: Array de `SavedActivityCheckDto`

```json
[
  {
    "activityId": 10,
    "price": 0,
    "availableSpots": 25,
    "currency": "ARS"
  },
  {
    "activityId": 15,
    "price": 1500,
    "availableSpots": 8,
    "currency": "ARS"
  },
  {
    "activityId": 22,
    "price": 2500,
    "availableSpots": 0,
    "currency": "ARS"
  }
]
```

**Notas de integración frontend**:

- **Worker periódico**: Recomendación implementar un `setInterval()` o similar que llame a este endpoint cada 5-10 minutos (según necesidad de reactividad).
- **Batch size**: No hay límite técnico, pero se recomienda enviar máximo 50 IDs por request para optimizar la BD.
- **Comparación local**: El endpoint devuelve el estado actual. Es responsabilidad del frontend comparar con su cache local de favoritas y detectar cambios en `price` o `availableSpots`.
- **Indicador visual**: Si detecta cambio, mostrar badge/badge notificación en la actividad.
- **IDs inválidos**: Si se envía un ID que no existe, simplemente se omite de la respuesta (no genera error).

**Flujo recomendado en frontend**:

```javascript
// 1. Obtener IDs de actividades favoritas (del localStorage o state local)
const favoriteIds = [10, 15, 22];

// 2. Cada 5 minutos, consultar estado actual
setInterval(async () => {
  const response = await fetch(
    `/api/v1/activities/saved-activities/batch?ids=${favoriteIds.join(',')}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  const currentState = await response.json();

  // 3. Comparar con estado guardado localmente
  currentState.forEach(activity => {
    const saved = favoriteState[activity.activityId];
    if (saved) {
      const priceChanged = saved.price !== activity.price;
      const spotsChanged = saved.availableSpots !== activity.availableSpots;
      
      if (priceChanged || spotsChanged) {
        // Mostrar indicador de novedad en la actividad
        showBadge(activity.activityId, 'Updated');
      }
    }
  });

  // 4. Actualizar cache local
  favoriteState = Object.fromEntries(
    currentState.map(a => [a.activityId, a])
  );
}, 5 * 60 * 1000); // 5 minutos
```

## 6. Perfil del viajero

Todos los endpoints de perfil requieren JWT y usan el usuario autenticado del token.

- `GET /api/v1/profile`: obtiene datos del perfil + resumen de reservas.
- `PUT /api/v1/profile`: actualiza `firstName`, `lastName`, `phone`, `profilePictureUrl`.
- `PUT /api/v1/profile/preferences`: reemplaza completamente preferencias de viaje.

### 6.1 Cambio de contraseña (OTP)

- Iniciar flujo (envía OTP al email registrado):

  - Metodo: `POST`
  - Endpoint: `/api/v1/profile/me/password-change/initiate`
  - Auth requerida: Sí (Bearer JWT)

  Response `200 OK` (sin body). OTP será registrado y enviado por el `OtpDeliveryService` (en desarrollo: `LoggingOtpDeliveryService`).

- Confirmar cambio de contraseña:

  - Metodo: `POST`
  - Endpoint: `/api/v1/profile/me/password-change/confirm`
  - Auth requerida: Sí (Bearer JWT)

  Request example:

  ```json
  {
    "code": "123456",
    "newPassword": "NewP@ssw0rd"
  }
  ```

  Response `200 OK`:

  ```json
  {
    "message": "Password changed successfully"
  }
  ```

Notes:
- `code` debe ser el OTP enviado al email (6 dígitos).
- `newPassword` debe cumplir con la validación mínima (8 caracteres).
- El endpoint usa el usuario autenticado; el OTP se valida contra el email registrado.

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
- Estados posibles: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`.
- Existe un job horario que transiciona `CONFIRMED -> COMPLETED` cuando el `endDateTime` del schedule ya paso.

## 8. Historial

El historial corresponde a reservas del usuario autenticado. Por defecto devuelve todos los estados, permite filtrar por `status` y se ordena de ultimo agregado a primero agregado.

- `GET /api/v1/activity/history?fromDate=&toDate=&destinationId=&status=&page=&size=`
- `GET /api/v1/activity/history/{reservationId}`

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
