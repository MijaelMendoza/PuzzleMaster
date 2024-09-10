package com.example.rompecabezas.controller;

import com.example.rompecabezas.model.Node;

import java.util.*;

public class SolverImage {
    // Posiciones objetivo para un puzzle de 3x3
    private final List<String> goalState = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "0");

    // Método principal para resolver el puzzle
    public List<Node> solvePuzzle(List<String> initialState) {
        if (initialState.size() != 9) {
            throw new IllegalArgumentException("El estado inicial debe contener exactamente 9 elementos.");
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<List<String>> closedSet = new HashSet<>();

        // Nodo inicial
        Node startNode = new Node(initialState, 0, calculateHeuristic(initialState), null);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            // Si hemos llegado al estado objetivo
            if (currentNode.state.equals(goalState)) {
                return reconstructPath(currentNode);
            }

            // Añadir el estado actual al conjunto cerrado
            closedSet.add(new ArrayList<>(currentNode.state)); // Crear una nueva lista para evitar referencias erróneas

            // Explorar vecinos
            for (Node neighbor : getNeighbors(currentNode)) {
                if (closedSet.contains(neighbor.state)) {
                    continue; // Si ya está en cerrado, ignorarlo
                }

                int tentativeGScore = currentNode.gScore + 1;

                if (!openSet.contains(neighbor) || tentativeGScore < neighbor.gScore) {
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = neighbor.gScore + calculateHeuristic(neighbor.state);
                    neighbor.parent = currentNode;

                    // Añadir a la lista abierta si no está ya
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null; // Retornar null si no hay solución
    }

    // Método para calcular la heurística (distancia de Manhattan)
    private int calculateHeuristic(List<String> state) {
        int distance = 0;
        for (int i = 0; i < state.size(); i++) {
            String value = state.get(i);
            if (!value.equals("0")) { // La pieza vacía no se cuenta
                int targetIndex = goalState.indexOf(value);
                int currentRow = i / 3;
                int currentCol = i % 3;
                int targetRow = targetIndex / 3;
                int targetCol = targetIndex % 3;
                distance += Math.abs(currentRow - targetRow) + Math.abs(currentCol - targetCol);
            }
        }
        return distance;
    }

    // Método para obtener los estados vecinos de un nodo
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int zeroIndex = node.state.indexOf("0");

        int[] rowMoves = {-1, 1, 0, 0};
        int[] colMoves = {0, 0, -1, 1};

        int zeroRow = zeroIndex / 3;
        int zeroCol = zeroIndex % 3;

        for (int i = 0; i < 4; i++) {
            int newRow = zeroRow + rowMoves[i];
            int newCol = zeroCol + colMoves[i];

            if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                int newZeroIndex = newRow * 3 + newCol;
                List<String> newState = new ArrayList<>(node.state);
                Collections.swap(newState, zeroIndex, newZeroIndex);
                neighbors.add(new Node(newState, Integer.MAX_VALUE, Integer.MAX_VALUE, node));
            }
        }

        return neighbors;
    }

    // Método para reconstruir el camino desde el nodo objetivo al nodo inicial
    private List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
