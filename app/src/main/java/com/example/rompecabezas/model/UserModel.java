package com.example.rompecabezas.model;

public class UserModel {
    private int id;
    private String nombre_usuario;
    private String correo;
    private String foto_perfil;
    private String pregunta_seguridad;
    private String respuesta_seguridad;
    private String fecha_creacion;
    private String contrasena;
    private int nivel;
    private int experiencia_acumulada;

    // Constructor vacío y con parámetros
    public UserModel() {}

    public UserModel(String contrasena, String nombre_usuario, String correo, String foto_perfil, String pregunta_seguridad,
                        String respuesta_seguridad, String fecha_creacion, int nivel, int experiencia_acumulada) {
        this.contrasena = contrasena;
        this.nombre_usuario = nombre_usuario;
        this.correo = correo;
        this.foto_perfil = foto_perfil;
        this.pregunta_seguridad = pregunta_seguridad;
        this.respuesta_seguridad = respuesta_seguridad;
        this.fecha_creacion = fecha_creacion;
        this.nivel = nivel;
        this.experiencia_acumulada = experiencia_acumulada;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getExperiencia_acumulada() {
        return experiencia_acumulada;
    }

    public void setExperiencia_acumulada(int experiencia_acumulada) {
        this.experiencia_acumulada = experiencia_acumulada;
    }

    public String getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(String fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public String getFoto_perfil() {
        return foto_perfil;
    }

    public void setFoto_perfil(String foto_perfil) {
        this.foto_perfil = foto_perfil;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public String getPregunta_seguridad() {
        return pregunta_seguridad;
    }

    public void setPregunta_seguridad(String pregunta_seguridad) {
        this.pregunta_seguridad = pregunta_seguridad;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public String getRespuesta_seguridad() {
        return respuesta_seguridad;
    }

    public void setRespuesta_seguridad(String respuesta_seguridad) {
        this.respuesta_seguridad = respuesta_seguridad;
    }
}
