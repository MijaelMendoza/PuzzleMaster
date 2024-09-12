package com.example.rompecabezas.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rompecabezas.R;
import com.example.rompecabezas.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class NormalPlay extends AppCompatActivity {
    private Node finalNode;
    private GridLayout tableroJugador, tableroMeta;
    private TextView cronometro;
    private TextView moveCounter;
    private Button btnNuevo, btnSolver, btnSalir;
    private Spinner spinnerDificultad;
    private boolean isPlaying = false;
    private int[][] puzzleMeta;
    private int[][] puzzleJugador;
    private int emptyTileRow, emptyTileCol;
    private int nivelDificultad = 3; // Nivel de dificultad por defecto (3x3)
    private int size;
    private Handler handler = new Handler();
    private long startTime;
    private int moveCount = 0; // Variable para contar los movimientos
    private boolean isTimerRunning = false;
    private boolean isFirstMove = true;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning) {
                long elapsedTime = SystemClock.elapsedRealtime() - startTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) (elapsedTime / 1000) / 60;
                // Actualiza el TextView con el tiempo transcurrido
                cronometro.setText(String.format("%02d:%02d", minutes, seconds));

                // Vuelve a ejecutar este runnable en 1 segundo
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_play);

        tableroJugador = findViewById(R.id.tableroJugador);
        tableroMeta = findViewById(R.id.tableroMeta);
        cronometro = findViewById(R.id.cronometro);
        btnNuevo = findViewById(R.id.btnNuevo);
        btnSolver = findViewById(R.id.btnSolverN);
        btnSalir = findViewById(R.id.btnSalir);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        moveCounter = findViewById(R.id.moveCounter);

        // Configurar el spinner para elegir la dificultad
        spinnerDificultad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: nivelDificultad = 2; break; // Fácil (2x2)
                    case 1: nivelDificultad = 3; break; // Media (3x3)
                    case 2: nivelDificultad = 4; break; // Difícil (4x4)
                }
                nuevoJuego();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnNuevo.setOnClickListener(v -> nuevoJuego());
        btnSolver.setOnClickListener(v -> solvePuzzle());
        btnSalir.setOnClickListener(v -> salir());

        nuevoJuego();
    }

    private void nuevoJuego() {
        resetCronometro(); // Reiniciar cronómetro
        moveCount = 0; // Reiniciar el contador de movimientos
        isPlaying = false;
        isFirstMove = true;

        size = nivelDificultad;

        // Generar puzzles
        generarPuzzle();
        rellenarTableros();
    }


    private void startCronometro() {
        startTime = SystemClock.elapsedRealtime();
        Log.d("timer", String.valueOf(startTime));
        isTimerRunning = true;
        handler.post(timerRunnable);  // Inicia el runnable que actualiza el cronómetro
    }

    private void resetCronometro() {
        handler.removeCallbacks(timerRunnable); // Detener el cronómetro
        isTimerRunning = false;
        cronometro.setText("00:00"); // Reiniciar el texto del cronómetro
    }

    private void generarPuzzle() {
        puzzleMeta = generarPuzzleSolucionable(size);
        do {
            puzzleJugador = mezclarPuzzle(puzzleMeta);
        } while (esIgual(puzzleMeta, puzzleJugador)); // Asegurar que los puzzles no sean iguales
    }

    private void rellenarTableros() {
        tableroJugador.removeAllViews();
        tableroMeta.removeAllViews();

        tableroJugador.setRowCount(size);
        tableroJugador.setColumnCount(size);
        tableroMeta.setRowCount(size);
        tableroMeta.setColumnCount(size);

        int tileSize = getTileSize(); // Calcular el tamaño de las piezas dinámicamente

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Crear TextView para las piezas del jugador
                TextView tvJugador = new TextView(this);
                GridLayout.LayoutParams paramsJugador = new GridLayout.LayoutParams();
                paramsJugador.width = tileSize;
                paramsJugador.height = tileSize;
                paramsJugador.setMargins(5, 5, 5, 5); // Márgenes entre piezas
                tvJugador.setLayoutParams(paramsJugador);

                // Crear TextView para las piezas de la meta
                TextView tvMeta = new TextView(this);
                GridLayout.LayoutParams paramsMeta = new GridLayout.LayoutParams();
                paramsMeta.width = tileSize;
                paramsMeta.height = tileSize;
                paramsMeta.setMargins(5, 5, 5, 5); // Márgenes entre piezas
                tvMeta.setLayoutParams(paramsMeta);

                int valueJugador = puzzleJugador[i][j];
                int valueMeta = puzzleMeta[i][j];

                // Configurar el contenido y estilo de las piezas del jugador
                if (valueJugador != 0) {
                    tvJugador.setText(String.valueOf((char) ('A' + valueJugador - 1)));
                    tvJugador.setBackgroundColor(getColorForTile(valueJugador));
                    tvJugador.setTextSize(24); // Tamaño del texto
                    tvJugador.setGravity(Gravity.CENTER); // Centrar el texto
                    tvJugador.setTextColor(getResources().getColor(R.color.white));
                } else {
                    // Si es la pieza vacía, solo ajustar color blanco y otros parámetros
                    tvJugador.setText("");
                    tvJugador.setBackgroundColor(getResources().getColor(R.color.color_X));
                    tvJugador.setTextSize(24); // Tamaño del texto para que ocupe el mismo espacio
                    tvJugador.setGravity(Gravity.CENTER); // Asegurar que sigue centrado
                    emptyTileRow = i;
                    emptyTileCol = j;
                }

                // Configurar el contenido y estilo de las piezas de la meta
                if (valueMeta != 0) {
                    tvMeta.setText(String.valueOf((char) ('A' + valueMeta - 1)));
                    tvMeta.setBackgroundColor(getColorForTile(valueMeta));
                    tvMeta.setTextSize(24); // Tamaño del texto
                    tvMeta.setGravity(Gravity.CENTER); // Centrar el texto
                    tvMeta.setTextColor(getResources().getColor(R.color.white));
                } else {
                    // Si es la pieza vacía en el tablero de meta
                    tvMeta.setText("");
                    tvMeta.setBackgroundColor(getResources().getColor(R.color.color_X));
                    tvMeta.setTextSize(24); // Tamaño del texto para que ocupe el mismo espacio
                    tvMeta.setGravity(Gravity.CENTER); // Asegurar que sigue centrado
                }

                // Añadir los TextView al GridLayout
                tableroJugador.addView(tvJugador);
                tableroMeta.addView(tvMeta);

                // Hacer que las piezas del jugador sean clicables
                final int finalI = i;
                final int finalJ = j;
                tvJugador.setOnClickListener(v -> moverPieza(finalI, finalJ));
            }
        }
    }

    private int getTileSize() {
        // Calcular el tamaño de las piezas en función del tamaño del GridLayout
        int gridWidth = getResources().getDisplayMetrics().widthPixels * 60 / 100;
        return gridWidth / size;
    }

    private void moverPieza(int row, int col) {
        if (isFirstMove) {
            startCronometro(); // Iniciar cronómetro cuando el jugador haga el primer movimiento
            isFirstMove = false;
        }
        if ((Math.abs(emptyTileRow - row) == 1 && emptyTileCol == col) ||
                (Math.abs(emptyTileCol - col) == 1 && emptyTileRow == row)) {
            // Intercambiar piezas
            puzzleJugador[emptyTileRow][emptyTileCol] = puzzleJugador[row][col];
            puzzleJugador[row][col] = 0;

            emptyTileRow = row;
            emptyTileCol = col;

            moveCount++; // Incrementar el contador de movimientos
            moveCounter.setText("Movimientos: " + moveCount); // Actualizar el contador en la pantalla

            rellenarTableros();
            verificarVictoria();
        }
    }

    private void verificarVictoria() {
        if (esIgual(puzzleJugador, puzzleMeta)) {
            stopCronometro(); // Detener el cronómetro
            mostrarDialogoVictoria();
        }
    }

    private List<String> getTextFromTiles() {
        List<String> tiles = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = puzzleJugador[i][j];
                if (value == 0) {
                    tiles.add("X");  // Representar la pieza vacía como "X"
                } else {
                    tiles.add(String.valueOf((char) ('A' + value - 1)));
                }
            }
        }
        return tiles;
    }

    private List<String> getTextFromGoal() {
        List<String> tiles = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = puzzleMeta[i][j];
                if (value == 0) {
                    tiles.add("X");  // Representar la pieza vacía como "X"
                } else {
                    tiles.add(String.valueOf((char) ('A' + value - 1)));
                }
            }
        }
        return tiles;
    }

    private void stopCronometro() {
        isTimerRunning = false;
        handler.removeCallbacks(timerRunnable); // Detener el Runnable
    }

    private boolean esIgual(int[][] puzzle1, int[][] puzzle2) {
        for (int i = 0; i < puzzle1.length; i++) {
            for (int j = 0; j < puzzle1[i].length; j++) {
                if (puzzle1[i][j] != puzzle2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void mostrarDialogoVictoria() {
        long elapsedTime = SystemClock.elapsedRealtime() - startTime;
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) (elapsedTime / 1000) / 60;

        String tiempo = String.format("%02d:%02d", minutes, seconds);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Has completado el rompecabezas en " + minutes + " minutos y " + seconds + " con " + moveCount + " movimientos.")
                .setPositiveButton("OK", (dialog, which) -> resetGame());
        builder.create().show();
    }

    private void solvePuzzle() {
        // Mover la lógica de resolución a un hilo separado
        new Thread(() -> {
            runOnUiThread(() -> {
                // Iniciar cronómetro
                startCronometro();
            });

            List<String> initial = getTextFromTiles();  // Obtener el estado actual del jugador como lista lineal
            List<String> goal = getTextFromGoal();      // Obtener el estado meta como lista lineal

            List<List<String>> solutionPath;

            // Seleccionar el algoritmo según el tamaño del puzzle
            if (size == 4) {
                solutionPath = bidirectionalAStar(initial, goal);  // Usar A* Bidireccional para 4x4
            } else {
                solutionPath = aStar(initial, goal);    // Usar A* para tamaños más pequeños
            }

            if (solutionPath != null && !solutionPath.isEmpty()) {
                // Mostrar los pasos de la solución
                for (int i = 1; i < solutionPath.size(); i++) {
                    List<String> step = solutionPath.get(i);
                    final int delay = i * 250;  // 250ms por cada movimiento
                    runOnUiThread(() -> {
                        setTilesFromList(step);  // Actualizar el tablero con los movimientos
                        moveCount++;
                        moveCounter.setText("Movimientos: " + moveCount);  // Actualizar el contador de movimientos
                    });

                    try {
                        Thread.sleep(250);  // Simular el retraso entre los movimientos
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                long elapsedTime = SystemClock.elapsedRealtime() - startTime;
                runOnUiThread(() -> showSolverResult(elapsedTime, moveCount));
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No se encontró una solución. Verifique si el puzzle es solucionable.", Toast.LENGTH_SHORT).show());
            }

        }).start();
    }

    // Implementación de A* Bidireccional para 4x4
    private List<List<String>> bidirectionalAStar(List<String> initial, List<String> goal) {
        PriorityQueue<Node> forwardOpenSet = new PriorityQueue<>();
        PriorityQueue<Node> backwardOpenSet = new PriorityQueue<>();
        Set<List<String>> forwardClosedSet = new HashSet<>();
        Set<List<String>> backwardClosedSet = new HashSet<>();

        forwardOpenSet.add(new Node(initial, 0, heuristic(initial, goal), null));
        backwardOpenSet.add(new Node(goal, 0, heuristic(goal, initial), null));  // Búsqueda desde el objetivo

        Node meetingNode = null;

        while (!forwardOpenSet.isEmpty() && !backwardOpenSet.isEmpty()) {
            // Expandir el nodo con menor costo de la búsqueda hacia adelante
            Node forwardNode = forwardOpenSet.poll();
            List<String> forwardState = forwardNode.state;

            if (backwardClosedSet.contains(forwardState)) {
                meetingNode = forwardNode;
                break;
            }

            forwardClosedSet.add(forwardState);
            expandNeighbors(forwardOpenSet, forwardClosedSet, forwardNode, goal);

            // Expandir el nodo con menor costo de la búsqueda hacia atrás
            Node backwardNode = backwardOpenSet.poll();
            List<String> backwardState = backwardNode.state;

            if (forwardClosedSet.contains(backwardState)) {
                meetingNode = backwardNode;
                break;
            }

            backwardClosedSet.add(backwardState);
            expandNeighbors(backwardOpenSet, backwardClosedSet, backwardNode, initial);
        }

        if (meetingNode != null) {
            // Reconstruir el camino combinando las dos búsquedas
            return reconstructBidirectionalPath(meetingNode);
        }

        return null;  // No se encontró solución
    }

    // Expande los nodos vecinos
// Expande los nodos vecinos, priorizando aquellos que minimizan la heurística
    private void expandNeighbors(PriorityQueue<Node> openSet, Set<List<String>> closedSet, Node currentNode, List<String> goal) {
        int emptyIndex = currentNode.state.indexOf("X");
        int[] directions = {-size, size, -1, 1};  // Movimientos posibles en las cuatro direcciones
        List<Node> neighbors = new ArrayList<>();

        for (int dir : directions) {
            int newIndex = emptyIndex + dir;

            if (newIndex >= 0 && newIndex < size * size && isValidMove(emptyIndex, newIndex, size)) {
                List<String> neighbor = new ArrayList<>(currentNode.state);
                Collections.swap(neighbor, emptyIndex, newIndex);

                if (!closedSet.contains(neighbor)) {
                    int tentativeGScore = currentNode.gScore + 1;
                    int hScore = heuristic(neighbor, goal);  // Calcula la heurística para este vecino
                    Node neighborNode = new Node(neighbor, tentativeGScore, tentativeGScore + hScore, currentNode);
                    neighbors.add(neighborNode);
                }
            }
        }

        // Ordenar vecinos por la heurística para priorizar los mejores movimientos
        neighbors.sort((a, b) -> Integer.compare(a.fScore, b.fScore));

        // Añadir los mejores vecinos al openSet
        openSet.addAll(neighbors);
    }


    // Reconstruye el camino combinado entre las búsquedas
    private List<List<String>> reconstructBidirectionalPath(Node meetingNode) {
        List<List<String>> forwardPath = reconstructPath(meetingNode);
        List<List<String>> backwardPath = reconstructBackwardPath(meetingNode);

        // Combinar los caminos
        forwardPath.addAll(backwardPath);
        return forwardPath;
    }

    // Reconstruye la ruta hacia atrás
    private List<List<String>> reconstructBackwardPath(Node node) {
        List<List<String>> path = new ArrayList<>();
        node = node.parent;  // Evitar duplicar el estado del encuentro
        while (node != null) {
            path.add(node.state);
            node = node.parent;
        }
        return path;
    }

    // Heurística combinada de distancia Manhattan + Linear Conflict para puzzles deslizantes
    private int heuristic(List<String> state, List<String> goal) {
        int manhattanDistance = 0;
        int linearConflicts = 0;
        int size = (int) Math.sqrt(state.size());

        // Calcular la distancia Manhattan
        for (int i = 0; i < state.size(); i++) {
            String tile = state.get(i);
            if (!tile.equals("X")) {
                int goalIndex = goal.indexOf(tile);
                int currentRow = i / size;
                int currentCol = i % size;
                int goalRow = goalIndex / size;
                int goalCol = goalIndex % size;
                manhattanDistance += Math.abs(currentRow - goalRow) + Math.abs(currentCol - goalCol);
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

        return manhattanDistance + 2 * linearConflicts;  // Cada conflicto lineal añade un costo de 2 movimientos
    }

    // Función para calcular conflictos lineales en filas
    private int calculateRowConflicts(List<String> state, List<String> goal, int row, int size) {
        int conflicts = 0;

        for (int i = 0; i < size; i++) {
            String tile1 = state.get(row * size + i);
            if (tile1.equals("X")) continue;  // Ignorar la pieza vacía

            int goalIndex1 = goal.indexOf(tile1);
            int goalRow1 = goalIndex1 / size;

            // Si la ficha está en su fila objetivo
            if (goalRow1 == row) {
                for (int j = i + 1; j < size; j++) {
                    String tile2 = state.get(row * size + j);
                    if (tile2.equals("X")) continue;

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
            if (tile1.equals("X")) continue;  // Ignorar la pieza vacía

            int goalIndex1 = goal.indexOf(tile1);
            int goalCol1 = goalIndex1 % size;

            // Si la ficha está en su columna objetivo
            if (goalCol1 == col) {
                for (int j = i + 1; j < size; j++) {
                    String tile2 = state.get(j * size + col);
                    if (tile2.equals("X")) continue;

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

    private void showSolverResult(long elapsedTime, int movesCount) {
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) (elapsedTime / 1000) / 60;

        new AlertDialog.Builder(this)
                .setTitle("Solver Completado")
                .setMessage("El solver ha resuelto el rompecabezas en " + minutes + " minutos y " + seconds + " segundos, con " + movesCount + " movimientos.")
                .setPositiveButton("OK", null)
                .show();

        resetGame();

    }

    private void resetGame() {
        // Reiniciar el cronómetro
        resetCronometro();

        // Reiniciar el contador de movimientos
        moveCount = 0;
        moveCounter.setText("Movimientos: " + moveCount);

        // Generar nuevo puzzle y tablero de meta
        puzzleMeta = generarPuzzleSolucionable(size);
        do {
            puzzleJugador = mezclarPuzzle(puzzleMeta);
        } while (esIgual(puzzleMeta, puzzleJugador)); // Asegurar que los puzzles no sean iguales

        // Rellenar los tableros con las nuevas piezas
        rellenarTableros();

        // Reiniciar la bandera para la primera jugada
        isFirstMove = true;
    }


    private void setTilesFromList(List<String> tiles) {
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                String tile = tiles.get(index++);
                if (tile.equals("X")) {
                    puzzleJugador[i][j] = 0;  // La pieza vacía
                } else {
                    puzzleJugador[i][j] = tile.charAt(0) - 'A' + 1;
                }
            }
        }
        rellenarTableros();  // Actualizar la visualización del tablero
    }

    // Método para reconstruir la ruta desde el nodo final
    private List<List<String>> reconstructPath(Node node) {
        List<List<String>> path = new ArrayList<>();
        while (node != null) {
            path.add(0, node.state);
            node = node.parent;
        }
        return path;
    }

    private List<List<String>> aStar(List<String> initial, List<String> goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<List<String>> closedSet = new HashSet<>();
        openSet.add(new Node(initial, 0, heuristic(initial, goal), null));

        int gridSize = size * size;  // Tamaño total del tablero (por ejemplo, 3x3 o 4x4)

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            List<String> current = currentNode.state;

            if (current.equals(goal)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(current);
            int emptyIndex = current.indexOf("X");
            int[] directions = {-size, size, -1, 1};  // Ajustar las direcciones según el tamaño del tablero

            for (int dir : directions) {
                int newIndex = emptyIndex + dir;

                if (newIndex >= 0 && newIndex < gridSize && isValidMove(emptyIndex, newIndex, size)) {
                    List<String> neighbor = new ArrayList<>(current);
                    Collections.swap(neighbor, emptyIndex, newIndex);

                    if (!closedSet.contains(neighbor)) {
                        int tentativeGScore = currentNode.gScore + 1;
                        Node neighborNode = new Node(neighbor, tentativeGScore, tentativeGScore + heuristic(neighbor, goal), currentNode);
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return null;
    }



    private boolean isValidMove(int emptyIndex, int newIndex, int size) {
        int emptyRow = emptyIndex / size;
        int emptyCol = emptyIndex % size;
        int newRow = newIndex / size;
        int newCol = newIndex % size;

        return (Math.abs(emptyRow - newRow) + Math.abs(emptyCol - newCol)) == 1;
    }

    private void salir() {
        finish();
    }

    private int getColorForTile(int value) {
        switch (value) {
            case 1: return getResources().getColor(R.color.color_A);
            case 2: return getResources().getColor(R.color.color_B);
            case 3: return getResources().getColor(R.color.color_C);
            case 4: return getResources().getColor(R.color.color_D);
            case 5: return getResources().getColor(R.color.color_E);
            case 6: return getResources().getColor(R.color.color_F);
            case 7: return getResources().getColor(R.color.color_G);
            case 8: return getResources().getColor(R.color.color_H);
            case 9: return getResources().getColor(R.color.color_I);
            case 10: return getResources().getColor(R.color.color_J);
            case 11: return getResources().getColor(R.color.color_K);
            case 12: return getResources().getColor(R.color.color_L);
            case 13: return getResources().getColor(R.color.color_M);
            case 14: return getResources().getColor(R.color.color_N);
            case 15: return getResources().getColor(R.color.color_O);
            case 16: return getResources().getColor(R.color.color_P);
            case 17: return getResources().getColor(R.color.color_Q);
            case 18: return getResources().getColor(R.color.color_R);
            case 19: return getResources().getColor(R.color.color_S);
            case 20: return getResources().getColor(R.color.color_T);
            case 21: return getResources().getColor(R.color.color_U);
            case 22: return getResources().getColor(R.color.color_V);
            case 23: return getResources().getColor(R.color.color_W);
            case 24: return getResources().getColor(R.color.color_X);
            case 25: return getResources().getColor(R.color.color_Y);
            case 26: return getResources().getColor(R.color.color_Z);
            default: return getResources().getColor(R.color.color_X); // Blanco por defecto
        }
    }

    private boolean esSolucionable(List<Integer> puzzleList, int size) {
        int countInversions = 0;

        // Contar el número de inversiones (pares de fichas en el orden incorrecto)
        for (int i = 0; i < puzzleList.size(); i++) {
            for (int j = i + 1; j < puzzleList.size(); j++) {
                if (puzzleList.get(i) != 0 && puzzleList.get(j) != 0 && puzzleList.get(i) > puzzleList.get(j)) {
                    countInversions++;
                }
            }
        }

        if (size % 2 == 1) {
            // Para puzzles de tamaño impar (como 3x3), el número de inversiones debe ser par para ser solucionable
            return countInversions % 2 == 0;
        } else {
            // Para puzzles de tamaño par (como 4x4)
            int emptyRowFromBottom = size - (puzzleList.indexOf(0) / size);  // Contar la fila desde abajo
            // Verificar si la fila vacía es impar y el número de inversiones es par, o viceversa
            if (emptyRowFromBottom % 2 == 1) {
                return countInversions % 2 == 0;
            } else {
                return countInversions % 2 == 1;
            }
        }
    }

    private int[][] generarPuzzleSolucionable(int size) {
        List<Integer> puzzleList = new ArrayList<>();
        for (int i = 1; i < size * size; i++) {
            puzzleList.add(i);
        }
        puzzleList.add(0); // Espacio vacío

        do {
            Collections.shuffle(puzzleList);
        } while (!esSolucionable(puzzleList, size));

        int[][] puzzle = new int[size][size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                puzzle[i][j] = puzzleList.get(index++);
            }
        }
        return puzzle;
    }
    private int[][] mezclarPuzzle(int[][] puzzle) {
        List<Integer> puzzleList = new ArrayList<>();
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                puzzleList.add(puzzle[i][j]);
            }
        }

        do {
            Collections.shuffle(puzzleList);
        } while (!esSolucionable(puzzleList, size));

        int[][] nuevoPuzzle = new int[size][size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                nuevoPuzzle[i][j] = puzzleList.get(index++);
            }
        }
        return nuevoPuzzle;
    }

}