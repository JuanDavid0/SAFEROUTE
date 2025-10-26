# Frontend Dockerfile
FROM node:20-alpine3.19

WORKDIR /app

# Copiar package files
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar código fuente
COPY . .

# Construir aplicación
RUN npm run build

# Ejecutar aplicación
EXPOSE 3000
CMD ["npm", "start"]