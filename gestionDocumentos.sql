-- Base de datos: springboot
DROP DATABASE IF EXISTS springboot;
CREATE DATABASE springboot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE springboot;

CREATE TABLE usuarios (
  id_usuario bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  nombre_usuario varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
  password_hash varchar(255) NOT NULL,
  creado_en timestamp DEFAULT CURRENT_TIMESTAMP,
  KEY idx_nombre_usuario (nombre_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario_datos (
  id_usuario bigint UNSIGNED PRIMARY KEY,
  nombre varchar(100) NOT NULL,
  apellido varchar(100) NOT NULL,
  correo varchar(255) NOT NULL UNIQUE,
  foto longblob NULL COMMENT 'NULL = sin foto de perfil',
  activo boolean DEFAULT true,
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
  id_rol int UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  nombre varchar(50) NOT NULL UNIQUE,
  descripcion varchar(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permisos (
  id_permiso int UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  nombre varchar(100) NOT NULL UNIQUE,
  descripcion varchar(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rol_permiso (
  id_rol int UNSIGNED NOT NULL,
  id_permiso int UNSIGNED NOT NULL,
  PRIMARY KEY (id_rol, id_permiso),
  FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE,
  FOREIGN KEY (id_permiso) REFERENCES permisos(id_permiso) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario_rol (
  id_usuario bigint UNSIGNED NOT NULL,
  id_rol int UNSIGNED NOT NULL,
  PRIMARY KEY (id_usuario, id_rol),
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE estados (
  id_estado varchar(30) PRIMARY KEY,
  descripcion varchar(255),
  es_final boolean DEFAULT false
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE documentos (
  id_doc bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  creado_por bigint UNSIGNED NOT NULL,
  titulo varchar(255) NOT NULL,
  ubicacion varchar(500) NOT NULL,
  estado_actual varchar(30) NOT NULL,
  version int UNSIGNED DEFAULT 1,
  creado_en datetime DEFAULT CURRENT_TIMESTAMP,
  actualizado_en datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_estado (estado_actual),
  INDEX idx_creado_por (creado_por),
  FOREIGN KEY (creado_por) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
  FOREIGN KEY (estado_actual) REFERENCES estados(id_estado) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transiciones (
  id_transicion int UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  desde varchar(30) NOT NULL,
  hacia varchar(30) NOT NULL,
  requiere_firma boolean DEFAULT false,
  activo boolean DEFAULT true,
  UNIQUE KEY uniq_transicion (desde, hacia),
  FOREIGN KEY (desde) REFERENCES estados(id_estado) ON DELETE RESTRICT,
  FOREIGN KEY (hacia) REFERENCES estados(id_estado) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transicion_autorizada (
  id_rol int UNSIGNED NOT NULL,
  id_transicion int UNSIGNED NOT NULL,
  PRIMARY KEY (id_rol, id_transicion),
  INDEX idx_transicion (id_transicion),
  FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE,
  FOREIGN KEY (id_transicion) REFERENCES transiciones(id_transicion) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historial_documento (
  id_hist bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  id_doc bigint UNSIGNED NOT NULL,
  id_transicion int UNSIGNED NOT NULL,
  estado_anterior varchar(30) NOT NULL,
  estado_nuevo varchar(30) NOT NULL,
  cambiado_por bigint UNSIGNED NOT NULL,
  cambiado_en datetime DEFAULT CURRENT_TIMESTAMP,
  comentario text,
  FOREIGN KEY (id_doc) REFERENCES documentos(id_doc) ON DELETE CASCADE,
  FOREIGN KEY (id_transicion) REFERENCES transiciones(id_transicion) ON DELETE RESTRICT,
  FOREIGN KEY (estado_anterior) REFERENCES estados(id_estado) ON DELETE RESTRICT,
  FOREIGN KEY (estado_nuevo) REFERENCES estados(id_estado) ON DELETE RESTRICT,
  FOREIGN KEY (cambiado_por) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos iniciales recomendados
INSERT INTO estados (id_estado, descripcion, es_final) VALUES
('primera_vista', 'Documento recién subido', false),
('verificado', 'Revisado y correcto', false),
('firmado', 'Firmado digitalmente', true),
('rechazado', 'Rechazado', true);

INSERT INTO roles (nombre, descripcion) VALUES
('admin', 'Administrador del sistema'),
('firmante', 'Puede firmar documentos'),
('revisor', 'Puede verificar documentos'),
('usuario', 'Usuario normal');

INSERT INTO usuarios (nombre_usuario, password_hash) VALUES
('admin', '$2a$10$5U7tkLjxJvLCYOtJk9H6beSzF/Lo6GrCVuVi//3yQtJCb52ZS4nDi'), -- admin123
('usuario', '$2a$10$0vsaNQre58QktF9UfPPxkeyM6E5qUPpoFVIhSK20WfwGRJO/SyWRW'); -- usuario123

INSERT INTO usuario_datos (id_usuario, nombre, apellido, correo, foto, activo) VALUES
(1, 'Carlos', 'Administrador', 'admin@empresa.com', NULL, true),
(2, 'Pedro', 'Usuario', 'usuario@empresa.com', NULL, true);

INSERT INTO usuario_rol (id_usuario, id_rol)
VALUES (1, 1);  -- admin tiene rol admin
INSERT INTO usuario_rol (id_usuario, id_rol)
VALUES (2, 4);  -- usuario tiene rol usuario

