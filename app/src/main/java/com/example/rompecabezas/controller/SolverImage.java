package com.example.rompecabezas.controller;

import com.example.rompecabezas.model.Node;

import java.util.*;

public class SolverImage {

    private List<String> goalState;

    public void setGoalState(List<String> goalState) {
        this.goalState = goalState;
    }

    // Método principal para resolver el puzzle con A* estándar
    public List<Node> solvePuzzle(List<String> initialState) {
        if (goalState == null || goalState.isEmpty()) {
            throw new IllegalStateException("El estado objetivo no está inicializado.");
        }

        // Conjunto cerrado para los nodos ya evaluados
        Set<List<String>> closedSet = new HashSet<>();

        // Cola de prioridad para la frontera
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.fScore));

        // Nodo inicial
        Node startNode = new Node(initialState, 0, calculateHeuristic(initialState, goalState), null);

        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            // Extraer el nodo con el menor fScore
            Node currentNode = openSet.poll();

            // Si el estado actual es el estado objetivo, reconstruir el camino
            if (currentNode.state.equals(goalState)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.state);

            // Expandir los vecinos del nodo actual
            for (Node neighbor : getNeighbors(currentNode, goalState)) {
                if (closedSet.contains(neighbor.state)) {
                    continue; // Si ya fue evaluado, ignorarlo
                }

                int tentativeGScore = currentNode.gScore + 1; // Suponiendo que cada movimiento tiene un costo de 1

                // Si encontramos una ruta más corta hacia el vecino
                if (tentativeGScore < neighbor.gScore) {
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = neighbor.gScore + calculateHeuristic(neighbor.state, goalState);
                    neighbor.parent = currentNode;
                    openSet.add(neighbor);
                }
            }
        }

        return null; // No se encontró solución
    }

    // Método para calcular la heurística (Manhattan + Linear Conflicts)
    private int calculateHeuristic(List<String> state, List<String> goal) {
        int manhattanDistance = 0;
        int linearConflicts = 0;
        int size = (int) Math.sqrt(state.size());

        // Calcular la distancia Manhattan
        for (int i = 0; i < state.size(); i++) {
            String value = state.get(i);
            if (!value.equals("0")) { // La pieza vacía no se cuenta
                int targetIndex = goal.indexOf(value);
                int currentRow = i / size;
                int currentCol = i % size;
                int targetRow = targetIndex / size;
                int targetCol = targetIndex % size;
                manhattanDistance += Math.abs(currentRow - targetRow) + Math.abs(currentCol - targetCol);
            }
        }

        // Calcular conflictos lineales en filas
        for (int row = 0; row < size; row++) {
            linearConflicts += calculateRowConflicts(state, goal, row, size);
        }

        // Calcular conflictos lineales en columnas
        for (int col = 0; col < size; col++) {
            linearConflicts += calculateColumnConflicts(state, goal, col, size);
        }

        // La heurística es la distancia Manhattan más 2 veces los conflictos lineales
        return manhattanDistance + 2 * linearConflicts;
    }

    // Método para obtener los estados vecinos de un nodo
    private List<Node> getNeighbors(Node node, List<String> goal) {
        List<Node> neighbors = new ArrayList<>();
        int size = (int) Math.sqrt(node.state.size());
        int zeroIndex = node.state.indexOf("0");

        int[] rowMoves = {-1, 1, 0, 0};
        int[] colMoves = {0, 0, -1, 1};

        int zeroRow = zeroIndex / size;
        int zeroCol = zeroIndex % size;

        for (int i = 0; i < 4; i++) {
            int newRow = zeroRow + rowMoves[i];
            int newCol = zeroCol + colMoves[i];

            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                int newZeroIndex = newRow * size + newCol;
                List<String> newState = new ArrayList<>(node.state);
                Collections.swap(newState, zeroIndex, newZeroIndex);
                neighbors.add(new Node(newState, Integer.MAX_VALUE, Integer.MAX_VALUE, node));
            }
        }

        return neighbors;
    }

    // Método para reconstruir el camino desde el nodo final hasta el inicial
    private List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        Node current = node;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    // Función para calcular conflictos lineales en filas
    private int calculateRowConflicts(List<String> state, List<String> goal, int row, int size) {
        int conflicts = 0;

        for (int i = 0; i < size; i++) {
            String tile1 = state.get(row * size + i);
            if (tile1.equals("0")) continue;  // Ignorar la pieza vacía

            int goalIndex1 = goal.indexOf(tile1);
            int goalRow1 = goalIndex1 / size;

            // Si la ficha está en su fila objetivo
            if (goalRow1 == row) {
                for (int j = i + 1; j < size; j++) {
                    String tile2 = state.get(row * size + j);
                    if (tile2.equals("0")) continue;

                    int goalIndex2 = goal.indexOf(tile2);
                    int goalRow2 = goalIndex2 / size;

                    // Ambas fichas están en la misma fila, pero en el orden incorrecto (conflicto lineal)
                    if (goalRow2 == row && goalIndex1 > goalIndex2) {
                        conflicts++;
                    }
                }
            }
        }

        return conflicts;
    }

    // Función para calcular conflictos lineales en columnas
    private int calculateColumnConflicts(List<String> state, List<String> goal, int col, int size) {
        int conflicts = 0;

        for (int i = 0; i < size; i++) {
            String tile1 = state.get(i * size + col);
            if (tile1.equals("0")) continue;  // Ignorar la pieza vacía

            int goalIndex1 = goal.indexOf(tile1);
            int goalCol1 = goalIndex1 % size;

            // Si la ficha está en su columna objetivo
            if (goalCol1 == col) {
                for (int j = i + 1; j < size; j++) {
                    String tile2 = state.get(j * size + col);
                    if (tile2.equals("0")) continue;

                    int goalIndex2 = goal.indexOf(tile2);
                    int goalCol2 = goalIndex2 % size;

                    // Ambas fichas están en la misma columna, pero en el orden incorrecto (conflicto lineal)
                    if (goalCol2 == col && goalIndex1 > goalIndex2) {
                        conflicts++;
                    }
                }
            }
        }

        return conflicts;
    }
}
