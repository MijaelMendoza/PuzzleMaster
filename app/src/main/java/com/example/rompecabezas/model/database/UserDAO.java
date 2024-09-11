package com.example.rompecabezas.model.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rompecabezas.dpHelper.DatabaseHelper;
import com.example.rompecabezas.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    // Método para crear un nuevo usuario
    public long insertarUsuario(UserModel usuario) {
        ContentValues values = new ContentValues();
        values.put("contrasena", usuario.getContrasena());
        values.put("nombre_usuario", usuario.getNombre_usuario());
        values.put("correo", usuario.getCorreo());
        values.put("foto_perfil", usuario.getFoto_perfil());
        values.put("pregunta_seguridad", usuario.getPregunta_seguridad());
        values.put("respuesta_seguridad", usuario.getRespuesta_seguridad());
        values.put("fecha_creacion", usuario.getFecha_creacion());
        values.put("nivel", usuario.getNivel());
        values.put("experiencia_acumulada", usuario.getExperiencia_acumulada());

        return database.insert(DatabaseHelper.TABLE_USUARIOS, null, values);
    }

    // Método para leer todos los usuarios
    @SuppressLint("Range")
    public List<UserModel> obtenerUsuarios() {
        List<UserModel> usuarios = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_USUARIOS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                UserModel usuario = new UserModel();
                usuario.setId(cursor.getInt(cursor.getColumnIndex("id")));
                usuario.setNombre_usuario(cursor.getString(cursor.getColumnIndex("nombre_usuario")));
                usuario.setCorreo(cursor.getString(cursor.getColumnIndex("correo")));
                usuario.setFoto_perfil(cursor.getString(cursor.getColumnIndex("foto_perfil")));
                usuario.setPregunta_seguridad(cursor.getString(cursor.getColumnIndex("pregunta_seguridad")));
                usuario.setRespuesta_seguridad(cursor.getString(cursor.getColumnIndex("respuesta_seguridad")));
                usuario.setFecha_creacion(cursor.getString(cursor.getColumnIndex("fecha_creacion")));
                usuario.setNivel(cursor.getInt(cursor.getColumnIndex("nivel")));
                usuario.setExperiencia_acumulada(cursor.getInt(cursor.getColumnIndex("experiencia_acumulada")));

                usuarios.add(usuario);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return usuarios;
    }

    // Método para obtener un usuario por su ID
    public UserModel obtenerUsuarioPorId(int id) {
        UserModel usuario = null;
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USUARIOS + " WHERE id = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            try {
                usuario = new UserModel();
                // Asegúrate de que los nombres de las columnas coincidan con los nombres exactos de tu tabla
                usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                usuario.setNombre_usuario(cursor.getString(cursor.getColumnIndexOrThrow("nombre_usuario")));
                usuario.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
                usuario.setFoto_perfil(cursor.getString(cursor.getColumnIndexOrThrow("foto_perfil")));
                usuario.setContrasena(cursor.getString(cursor.getColumnIndexOrThrow("contrasena")));
                usuario.setPregunta_seguridad(cursor.getString(cursor.getColumnIndexOrThrow("pregunta_seguridad")));
                usuario.setRespuesta_seguridad(cursor.getString(cursor.getColumnIndexOrThrow("respuesta_seguridad")));
                usuario.setFecha_creacion(cursor.getString(cursor.getColumnIndexOrThrow("fecha_creacion")));
                usuario.setNivel(cursor.getInt(cursor.getColumnIndexOrThrow("nivel")));
                usuario.setExperiencia_acumulada(cursor.getInt(cursor.getColumnIndexOrThrow("experiencia_acumulada")));
            } catch (IllegalArgumentException e) {
                // Manejar el caso donde las columnas no existen en la tabla
                e.printStackTrace();
            }
        }
        cursor.close();
        return usuario;
    }


    // Método para actualizar un usuario
    public int actualizarUsuario(UserModel usuario) {
        ContentValues values = new ContentValues();
        values.put("contrasena", usuario.getContrasena());
        values.put("nombre_usuario", usuario.getNombre_usuario());
        values.put("correo", usuario.getCorreo());
        values.put("foto_perfil", usuario.getFoto_perfil());
        values.put("pregunta_seguridad", usuario.getPregunta_seguridad());
        values.put("respuesta_seguridad", usuario.getRespuesta_seguridad());
        values.put("fecha_creacion", usuario.getFecha_creacion());
        values.put("nivel", usuario.getNivel());
        values.put("experiencia_acumulada", usuario.getExperiencia_acumulada());

        return database.update(DatabaseHelper.TABLE_USUARIOS, values, "id = ?", new String[]{String.valueOf(usuario.getId())});
    }


    // Método para eliminar un usuario
    public void eliminarUsuario(int id) {
        database.delete(DatabaseHelper.TABLE_USUARIOS, "id = ?", new String[]{String.valueOf(id)});
    }

    // Método para obtener un usuario por correo y contraseña
    public UserModel obtenerUsuarioPorCorreoYContrasena(String correo, String contrasena) {
        UserModel usuario = null;

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USUARIOS + " WHERE correo = ? AND contrasena = ?";
        Cursor cursor = database.rawQuery(query, new String[]{correo, contrasena});

        if (cursor.moveToFirst()) {
            usuario = new UserModel();
            usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            usuario.setNombre_usuario(cursor.getString(cursor.getColumnIndexOrThrow("nombre_usuario")));
            usuario.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
            usuario.setFoto_perfil(cursor.getString(cursor.getColumnIndexOrThrow("foto_perfil")));
        }
        cursor.close();
        return usuario;
    }
}
