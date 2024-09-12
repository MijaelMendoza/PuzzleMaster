package com.example.rompecabezas.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.rompecabezas.dpHelper.DatabaseHelper;
import com.example.rompecabezas.model.JuegoModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JuegoDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public JuegoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Abrir la base de datos
    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    // Cerrar la base de datos
    public void close() {
        dbHelper.close();
    }

    // Insertar un nuevo juego
    public long insertarJuego(JuegoModel juego) {
        ContentValues values = new ContentValues();
        values.put("dificultad", juego.getDificultad());
        values.put("tipo_juego", juego.getTipoJuego());
        values.put("cantidad_movimientos", juego.getCantidadMovimientos());
        values.put("resultado", juego.isResultado() ? 1 : 0);
        values.put("experiencia_ganada", juego.getExperienciaGanada());
        values.put("experiencia_perdida", juego.getExperienciaPerdida());
        values.put("tiempo", juego.getTiempo());
        values.put("fecha_juego", juego.getFechaJuego().getTime());
        values.put("isSolverUsed", juego.isSolverUsed() ? 1 : 0);
        values.put("Usuarios_id", juego.getUsuarioId());

        return database.insert(DatabaseHelper.TABLE_JUEGOS, null, values);
    }

    // Obtener todos los juegos
    public List<JuegoModel> obtenerJuegos() {
        List<JuegoModel> juegos = new ArrayList<>();

        // Realizar la consulta
        Cursor cursor = database.query(DatabaseHelper.TABLE_JUEGOS, null, null, null, null, null, "fecha_juego DESC");

        if (cursor.moveToFirst()) {
            do {
                JuegoModel juego = new JuegoModel();

                // Usamos getColumnIndexOrThrow para asegurarnos de que las columnas existan
                juego.setCj(cursor.getInt(cursor.getColumnIndexOrThrow("cj")));
                juego.setDificultad(cursor.getString(cursor.getColumnIndexOrThrow("dificultad")));
                juego.setTipoJuego(cursor.getString(cursor.getColumnIndexOrThrow("tipo_juego")));
                juego.setCantidadMovimientos(cursor.getInt(cursor.getColumnIndexOrThrow("cantidad_movimientos")));
                juego.setResultado(cursor.getInt(cursor.getColumnIndexOrThrow("resultado")) == 1);
                juego.setExperienciaGanada(cursor.getInt(cursor.getColumnIndexOrThrow("experiencia_ganada")));
                juego.setExperienciaPerdida(cursor.getInt(cursor.getColumnIndexOrThrow("experiencia_perdida")));
                juego.setTiempo(cursor.getInt(cursor.getColumnIndexOrThrow("tiempo")));
                juego.setFechaJuego(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("fecha_juego"))));
                juego.setSolverUsed(cursor.getInt(cursor.getColumnIndexOrThrow("isSolverUsed")) == 1);
                juego.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow("Usuarios_id")));

                juegos.add(juego);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return juegos;
    }


    // Eliminar un juego por su ID
    public void eliminarJuego(int cj) {
        database.delete(DatabaseHelper.TABLE_JUEGOS, "cj = ?", new String[]{String.valueOf(cj)});
    }
}
