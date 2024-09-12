package com.example.rompecabezas.model;

import java.util.Date;

public class JuegoModel {
    private int cj; // Identificador del juego
    private String dificultad;
    private String tipoJuego;
    private int cantidadMovimientos;
    private boolean resultado;
    private int experienciaGanada;
    private int experienciaPerdida;
    private int tiempo;
    private Date fechaJuego;
    private boolean isSolverUsed;
    private int usuarioId; // ID del usuario relacionado

    // Constructor vacío
    public JuegoModel() {}

    // Constructor completo
    public JuegoModel(int cj, String dificultad, String tipoJuego, int cantidadMovimientos, boolean resultado,
                 int experienciaGanada, int experienciaPerdida, int tiempo, Date fechaJuego, boolean isSolverUsed, int usuarioId) {
        this.cj = cj;
        this.dificultad = dificultad;
        this.tipoJuego = tipoJuego;
        this.cantidadMovimientos = cantidadMovimientos;
        this.resultado = resultado;
        this.experienciaGanada = experienciaGanada;
        this.experienciaPerdida = experienciaPerdida;
        this.tiempo = tiempo;
        this.fechaJuego = fechaJuego;
        this.isSolverUsed = isSolverUsed;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public int getCj() {
        return cj;
    }

    public void setCj(int cj) {
        this.cj = cj;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public String getTipoJuego() {
        return tipoJuego;
    }

    public void setTipoJuego(String tipoJuego) {
        this.tipoJuego = tipoJuego;
    }

    public int getCantidadMovimientos() {
        return cantidadMovimientos;
    }

    public void setCantidadMovimientos(int cantidadMovimientos) {
        this.cantidadMovimientos = cantidadMovimientos;
    }

    public boolean isResultado() {
        return resultado;
    }

    public void setResultado(boolean resultado) {
        this.resultado = resultado;
    }

    public int getExperienciaGanada() {
        return experienciaGanada;
    }

    public void setExperienciaGanada(int experienciaGanada) {
        this.experienciaGanada = experienciaGanada;
    }

    public int getExperienciaPerdida() {
        return experienciaPerdida;
    }

    public void setExperienciaPerdida(int experienciaPerdida) {
        this.experienciaPerdida = experienciaPerdida;
    }

    public int getTiempo() {
        return tiempo;
    }

    public void setTiempo(int tiempo) {
        this.tiempo = tiempo;
    }

    public Date getFechaJuego() {
        return fechaJuego;
    }

    public void setFechaJuego(Date fechaJuego) {
        this.fechaJuego = fechaJuego;
    }

    public boolean isSolverUsed() {
        return isSolverUsed;
    }

    public void setSolverUsed(boolean solverUsed) {
        isSolverUsed = solverUsed;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
}
