# 📋 Documentación de Endpoints - ResuelveYa API

**Base URL:** `http://localhost:8080`  
**Versión:** v1  

---

## 🔐 Autenticación y Seguridad

Todos los endpoints protegidos requieren incluir el token JWT en el header:

```http
Authorization: Bearer <token>
```

### Roles del Sistema:
- **`ADMIN`**: Acceso completo a gestión de usuarios y CRUD de categorías.
- **`TECNICO`**: Gestión de perfil profesional, métodos de pago (Yape/Plin) y CRUD de sus propios servicios ofrecidos.
- **`CLIENTE`**: Actualización de perfil personal, consulta de detalle completo de técnicos y publicación de reseñas.
- **`VISITANTE` (No autenticado)**: Búsqueda pública de servicios, listado público de técnicos y consulta de categorías.

---

## 🔓 1. Autenticación Pública

### 1.1. Registro de usuario
Permite el registro público seleccionando el rol (`CLIENTE` o `TECNICO`).
- **Método:** `POST`
- **Ruta:** `/api/v1/auth/registro`
- **Permisos:** Público (Sin autenticación)

**Body (Request):**
```json
{
  "nombre": "Carlos Gómez",
  "email": "carlos@ejemplo.com",
  "password": "Password123",
  "telefono": "987654321",
  "rol": "TECNICO"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "nombre": "Carlos Gómez",
  "email": "carlos@ejemplo.com",
  "telefono": "987654321",
  "fotoUrl": null,
  "direccion": null,
  "distrito": null,
  "ciudad": null,
  "rol": "TECNICO"
}
```

---

### 1.2. Iniciar sesión (Login)
- **Método:** `POST`
- **Ruta:** `/api/v1/auth/login`
- **Permisos:** Público (Sin autenticación)

**Body (Request):**
```json
{
  "email": "carlos@ejemplo.com",
  "password": "Password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiresIn": 3600000,
  "email": "carlos@ejemplo.com",
  "rol": "TECNICO"
}
```

---

### 1.3. Iniciar sesión con Firebase (App Móvil)
Permite a la app móvil autenticarse usando Firebase Auth (Google Sign-In, Email, etc.) y obtener un token JWT de ResuelveYa.
Si el usuario no existe en la base de datos, se registra automáticamente con el rol indicado (`CLIENTE` por defecto).
- **Método:** `POST`
- **Ruta:** `/api/v1/auth/firebase-login`
- **Permisos:** Público (Sin autenticación)

**Body (Request):**
```json
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "rol": "CLIENTE",
  "nombre": "Carlos Gómez",
  "telefono": "987654321",
  "direccion": "Av. Las Flores 123",
  "ciudad": "Lima"
}
```
> **Nota:** Todos los campos salvo `firebaseToken` son opcionales:
> - `rol`: `CLIENTE` (por defecto) o `TECNICO`.
> - `nombre`: Nombre del usuario (si no se envía, se toma el nombre configurado en Firebase o el prefijo del correo).
> - `telefono`, `direccion`, `ciudad`: Datos del perfil que se guardan directamente en MySQL al registrarse o completarse.


**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiresIn": 3600000,
  "email": "carlos@gmail.com",
  "rol": "CLIENTE",
  "id": 15,
  "nombre": "Carlos Gómez",
  "telefono": null
}
```

**Configuración del servidor:**
- Variable de entorno `FIREBASE_CONFIG_PATH` apuntando al archivo `serviceAccountKey.json` de Firebase.
- O colocar el archivo `firebase-service-account.json` en `src/main/resources/`.

---

## 🔍 2. Catálogo Público y Búsqueda (Visitantes)

### 2.1. Búsqueda pública de servicios
Permite buscar servicios en tiempo real examinando de forma simultánea el **título**, la **descripción** y la **categoría**. Oculta datos sensibles de contacto para visitantes.
- **Método:** `GET`
- **Ruta:** `/api/v1/public/servicios`
- **Permisos:** Público

**Parámetros de consulta (Query Params - Opcionales):**
- `q`: Término de búsqueda (ej. `"gas"`, `"fuga"`, `"mantenimiento"`).
- `categoriaId`: ID de la categoría (Long).
- `distrito`: Nombre del distrito o ciudad (ej. `"Surco"`).
- `precioMin`: Precio mínimo (Decimal).
- `precioMax`: Precio máximo (Decimal).
- `page`: Número de página (0-indexed, default: `0`).
- `size`: Elementos por página (default: `10`).
- `sort`: Campo de ordenamiento (ej. `precioEstimado,asc` o `createdAt,desc`).

**Ejemplo de Request:**
```http
GET /api/v1/public/servicios?q=fuga&distrito=Surco&page=0&size=10
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 10,
      "titulo": "Detección y reparación de fuga de gas",
      "descripcion": "Revisión integral de tuberías de cobre y válvulas de paso.",
      "precioEstimado": 80.00,
      "tiempoEstimado": "2 horas",
      "categoriaId": 2,
      "categoriaNombre": "Gasfitería",
      "tecnicoId": 5,
      "tecnicoNombre": "Carlos Gómez",
      "tecnicoFotoUrl": "https://mi-servidor.com/fotos/carlos.jpg",
      "tecnicoDistrito": "Santiago de Surco",
      "tecnicoCiudad": "Lima",
      "tecnicoCalificacionPromedio": 4.8
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### 2.2. Directorio público de técnicos
- **Método:** `GET`
- **Ruta:** `/api/v1/public/tecnicos`
- **Permisos:** Público

