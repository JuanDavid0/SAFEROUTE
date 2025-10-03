-- Datos iniciales del sistema
-- seed_initial_data.sql

-- Insertar roles básicos
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrador del sistema con todos los permisos'),
('DRIVER', 'Conductor de vehículos de transporte'),
('USER', 'Usuario final que solicita servicios de transporte'),
('DISPATCHER', 'Despachador que asigna y coordina servicios');

-- Insertar usuario administrador por defecto
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, role_id) VALUES 
('admin', 'admin@saferoute.com', '$2a$10$DJmYPODknQGCi2LlNJTbX.JZjxI5LJ5pKE5l.9VDWJ3K8q3p2x1mG', 'Admin', 'System', '+1234567890', 
 (SELECT id FROM roles WHERE name = 'ADMIN'));

-- Insertar algunos usuarios de ejemplo para pruebas
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, role_id) VALUES 
('driver1', 'driver1@saferoute.com', '$2a$10$DJmYPODknQGCi2LlNJTbX.JZjxI5LJ5pKE5l.9VDWJ3K8q3p2x1mG', 'Juan', 'Pérez', '+1234567891', 
 (SELECT id FROM roles WHERE name = 'DRIVER')),
('user1', 'user1@saferoute.com', '$2a$10$DJmYPODknQGCi2LlNJTbX.JZjxI5LJ5pKE5l.9VDWJ3K8q3p2x1mG', 'María', 'González', '+1234567892', 
 (SELECT id FROM roles WHERE name = 'USER')),
('dispatcher1', 'dispatcher1@saferoute.com', '$2a$10$DJmYPODknQGCi2LlNJTbX.JZjxI5LJ5pKE5l.9VDWJ3K8q3p2x1mG', 'Carlos', 'Rodríguez', '+1234567893', 
 (SELECT id FROM roles WHERE name = 'DISPATCHER'));