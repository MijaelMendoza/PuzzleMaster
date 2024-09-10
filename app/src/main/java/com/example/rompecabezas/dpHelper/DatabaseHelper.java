package com.example.rompecabezas.dpHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "rompecabezas.db";
    private static final int DATABASE_VERSION = 1;

    // Tablas
    public static final String TABLE_USUARIOS = "Usuarios";
    public static final String TABLE_JUEGOS = "Juegos";

    // SQL para crear la tabla Usuarios
    private static final String CREATE_TABLE_USUARIOS = "CREATE TABLE " + TABLE_USUARIOS + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "nombre_usuario VARCHAR(50) NOT NULL, "
            + "correo VARCHAR(50) NOT NULL, "
            + "contrasena VARCHAR(50) NOT NULL, "
            + "foto_perfil VARCHAR(300) NOT NULL, "
            + "pregunta_seguridad VARCHAR(300) NOT NULL, "
            + "respuesta_seguridad VARCHAR(300) NOT NULL, "
            + "fecha_creacion DATE NOT NULL, "
            + "nivel INTEGER NOT NULL, "
            + "experiencia_acumulada INTEGER NOT NULL);";

    // SQL para crear la tabla Juegos
    private static final String CREATE_TABLE_JUEGOS = "CREATE TABLE " + TABLE_JUEGOS + " ("
            + "cj INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "dificultad VARCHAR(50) NOT NULL, "
            + "tipo_juego VARCHAR(50) NOT NULL, "
            + "cantidad_movimientos INTEGER NOT NULL, "
            + "resultado BOOLEAN NOT NULL, "
            + "experiencia_ganada INTEGER NOT NULL, "
            + "experiencia_perdida INTEGER NOT NULL, "
            + "tiempo INTEGER NOT NULL, "
            + "fecha_juego DATE NOT NULL, "
            + "isSolverUsed BOOLEAN NOT NULL, "
            + "Usuarios_id INTEGER NOT NULL, "
            + "FOREIGN KEY (Usuarios_id) REFERENCES Usuarios (id));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIOS);
        db.execSQL(CREATE_TABLE_JUEGOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JUEGOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }
}
