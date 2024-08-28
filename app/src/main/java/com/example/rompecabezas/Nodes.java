package com.example.rompecabezas;

import java.util.List;

// Clase Node para manejar la cola de prioridad
class Nodes implements Comparable<Nodes> {
    List<String> state;
    int gScore;
    int fScore;
    Nodes parent;  // Referencia al nodo anterior en el camino

    Nodes(List<String> state, int gScore, int fScore, Nodes parent) {
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
