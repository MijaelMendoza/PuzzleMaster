package com.example.rompecabezas.model;

import java.util.List;

// Clase Node para manejar la cola de prioridad
public class Node implements Comparable<Node> {
    public List<String> state;
    public int gScore;
    public int fScore;
    public Node parent;  // Referencia al nodo anterior en el camino

    public Node(List<String> state, int gScore, int fScore, Node parent) {
        this.state = state;
        this.gScore = gScore;
        this.fScore = fScore;
        this.parent = parent;  // Inicializamos el parent
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.fScore, other.fScore);
    }
}
