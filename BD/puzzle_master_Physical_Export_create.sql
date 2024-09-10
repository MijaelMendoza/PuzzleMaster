-- Created by Vertabelo (http://vertabelo.com)
-- Last modification date: 2024-09-10 18:13:22.307

-- tables
-- Table: Juegos
CREATE TABLE Juegos (
    cj integer NOT NULL CONSTRAINT Juegos_pk PRIMARY KEY,
    dificultad varchar(50) NOT NULL,
    tipo_juego varchar(50) NOT NULL,
    cantidad_movimientos integer NOT NULL,
    resultado boolean NOT NULL,
    experiencia_ganada integer NOT NULL,
    experiencia_perdida integer NOT NULL,
    tiempo integer NOT NULL,
    fecha_juego date NOT NULL,
    isSolverUsed boolean NOT NULL,
    Usuarios_id integer NOT NULL,
    CONSTRAINT Juegos_Usuarios FOREIGN KEY (Usuarios_id)
    REFERENCES Usuarios (id)
);

-- Table: Usuarios
CREATE TABLE Usuarios (
    id integer NOT NULL CONSTRAINT Usuarios_pk PRIMARY KEY,
    nombre_usuario varchar(50) NOT NULL,
    correo varchar(50) NOT NULL,
    foto_perfil varchar(300) NOT NULL,
    pregunta_seguridad varchar(300) NOT NULL,
    respuesta_seguridad varchar(300) NOT NULL,
    fecha_creacion date NOT NULL,
    nivel integer NOT NULL,
    experiencia_acumulada integer NOT NULL
);

-- End of file.

