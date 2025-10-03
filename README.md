# SafeRoute

Sistema de gestión de transporte seguro desarrollado con Spring Boot (backend) y Next.js (frontend).

## Estructura del Proyecto

```
safe-route/
├── frontend/                    # Aplicación Next.js (UI)
├── backend/                     # Aplicación Spring Boot
├── database/                    # Scripts de base de datos
├── docs/                        # Documentación del sistema
├── infra/                       # Infraestructura (Docker, K8s)
└── README.md
```

## Tecnologías Utilizadas

### Frontend
- Next.js 14+ (App Router)
- TypeScript
- Tailwind CSS

### Backend
- Spring Boot 3.x
- Java 17+
- PostgreSQL
- Spring Data JPA
- Spring Security

### Infraestructura
- Docker & Docker Compose
- Kubernetes (opcional)

## Instalación y Configuración

### Prerrequisitos
- Node.js 18+
- Java 17+
- PostgreSQL 15+
- Docker (opcional)

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Base de Datos
1. Crear base de datos PostgreSQL
2. Configurar credenciales en `backend/src/main/resources/application.yml`
3. Ejecutar migraciones en `database/migrations/`

## Documentación Adicional
- [Arquitectura del Sistema](docs/arquitectura/)
- [Casos de Uso](docs/casos_uso/)
- [Configuración de Infraestructura](infra/)