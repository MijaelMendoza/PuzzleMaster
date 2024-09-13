package com.example.rompecabezas.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rompecabezas.R;
import com.example.rompecabezas.controller.JuegoController;
import com.example.rompecabezas.model.JuegoModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class Versus extends AppCompatActivity {

    TextView[] puzzleTiles;
    TextView[] sampleTiles;
    TextView[] solverTiles;
    Button btNuevo, btSalir, btStart;
    Spinner spinnerDificultad;
    int playerPivot;
    int solverPivot;
    Handler handler = new Handler();
    int[] delayTimes = {1500, 800, 300};  // Dificultades: fácil, media, difícil
    int selectedDelay = 1000;  // Por defecto fácil
    private boolean isTimerRunning = false;
    private boolean isFirstMove = true;  // Para controlar el inicio del juego
    private int playerMoveCount = 0;  // Contador de movimientos del jugador
    private int solverMoveCount = 0;
    private long startTime;  // Para controlar el inicio del cronómetro
    private TextView moveCounter;  // Texto para mostrar los movimientos
    private TextView timerText;  // Texto para mostrar el cronómetro

    private int userId;
    private JuegoController juegoController;

    // Mapa de colores para las letras
    HashMap<String, Integer> colorMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus);

        // Inicializar las vistas
        spinnerDificultad = findViewById(R.id.spinner_dificultad);
        btSalir = findViewById(R.id.btsalir);
        btNuevo = findViewById(R.id.btnuevo);
        moveCounter = findViewById(R.id.moveCounter);  // TextView para el contador de movimientos
        timerText = findViewById(R.id.timerText);  // TextView para el cronómetro


        puzzleTiles = new TextView[]{
                findViewById(R.id.tvA), findViewById(R.id.tvB), findViewById(R.id.tvC),
                findViewById(R.id.tvD), findViewById(R.id.tvX), findViewById(R.id.tvE),
                findViewById(R.id.tvF), findViewById(R.id.tvG), findViewById(R.id.tvH)
        };

        sampleTiles = new TextView[]{
                findViewById(R.id.sampleA), findViewById(R.id.sampleB), findViewById(R.id.sampleC),
                findViewById(R.id.sampleD), findViewById(R.id.sampleE), findViewById(R.id.sampleF),
                findViewById(R.id.sampleG), findViewById(R.id.sampleH), findViewById(R.id.sampleX)
        };

        solverTiles = new TextView[]{
                findViewById(R.id.solverA), findViewById(R.id.solverB), findViewById(R.id.solverC),
                findViewById(R.id.solverD), findViewById(R.id.solverE), findViewById(R.id.solverF),
                findViewById(R.id.solverG), findViewById(R.id.solverH), findViewById(R.id.solverX)
        };

        // Inicializar el mapa de colores basado en el XML
        initializeColorMap();

        // Configuración del Spinner de Dificultad
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dificultades, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDificultad.setAdapter(adapter);

        spinnerDificultad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDelay = delayTimes[position];  // Actualiza el delay según la dificultad
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDelay = delayTimes[0];  // Por defecto a fácil
            }
        });

        btSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Configuración inicial de los estados de las fichas
        resetGame();

        btNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        for (int i = 0; i < puzzleTiles.length; i++) {
            final int index = i;
            puzzleTiles[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    movePlayerTile(index);
                }
            });
        }
        juegoController = new JuegoController(this);
        obtenerIdUsuario();
    }

    // Método para obtener el ID del usuario
    private void obtenerIdUsuario() {
        SharedPreferences sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);  // Obtener el ID del usuario
        Toast.makeText(this, "ID del usuario: " + userId, Toast.LENGTH_SHORT).show();
        if (userId == -1) {
            Toast.makeText(this, "Error al obtener el ID del usuario", Toast.LENGTH_SHORT).show();
            finish();  // Terminar la actividad si no se encuentra el ID del usuario
        }
    }

    // Método para registrar un nuevo juego
    private void registrarJuego(boolean isUserWin, long elapsedTime, int movimientos, boolean isSolverUsed) {
        // Datos del juego
        String dificultad = spinnerDificultad.getSelectedItem().toString().toLowerCase();
        String tipoJuego = "versus";
        String resultado = isUserWin ? "gano" : "perdio";
        int experienciaGanada = isUserWin ? 500 : 0;
        int experienciaPerdida = isUserWin ? 0 : 500;
        Date fechaJuego = new Date();  // Fecha actual

        // Crear un nuevo modelo de juego
        JuegoModel juego = new JuegoModel();
        juego.setDificultad(dificultad);
        juego.setTipoJuego(tipoJuego);
        juego.setCantidadMovimientos(movimientos);
        juego.setResultado(isUserWin);
        juego.setExperienciaGanada(experienciaGanada);
        juego.setExperienciaPerdida(experienciaPerdida);
        juego.setTiempo((int) (elapsedTime / 1000));  // Convertir milisegundos a segundos
        juego.setFechaJuego(fechaJuego);
        juego.setSolverUsed(isSolverUsed);
        juego.setUsuarioId(userId);
        try{
            // Insertar el juego en la base de datos
            juegoController.crearJuego(juego);
            Toast.makeText(this, "Registro del juego exitoso", Toast.LENGTH_SHORT).show();
            Log.d("Registro juego", String.valueOf(juego));
        }catch (IllegalArgumentException e){
            Toast.makeText(this, "Error al registrar juego", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateMoveCounter() {
        moveCounter.setText("Movimientos: " + playerMoveCount);
    }

    private void startTimer() {
        startTime = SystemClock.elapsedRealtime();
        isTimerRunning = true;
        handler.post(timerRunnable);
    }

    private void stopTimer() {
        isTimerRunning = false;
        handler.removeCallbacks(timerRunnable);  // Detener el Runnable del cronómetro
    }

    private void resetTimer() {
        handler.removeCallbacks(timerRunnable);  // Detener cualquier actualización previa
        isTimerRunning = false;
        timerText.setText("00:00");
    }

    // Runnable para actualizar el cronómetro cada segundo
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning) {
                long elapsedTime = SystemClock.elapsedRealtime() - startTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) (elapsedTime / 1000) / 60;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
                handler.postDelayed(this, 1000);  // Actualizar cada segundo
            }
        }
    };

    private void initializeColorMap() {
        // Definir colores específicos para cada letra
        colorMap.put("A", Color.parseColor("#2558D9")); // Rojo
        colorMap.put("B", Color.parseColor("#4CAF50")); // Rosa
        colorMap.put("C", Color.parseColor("#FFEB3B")); // Púrpura
        colorMap.put("D", Color.parseColor("#F44336")); // Azul púrpura
        colorMap.put("E", Color.parseColor("#FF9800")); // Azul índigo
        colorMap.put("F", Color.parseColor("#9C27B0")); // Azul claro
        colorMap.put("G", Color.parseColor("#00BCD4")); // Cian
        colorMap.put("H", Color.parseColor("#9E9E9E")); // Verde azulado
        colorMap.put("X", Color.parseColor("#FFFFFF")); // Blanco para la pieza vacía
    }

    private void resetGame() {
        // Generar configuraciones iniciales aleatorias
        List<String> randomTilesForSample = generateSolvableTileList();
        List<String> randomTilesForPuzzleSolver = generateSolvableTileList();

        // Configurar las fichas para muestra, solver y jugador
        setTiles(sampleTiles, randomTilesForSample);
        setTiles(puzzleTiles, randomTilesForPuzzleSolver);
        setTiles(solverTiles, randomTilesForPuzzleSolver);

        // Reiniciar el contador de movimientos y cronómetro
        playerMoveCount = 0;
        solverMoveCount = 0;
        updateMoveCounter();
        resetTimer();

        // Encontrar y configurar la posición del pivote (pieza vacía) para el jugador y el solver
        playerPivot = findPivot(puzzleTiles);
        solverPivot = findPivot(solverTiles);

        isFirstMove = true;  // Esperar el primer movimiento
    }

    private void startSolver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                solvePuzzle();
            }
        }).start();
    }

    private List<String> generateSolvableTileList() {
        List<String> tiles = new ArrayList<>();
        tiles.add("A");
        tiles.add("B");
        tiles.add("C");
        tiles.add("D");
        tiles.add("E");
        tiles.add("F");
        tiles.add("G");
        tiles.add("H");
        tiles.add("X");

        do {
            Collections.shuffle(tiles);
        } while (!isSolvable(tiles));

        return tiles;
    }

    private boolean isSolvable(List<String> tiles) {
        int inversions = 0;
        for (int i = 0; i < tiles.size() - 1; i++) {
            for (int j = i + 1; j < tiles.size(); j++) {
                if (!tiles.get(i).equals("X") && !tiles.get(j).equals("X") && tiles.get(i).compareTo(tiles.get(j)) > 0) {
                    inversions++;
                }
            }
        }

        // Para un puzzle 3x3, solo necesitamos que el número de inversiones sea par
        return inversions % 2 == 0;
    }

    private void setTiles(TextView[] tileViews, List<String> tiles) {
        for (int i = 0; i < tileViews.length; i++) {
            final String tile = tiles.get(i);
            tileViews[i].setText(tile);  // Actualiza el texto del TextView

            // Asignar color según la letra usando el mapa de colores
            tileViews[i].setBackgroundColor(colorMap.get(tile));
        }
    }

    private int findPivot(TextView[] tileViews) {
        for (int i = 0; i < tileViews.length; i++) {
            if (tileViews[i].getText().toString().equals("X")) {
                return i;  // Devuelve la posición de la pieza vacía "X"
            }
        }
        return -1;  // Retorna -1 si no se encuentra la pieza vacía
    }

    private void movePlayerTile(int index) {
        if (isFirstMove) {
            startTimer();
            solvePuzzle();
            isFirstMove = false;
        }

        // Verificar si la ficha seleccionada es adyacente a la ficha vacía
        if (isValidMove(playerPivot, index)) {
            // Intercambiar las fichas
            swapTiles(puzzleTiles, index, playerPivot);
            // Actualizar la posición del pivote del jugador
            playerPivot = index;

            // Incrementar el contador de movimientos del jugador
            playerMoveCount++;
            updateMoveCounter();

            // Comprobar si el jugador ha ganado
            if (isPlayerWin()) {
                stopTimer();
                showWinMessage();
            }
        }
    }

    private boolean isAdjacent(int index1, int index2) {
        int row1 = index1 / 3;
        int col1 = index1 % 3;
        int row2 = index2 / 3;
        int col2 = index2 % 3;

        return (Math.abs(row1 - row2) == 1 && col1 == col2) ||
                (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    private void swapTiles(TextView[] tiles, int index1, int index2) {
        String tempText = tiles[index1].getText().toString();
        int color1 = colorMap.get(tiles[index1].getText().toString());
        int color2 = colorMap.get(tiles[index2].getText().toString());

        // Intercambiar textos
        tiles[index1].setText(tiles[index2].getText().toString());
        tiles[index2].setText(tempText);

        // Intercambiar colores basados en los textos
        tiles[index1].setBackgroundColor(colorMap.get(tiles[index1].getText().toString()));
        tiles[index2].setBackgroundColor(colorMap.get(tiles[index2].getText().toString()));
    }

    private boolean isPlayerWin() {
        for (int i = 0; i < puzzleTiles.length; i++) {
            if (!puzzleTiles[i].getText().toString().equals(sampleTiles[i].getText().toString())) {
                return false;
            }
        }
        return true;
    }

    // Mostrar mensaje de victoria
    private void showWinMessage() {
        long elapsedTime = SystemClock.elapsedRealtime() - startTime;
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) (elapsedTime / 1000) / 60;

        registrarJuego(true, elapsedTime, playerMoveCount, false);

        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("¡Ganaste!")
                .setMessage("Has completado el rompecabezas en " + minutes + " minutos y " + seconds + " segundos, con " + playerMoveCount + " movimientos.")
                .setPositiveButton("OK", null)
                .show());

        resetGame(); // Reiniciar el juego
    }

    // Mostrar mensaje de derrota
    private void showLoseMessage() {
        long elapsedTime = SystemClock.elapsedRealtime() - startTime;
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) (elapsedTime / 1000) / 60;

        registrarJuego(false, elapsedTime, solverMoveCount, true);

        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("¡Perdiste!")
                .setMessage("La IA ha completado el rompecabezas antes que tú en " + minutes + " minutos y " + seconds + " segundos, con " + solverMoveCount + " movimientos. Perdiste 500 puntos.")
                .setPositiveButton("OK", null)
                .show());

        resetGame(); // Reiniciar el juego
    }


    private void solvePuzzle() {
        btNuevo.setEnabled(false);
        spinnerDificultad.setEnabled(false);
        List<String> initial = new ArrayList<>();
        for (TextView tile : solverTiles) {
            initial.add(tile.getText().toString());
        }

        List<String> goal = new ArrayList<>();
        for (TextView tile : sampleTiles) {
            goal.add(tile.getText().toString());
        }

        List<List<String>> solutionPath = aStar(initial, goal);

        if (solutionPath != null && !solutionPath.isEmpty()) {
            for (int i = 1; i < solutionPath.size(); i++) {
                List<String> step = solutionPath.get(i);
                final int delay = i * selectedDelay;
                handler.postDelayed(() -> {
                    setTiles(solverTiles, step);
                    solverPivot = findPivot(solverTiles);
                    solverMoveCount++;
                    if (isSolverWin()) {
                        showLoseMessage();// Muestra mensaje de derrota si el solver gana
                        btNuevo.setEnabled(true);
                        spinnerDificultad.setEnabled(true);
                    }
                }, delay);
            }
        }
    }

    private boolean isSolverWin() {
        for (int i = 0; i < solverTiles.length; i++) {
            if (!solverTiles[i].getText().toString().equals(sampleTiles[i].getText().toString())) {
                return false;
            }
        }
        return true;
    }

    // Clase Node para manejar la cola de prioridad
    class Node implements Comparable<Node> {
        List<String> state;
        int gScore;
        int fScore;
        Node parent;  // Referencia al nodo anterior en el camino

        Node(List<String> state, int gScore, int fScore, Node parent) {
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

    // Algoritmo A* modificado para encontrar la solución óptima
    private List<List<String>> aStar(List<String> initial, List<String> goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<List<String>> closedSet = new HashSet<>();
        openSet.add(new Node(initial, 0, heuristic(initial, goal), null));

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            List<String> current = currentNode.state;

            if (current.equals(goal)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(current);
            int emptyIndex = current.indexOf("X");
            int[] directions = {-3, 3, -1, 1};

            for (int dir : directions) {
                int newIndex = emptyIndex + dir;

                if (newIndex >= 0 && newIndex < 9 && isValidMove(emptyIndex, newIndex)) {
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

    // Método para reconstruir la ruta desde el nodo final
    private List<List<String>> reconstructPath(Node node) {
        List<List<String>> path = new ArrayList<>();
        while (node != null) {
            path.add(0, node.state);
            node = node.parent;
        }
        return path;
    }

    // Heurística de Manhattan
    private int heuristic(List<String> state, List<String> goal) {
        int distance = 0;
        for (int i = 0; i < state.size(); i++) {
            String tile = state.get(i);
            if (!tile.equals("X")) {
                int goalIndex = goal.indexOf(tile);
                int currentRow = i / 3;
                int currentCol = i % 3;
                int goalRow = goalIndex / 3;
                int goalCol = goalIndex % 3;
                distance += Math.abs(currentRow - goalRow) + Math.abs(currentCol - goalCol);
            }
        }
        return distance;
    }

    // Validar movimientos
    private boolean isValidMove(int emptyIndex, int newIndex) {
        int emptyRow = emptyIndex / 3;
        int emptyCol = emptyIndex % 3;
        int newRow = newIndex / 3;
        int newCol = newIndex % 3;

        // Movimiento válido si es adyacente
        return (Math.abs(emptyRow - newRow) + Math.abs(emptyCol - newCol)) == 1;
    }
}
