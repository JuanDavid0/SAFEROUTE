# Frontend Dockerfile
FROM node:20-alpine3.19

WORKDIR /app

# Copiar package files
COPY frontend/package*.json ./

# Instalar dependencias
RUN npm ci --only=production

# Copiar código fuente
COPY frontend/ .

# Construir aplicación
RUN npm run build

# Ejecutar aplicación
EXPOSE 3000
CMD ["npm", "start"]