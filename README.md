# API Fintech

Una API REST moderna para gestión de cuentas financieras y clientes, desarrollada con **Spring Boot 4.0.6** y **Java 21**.

## 🎯 Descripción

API Fintech proporciona servicios para:
- **Gestión de Cuentas**: crear, consultar y eliminar cuentas bancarias
- **Soporte Multimoneda**: operaciones en ARS y USD con conversión de divisas en tiempo real
- **Integración con APIs Externas**: cotización de dólar oficial desde [DolarAPI](https://dolarapi.com)
- **Persistencia de Datos**: base de datos MySQL con JPA/Hibernate
- **Arquitectura Escalable**: separación clara entre capas (Controller → Service → Repository)

## 📋 Requisitos Previos

- **Java 21** o superior ([descargar](https://www.oracle.com/java/technologies/downloads/#java21))
- **Maven 3.9+** (incluido Maven Wrapper en el proyecto)
- **Docker & Docker Compose** (para ejecutar MySQL)
- **Git** (opcional, para clonar el repositorio)

## 🚀 Instalación y Configuración

### 1. Clonar o descargar el proyecto

```bash
# Si tienes Git
git clone <https://github.com/ladyshanai/api-fintech.git>
cd api-fintech

```

### 2. Levantar la base de datos con Docker Compose

Asegúrate de tener Docker Running en tu sistema, luego ejecuta:

```bash
docker-compose up -d
```

Esto inicia un contenedor MySQL con:
- **Usuario**: `api_user`
- **Contraseña**: `api_password`
- **Base de Datos**: `api_fintech`
- **Puerto**: `3306`

Para verificar que está running:

```bash
docker-compose ps
```

Para detener los contenedores:

```bash
docker-compose down
```

### 3. Compilar el proyecto

Usa el Maven Wrapper incluido (no necesitas Maven instalado globalmente):

```bash
# Windows (PowerShell)
.\mvnw clean package

# Windows (CMD)
mvnw.cmd clean package

# Linux/Mac
./mvnw clean package
```

## ▶️ Ejecución

### Opción A: Ejecutar con Maven

```bash
# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Opción B: Ejecutar el JAR compilado

```bash
# Primero compilar
.\mvnw package

# Luego ejecutar
java -jar target/api-0.0.1-SNAPSHOT.jar
```

La API estará disponible en: **http://localhost:8080**

## 📊 Estructura del Proyecto

```
api-fintech/
├── src/
│   ├── main/
│   │   ├── java/com/fintech/api/
│   │   │   ├── ApiApplication.java          # Punto de entrada
│   │   │   ├── controller/
│   │   │   │   └── AccountController.java   # Endpoints REST
│   │   │   ├── service/
│   │   │   │   └── AccountService.java      # Lógica de negocio
│   │   │   ├── repository/                  # Acceso a datos (JPA)
│   │   │   ├── entity/                      # Modelos JPA
│   │   │   │   ├── AccountEntity.java
│   │   │   │   └── ClientEntity.java
│   │   │   ├── dto/                         # Data Transfer Objects
│   │   │   │   ├── AccountRequest.java
│   │   │   │   ├── AccountResponse.java
│   │   │   │   └── ...
│   │   │   ├── client/
│   │   │   │   └── DolarApiClient.java      # Cliente HTTP externo
│   │   │   └── enums/
│   │   │       └── Currency.java
│   │   └── resources/
│   │       ├── application.yaml             # Configuración
│   │       └── db/init.sql                  # Script inicial de BD
│   └── test/
│       └── java/com/fintech/api/            # Tests unitarios
├── docker-compose.yml                       # Configuración Docker
├── pom.xml                                  # Dependencias Maven
└── mvnw / mvnw.cmd                          # Maven Wrapper
```

## 🔌 API Endpoints

### Cuentas

#### 1. Obtener todas las cuentas

```http
GET /api/v1/accounts
```

**Respuesta:**
```json
[
  {
    "accountId": 1,
    "clientId": 1,
    "clientFirstName": "Juan",
    "accountNumber": "1234567890",
    "currency": "ARS",
    "balance": 1000.00,
    "balanceInDolar": 0,
    "active": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Obtener una cuenta por ID

```http
GET /api/v1/accounts/{id}
```

**Ejemplo:**
```http
GET /api/v1/accounts/1
```

**Respuesta:**
```json
{
  "accountId": 1,
  "clientId": 1,
  "clientFirstName": "Juan",
  "accountNumber": "1234567890",
  "currency": "ARS",
  "balance": 1000.00,
  "balanceInDolar": 23.04,
  "active": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Crear una nueva cuenta

```http
POST /api/v1/accounts
Content-Type: application/json
```

**Payload:**
```json
{
  "clientId": 1,
  "accountNumber": "9876543210",
  "currency": "USD",
  "balance": 500.00
}
```

**Respuesta:** `200 OK` con los datos de la cuenta creada

#### 4. Eliminar una cuenta

```http
DELETE /api/v1/accounts/{id}
```

**Ejemplo:**
```http
DELETE /api/v1/accounts/1
```

**Respuesta:** `204 No Content`

## 🧪 Testing

Ejecutar los tests unitarios:

```bash
# Windows
.\mvnw test

# Linux/Mac
./mvnw test
```

Test disponibles:
- `ApiApplicationTests.java` - Tests de arranque
- `AccountServiceTest.java` - Tests del servicio
- `AccountServiceParametrizedTest.java` - Tests parametrizados
- `DolarApiClientTest.java` - Tests del cliente HTTP
- `DolarModelTest.java` - Tests del modelo de dólar

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 4.0.6 | Framework web |
| **Spring Data JPA** | - | Acceso a datos |
| **Hibernate** | 6.x | ORM |
| **MySQL** | 8.0 | Base de datos |
| **MySQL Connector/J** | - | JDBC Driver |
| **Docker** | - | Contenerización |
| **Maven** | 3.9+ | Gestor de dependencias |

## 📝 Configuración de Base de Datos

La configuración se encuentra en `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/api_fintech?useSSL=false&serverTimezone=UTC
    username: api_user
    password: api_password
  jpa:
    hibernate:
      ddl-auto: validate
```

### Scripts de inicialización

El archivo `src/main/resources/db/init.sql` contiene:
- Creación de tablas (`client`, `account`)
- Datos de prueba iniciales
- Se ejecuta automáticamente al iniciar el contenedor Docker

## 🔄 Características Principales

### Conversión de Divisas
- Las cuentas en USD se convierten automáticamente a ARS usando la cotización oficial
- La API consulta `https://dolarapi.com/v1/dolares/oficial` en tiempo real
- El resultado se devuelve en el campo `balanceInDolar`

### Validaciones
- Las cuentas se asocian a clientes existentes
- Los números de cuenta son únicos
- Solo se pueden crear cuentas con monedas válidas (ARS/USD)



---


