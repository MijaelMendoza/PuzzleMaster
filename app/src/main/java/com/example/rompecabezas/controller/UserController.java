package com.example.rompecabezas.controller;

import android.content.Context;

import com.example.rompecabezas.model.UserModel;
import com.example.rompecabezas.model.database.UserDAO;

import java.util.List;

public class UserController {

    private UserDAO userDAO;

    // Constructor para inicializar el DAO
    public UserController(Context context) {
        userDAO = new UserDAO(context);
    }

    // Método para registrar un nuevo usuario
    public long registrarUsuario(String password, String nombre, String correo, String fotoPerfil, String preguntaSeguridad,
                                 String respuestaSeguridad, String fechaCreacion, int nivel, int experienciaAcumulada) {

        UserModel usuario = new UserModel(password, nombre, correo, fotoPerfil, preguntaSeguridad, respuestaSeguridad, fechaCreacion, nivel, experienciaAcumulada);
        return userDAO.insertarUsuario(usuario);
    }

    // Método para obtener todos los usuarios
    public List<UserModel> obtenerUsuarios() {
        return userDAO.obtenerUsuarios();
    }

    // Método para obtener un usuario por ID
    public UserModel obtenerUsuarioPorId(int id) {
        return userDAO.obtenerUsuarioPorId(id);
    }

    // Método para actualizar un usuario existente
    public int actualizarUsuario(String password, int id, String nombre, String correo, String fotoPerfil, String preguntaSeguridad,
                                 String respuestaSeguridad, String fechaCreacion, int nivel, int experienciaAcumulada) {

        UserModel usuario = new UserModel(password, nombre, correo, fotoPerfil, preguntaSeguridad, respuestaSeguridad, fechaCreacion, nivel, experienciaAcumulada);
        usuario.setId(id);  // Establecer el ID del usuario que se va a actualizar
        return userDAO.actualizarUsuario(usuario);
    }

    // Método para eliminar un usuario
    public void eliminarUsuario(int id) {
        userDAO.eliminarUsuario(id);
    }

    // Método para obtener un usuario por correo y contraseña
    public UserModel obtenerUsuarioPorCorreoYContrasena(String correo, String contrasena) {
        return userDAO.obtenerUsuarioPorCorreoYContrasena(correo, contrasena);
    }
}
