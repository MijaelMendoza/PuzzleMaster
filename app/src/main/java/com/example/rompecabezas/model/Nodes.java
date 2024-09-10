package com.example.rompecabezas.model;

import java.util.List;

// Clase Node para manejar la cola de prioridad
public class Nodes implements Comparable<Nodes> {
    public List<String> state;
    public int gScore;
    public int fScore;
    public Nodes parent;  // Referencia al nodo anterior en el camino

    public Nodes(List<String> state, int gScore, int fScore, Nodes parent) {
        this.state = state;
        this.gScore = gScore;
        this.fScore = fScore;
        this.parent = parent;  // Inicializamos el parent
    }

    @Override
    public int compareTo(Nodes other) {
        return Integer.compare(this.fScore, other.fScore);
    }
}