**Parámetros de consulta:** `categoriaId`, `nombre`, `page`, `size`, `sort`.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 5,
      "nombre": "Carlos Gómez",
      "fotoUrl": "https://mi-servidor.com/fotos/carlos.jpg",
      "distrito": "Santiago de Surco",
      "ciudad": "Lima",
      "presentacion": "Gasfitero técnico con más de 8 años de experiencia.",
      "aniosExperiencia": 8,
      "calificacionPromedio": 4.8,
      "totalResenias": 14,
      "especialidadId": 2,
      "especialidadNombre": "Gasfitería"
    }
  ]
}
```

---

## 👤 3. Perfil del Usuario Autenticado

Disponible para cualquier usuario logueado (`CLIENTE`, `TECNICO`, `ADMIN`).

### 3.1. Obtener mi perfil
- **Método:** `GET`
- **Ruta:** `/api/v1/perfil/me`
- **Permisos:** Requiere Token JWT (`authenticated`)

**Response (200 OK):**
```json
{
  "id": 5,
  "nombre": "Carlos Gómez",
  "email": "carlos@ejemplo.com",
  "telefono": "987654321",
  "fotoUrl": "https://mi-servidor.com/fotos/carlos.jpg",
  "direccion": "Av. Benavides 1234",
  "distrito": "Santiago de Surco",
  "ciudad": "Lima",
  "rol": "TECNICO",
  "presentacion": "Gasfitero certificado",
  "aniosExperiencia": 8,
  "calificacionPromedio": 4.8,
  "yapeNumero": "987654321",
  "plinNumero": "987654321",
  "titularPago": "Carlos Gómez R.",
  "especialidadId": 2,
  "especialidadNombre": "Gasfitería"
}
```

---

### 3.2. Actualizar mis datos personales
- **Método:** `PUT`
- **Ruta:** `/api/v1/perfil/me`
- **Permisos:** Requiere Token JWT (`authenticated`)

**Body (Request):**
```json
{
  "nombre": "Carlos Gómez Actualizado",
  "telefono": "987654321",
  "fotoUrl": "https://mi-servidor.com/fotos/carlos2.jpg",
  "direccion": "Av. Benavides 5678",
  "distrito": "Surco",
  "ciudad": "Lima"
}
```

---

## 🛠️ 4. Módulo de Técnicos (`ROLE_TECNICO`)

### 4.1. Configurar perfil profesional y métodos de pago
- **Método:** `PUT`
- **Ruta:** `/api/v1/tecnico/perfil`
- **Permisos:** Requiere rol `TECNICO`

**Body (Request):**
```json
{
  "presentacion": "Especialista en instalaciones sanitarias y de gas residencial.",
  "aniosExperiencia": 8,
  "especialidadId": 2,
  "yapeNumero": "987654321",
  "plinNumero": "987654321",
  "titularPago": "Carlos Gómez R.",
  "fotoUrl": "https://mi-servidor.com/fotos/carlos.jpg"
}
```

---

### 4.2. Listar mis servicios ofrecidos
- **Método:** `GET`
- **Ruta:** `/api/v1/tecnico/servicios`
- **Permisos:** Requiere rol `TECNICO`

---

### 4.3. Crear nuevo servicio ofrecido
- **Método:** `POST`
- **Ruta:** `/api/v1/tecnico/servicios`
- **Permisos:** Requiere rol `TECNICO`

**Body (Request):**
```json
{
  "titulo": "Instalación de terma a gas",
  "descripcion": "Instalación completa, verificación de presión y prueba de encendido.",
  "precioEstimado": 120.00,
  "tiempoEstimado": "3 horas",
  "categoriaId": 2,
  "activo": true
}
```

---

### 4.4. Modificar servicio ofrecido
- **Método:** `PUT`
- **Ruta:** `/api/v1/tecnico/servicios/{id}`
- **Permisos:** Requiere rol `TECNICO` (solo puede modificar sus propios servicios)

---

### 4.5. Eliminar servicio ofrecido
- **Método:** `DELETE`
- **Ruta:** `/api/v1/tecnico/servicios/{id}`
- **Permisos:** Requiere rol `TECNICO` (solo puede eliminar sus propios servicios)

---

## 📖 5. Ficha Técnica Completa y Reseñas

### 5.1. Consultar información completa de un técnico
Permite a usuarios registrados ver datos de contacto (teléfono, email), métodos de pago (Yape/Plin), catálogo de servicios y reseñas de clientes.
- **Método:** `GET`
- **Ruta:** `/api/v1/tecnicos/{id}/completo`
- **Permisos:** Requiere Token JWT (`authenticated`)

**Response (200 OK):**
```json
{
  "id": 5,
  "nombre": "Carlos Gómez",
  "email": "carlos@ejemplo.com",
  "telefono": "987654321",
  "fotoUrl": "https://mi-servidor.com/fotos/carlos.jpg",
  "direccion": "Av. Benavides 1234",
  "distrito": "Santiago de Surco",
  "ciudad": "Lima",
  "presentacion": "Gasfitero certificado con 8 años de experiencia.",
  "aniosExperiencia": 8,
  "calificacionPromedio": 4.8,
  "yapeNumero": "987654321",
  "plinNumero": "987654321",
  "titularPago": "Carlos Gómez R.",
  "especialidadId": 2,
  "especialidadNombre": "Gasfitería",
  "servicios": [
    {
      "id": 10,
      "titulo": "Detección y reparación de fuga de gas",
      "descripcion": "Revisión integral de tuberías de cobre.",
      "precioEstimado": 80.00,
      "tiempoEstimado": "2 horas",
      "activo": true,
      "categoriaId": 2,
      "categoriaNombre": "Gasfitería",
      "tecnicoId": 5,
      "tecnicoNombre": "Carlos Gómez",
      "createdAt": "2026-08-18T10:00:00"
    }
  ],
  "resenias": [
    {
      "id": 1,
      "calificacion": 5,
      "comentario": "Excelente servicio, muy puntual y solucionó la fuga de inmediato.",
      "clienteId": 8,
      "clienteNombre": "Ana Torres",
      "clienteFotoUrl": null,
      "tecnicoId": 5,
      "createdAt": "2026-08-18T11:30:00"
    }
  ]
}
```

---

### 5.2. Calificar y dejar reseña a un servicio o técnico
- **Método:** `POST`
- **Ruta:** `/api/v1/resenias`
- **Permisos:** Requiere rol `CLIENTE`

**Body (Request):**
```json
{
  "servicioId": 10,
  "tecnicoId": 5,
  "calificacion": 5,
  "comentario": "Excelente servicio, muy profesional y solucionó la fuga de inmediato."
}
```

---

### 5.3. Actualizar una reseña existente (Edición por el Autor)
- **Método:** `PUT`
- **Ruta:** `/api/v1/resenias/{id}`
- **Permisos:** Requiere rol `CLIENTE` (debe ser el cliente autor de la reseña)

**Body (Request):**
```json
{
  "calificacion": 5,
  "comentario": "Actualización: Servicio impecable con excelente garantía."
}
```

---

### 5.4. Listar reseñas de un servicio específico
- **Método:** `GET`
- **Ruta:** `/api/v1/resenias/servicio/{servicioId}`
- **Permisos:** Público

---

### 5.5. Listar reseñas de un técnico
- **Método:** `GET`
- **Ruta:** `/api/v1/resenias/tecnico/{tecnicoId}`
- **Permisos:** Público

---

## 📁 6. Categorías / Especialidades

- `GET /api/v1/categorias`: Listar todas las categorías (Público).
- `GET /api/v1/categorias/{id}`: Obtener categoría por ID (Público).
- `POST /api/v1/categorias`: Crear categoría (**Requiere ADMIN**).
- `PUT /api/v1/categorias/{id}`: Modificar categoría (**Requiere ADMIN**).
- `DELETE /api/v1/categorias/{id}`: Eliminar categoría (**Requiere ADMIN**).

**Body (Request para POST / PUT):**
```json
{
  "nombre": "Electricidad",
  "descripcion": "Instalaciones eléctricas, cableado y mantenimiento de tableros."
}
```

---

## 👥 7. Gestión de Usuarios (**Requiere ADMIN**)

- `GET /api/v1/usuarios`: Listar todos los usuarios.
- `GET /api/v1/usuarios/{id}`: Obtener usuario por ID.
- `POST /api/v1/usuarios`: Crear usuario (con contraseña obligatoria).
- `PUT /api/v1/usuarios/{id}`: Actualizar usuario (con actualización opcional de contraseña).
- `DELETE /api/v1/usuarios/{id}`: Eliminar usuario.
- `GET /api/v1/usuarios/consulta?nombre=carlos&page=0&size=5`: Búsqueda paginada.

---

---

## 📲 8. Solicitudes de Servicio (Móvil / Web)

### 8.1. Solicitar un servicio
Permite a un cliente solicitar un servicio a un técnico.
- **Método:** `POST`
- **Ruta:** `/api/v1/solicitudes`
- **Permisos:** Requiere rol `CLIENTE`

**Body (Request):**
```json
{
  "servicioId": 10,
  "tecnicoId": 5,
  "descripcion": "Fuga de agua en el lavadero de la cocina.",
  "direccionAtencion": "Av. Benavides 1234, Dpto 301, Surco",
  "latitud": -12.1298,
  "longitud": -77.0282,
  "fechaProgramada": "2026-10-20T10:00:00",
  "precioAcordado": 80.00
}
```

---

### 8.2. Listar mis solicitudes (Cliente)
- **Método:** `GET`
- **Ruta:** `/api/v1/solicitudes/cliente?estado=PENDIENTE` (parámetro `estado` opcional)
- **Permisos:** Requiere rol `CLIENTE`

---

### 8.3. Bandeja de solicitudes entrantes (Técnico / Proveedor)
- **Método:** `GET`
- **Ruta:** `/api/v1/solicitudes/tecnico?estado=PENDIENTE` (parámetro `estado` opcional)
- **Permisos:** Requiere rol `TECNICO`

---

### 8.4. Ver detalle de una solicitud
- **Método:** `GET`
- **Ruta:** `/api/v1/solicitudes/{id}`
- **Permisos:** Autenticado (Cliente emisor, Técnico destinatario o Admin)

---

### 8.5. Aceptar solicitud (Técnico / Proveedor)
- **Método:** `PATCH`
- **Ruta:** `/api/v1/solicitudes/{id}/aceptar`
- **Permisos:** Requiere rol `TECNICO` (solo el técnico asignado)

---

### 8.6. Rechazar solicitud (Técnico / Proveedor)
- **Método:** `PATCH`
- **Ruta:** `/api/v1/solicitudes/{id}/rechazar`
- **Permisos:** Requiere rol `TECNICO` (solo el técnico asignado)

**Body (Request - Opcional):**
```json
{
  "motivo": "Sin disponibilidad en la fecha y hora seleccionada."
}
```

---

### 8.7. Cancelar solicitud (Cliente)
- **Método:** `PATCH`
- **Ruta:** `/api/v1/solicitudes/{id}/cancelar`
- **Permisos:** Requiere rol `CLIENTE` (solo el cliente solicitante)

---

### 8.8. Completar / Finalizar servicio
- **Método:** `PATCH`
- **Ruta:** `/api/v1/solicitudes/{id}/completar`
- **Permisos:** Autenticado (Cliente o Técnico involucrado)

---

## 📊 Matriz de Permisos

| Endpoint | Método | Rol Permitido |
|---|---|---|
| `/api/v1/auth/**` | POST | Público |
| `/api/v1/public/**` | GET | Público |
| `/api/v1/categorias/**` | GET | Público |
| `/api/v1/categorias/**` | POST, PUT, DELETE | `ROLE_ADMIN` |
| `/api/v1/usuarios/**` | GET, POST, PUT, DELETE | `ROLE_ADMIN` |
| `/api/v1/tecnico/**` | GET, POST, PUT, DELETE | `ROLE_TECNICO` |
| `/api/v1/resenias/**` | GET | Público |
| `/api/v1/resenias` | POST | `ROLE_CLIENTE` |
| `/api/v1/resenias/{id}` | PUT | `ROLE_CLIENTE` (Autor) |
| `/api/v1/perfil/**` | GET, PUT | `ROLE_CLIENTE`, `ROLE_TECNICO`, `ROLE_ADMIN` |
| `/api/v1/tecnicos/{id}/completo` | GET | `ROLE_CLIENTE`, `ROLE_TECNICO`, `ROLE_ADMIN` |
| `/api/v1/solicitudes` | POST | `ROLE_CLIENTE` |
| `/api/v1/solicitudes/cliente/**` | GET | `ROLE_CLIENTE` |
| `/api/v1/solicitudes/tecnico/**` | GET | `ROLE_TECNICO` |
| `/api/v1/solicitudes/{id}/aceptar` | PATCH | `ROLE_TECNICO` |
| `/api/v1/solicitudes/{id}/rechazar` | PATCH | `ROLE_TECNICO` |
| `/api/v1/solicitudes/{id}/cancelar` | PATCH | `ROLE_CLIENTE` |
| `/api/v1/solicitudes/{id}/completar`| PATCH | `ROLE_CLIENTE`, `ROLE_TECNICO` |
| `/api/v1/solicitudes/{id}` | GET | `ROLE_CLIENTE`, `ROLE_TECNICO`, `ROLE_ADMIN` |


