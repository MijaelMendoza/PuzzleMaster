package com.example.rompecabezas.controller;

import android.content.Context;

import com.example.rompecabezas.model.database.JuegoDAO;
import com.example.rompecabezas.model.JuegoModel;

import java.util.List;

public class JuegoController {
    private JuegoDAO juegoDAO;

    public JuegoController(Context context) {
        juegoDAO = new JuegoDAO(context);
    }

    // Crear un nuevo juego
    public long crearJuego(JuegoModel juego) {
        juegoDAO.open();
        long id = juegoDAO.insertarJuego(juego);
        juegoDAO.close();
        return id;
    }

    // Obtener todos los juegos
    public List<JuegoModel> obtenerJuegos() {
        juegoDAO.open();
        List<JuegoModel> juegos = juegoDAO.obtenerJuegos();
        juegoDAO.close();
        return juegos;
    }

    // Eliminar un juego
    public void eliminarJuego(int cj) {
        juegoDAO.open();
        juegoDAO.eliminarJuego(cj);
        juegoDAO.close();
    }
}
