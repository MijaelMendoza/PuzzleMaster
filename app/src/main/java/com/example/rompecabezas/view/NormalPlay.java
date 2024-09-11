package com.example.rompecabezas.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class NormalPlay extends AppCompatActivity {
    private static final int PROFUNDIDAD_MAXIMA = 100;

    private GridLayout tableroJugador, tableroMeta;
    private TextView cronometro;
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
        btnSolver.setOnClickListener(v -> solver());
        btnSalir.setOnClickListener(v -> salir());

        nuevoJuego();
    }

    private void nuevoJuego() {
        resetCronometro(); // Reiniciar cronómetro
        isPlaying = false;
        isFirstMove = true;

        size = nivelDificultad;

        // Generar puzzles
        generarPuzzle();
        rellenarTableros();

        tableroJugador.setOnClickListener(v -> {
            if (isFirstMove) {
                startCronometro(); // Iniciar cronómetro cuando el jugador haga el primer movimiento
                isFirstMove = false;
            }
        });
    }

    private void startCronometro() {
        startTime = SystemClock.elapsedRealtime();
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
        if ((Math.abs(emptyTileRow - row) == 1 && emptyTileCol == col) ||
                (Math.abs(emptyTileCol - col) == 1 && emptyTileRow == row)) {
            // Intercambiar piezas
            puzzleJugador[emptyTileRow][emptyTileCol] = puzzleJugador[row][col];
            puzzleJugador[row][col] = 0;

            emptyTileRow = row;
            emptyTileCol = col;

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
        builder.setMessage("¡Felicidades! Resolviste el puzzle en " + tiempo + " minutos.")
                .setPositiveButton("OK", (dialog, which) -> nuevoJuego());
        builder.create().show();
    }

    private void solver() {
        try {
            PriorityQueue<EstadoPuzzle> openList = new PriorityQueue<>();
            HashMap<String, Integer> closedSet = new HashMap<>();

            EstadoPuzzle estadoInicial = new EstadoPuzzle(puzzleJugador, emptyTileRow, emptyTileCol, 0, null);
            openList.add(estadoInicial);
            closedSet.put(estadoInicial.serializar(), estadoInicial.g + estadoInicial.h);

            // Limitar el número de nodos procesados para evitar usar demasiada memoria
            int maxNodosProcesados = 100000;  // Por ejemplo, limitar a 100k nodos
            int nodosProcesados = 0;

            while (!openList.isEmpty()) {
                if (nodosProcesados++ > maxNodosProcesados) {
                    throw new Exception("Se alcanzó el límite de nodos procesados. Posible exceso de memoria.");
                }

                EstadoPuzzle actual = openList.poll();

                // Verificar si hemos alcanzado el estado meta
                if (actual.esMeta(puzzleMeta)) {
                    mostrarSolucion(actual);
                    return;
                }

                // Generar sucesores
                for (EstadoPuzzle sucesor : actual.generarSucesores()) {
                    String estadoSerializado = sucesor.serializar();
                    int nuevoCosto = sucesor.g + sucesor.h;

                    // Solo agregar el sucesor si no lo hemos visto antes o si tiene un costo mejor
                    if (!closedSet.containsKey(estadoSerializado) || closedSet.get(estadoSerializado) > nuevoCosto) {
                        openList.add(sucesor);
                        closedSet.put(estadoSerializado, nuevoCosto);
                    }
                }
            }
            Toast.makeText(this, "No se encontró solución", Toast.LENGTH_SHORT).show();
        } catch (OutOfMemoryError e) {
            Toast.makeText(this, "Error: Memoria insuficiente para resolver el puzzle", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Mostrar la solución paso a paso
    private void mostrarSolucion(EstadoPuzzle estadoFinal) {
        List<EstadoPuzzle> pasos = new ArrayList<>();
        while (estadoFinal != null) {
            pasos.add(estadoFinal);
            estadoFinal = estadoFinal.padre;
        }
        Collections.reverse(pasos);

        // Mostrar los pasos con un pequeño retraso
        new Thread(() -> {
            for (EstadoPuzzle paso : pasos) {
                runOnUiThread(() -> actualizarTablero(paso.estado)); // Actualizar el tablero en la interfaz
                try {
                    Thread.sleep(500);  // Pausa de 0.5 segundos entre cada paso
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Actualiza el tablero de jugador en la UI con el estado actual del puzzle
    private void actualizarTablero(int[][] estado) {
        tableroJugador.removeAllViews();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                TextView tvJugador = new TextView(this);
                GridLayout.LayoutParams paramsJugador = new GridLayout.LayoutParams();
                paramsJugador.width = getTileSize();
                paramsJugador.height = getTileSize();
                paramsJugador.setMargins(5, 5, 5, 5);
                tvJugador.setLayoutParams(paramsJugador);

                int valueJugador = estado[i][j];
                if (valueJugador != 0) {
                    tvJugador.setText(String.valueOf((char) ('A' + valueJugador - 1)));
                    tvJugador.setBackgroundColor(getColorForTile(valueJugador));
                    tvJugador.setTextSize(24);
                    tvJugador.setGravity(Gravity.CENTER);
                    tvJugador.setTextColor(getResources().getColor(R.color.white));
                } else {
                    tvJugador.setText("");
                    tvJugador.setBackgroundColor(getResources().getColor(R.color.color_X));
                    tvJugador.setTextSize(24);
                    tvJugador.setGravity(Gravity.CENTER);
                }

                tableroJugador.addView(tvJugador);
            }
        }
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

        // Contar el número de inversiones
        for (int i = 0; i < puzzleList.size(); i++) {
            for (int j = i + 1; j < puzzleList.size(); j++) {
                if (puzzleList.get(i) != 0 && puzzleList.get(j) != 0 && puzzleList.get(i) > puzzleList.get(j)) {
                    countInversions++;
                }
            }
        }

        if (size % 2 == 1) {
            // Para grids de tamaño impar (como 3x3), el número de inversiones debe ser par
            return countInversions % 2 == 0;
        } else {
            // Para grids de tamaño par (como 4x4)
            int emptyRowFromBottom = size - (puzzleList.indexOf(0) / size);  // Contar la fila desde abajo
            // Verificar si la fila vacía es impar y el número de inversiones es par o viceversa
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

class EstadoPuzzle implements Comparable<EstadoPuzzle> {
    int[][] estado;
    int filaVacia, colVacia;
    int g, h;  // g: costo desde el inicio, h: heurística (distancia Manhattan + piezas fuera de lugar)
    EstadoPuzzle padre;

    public EstadoPuzzle(int[][] estado, int filaVacia, int colVacia, int g, EstadoPuzzle padre) {
        this.estado = new int[estado.length][estado.length];
        for (int i = 0; i < estado.length; i++) {
            this.estado[i] = Arrays.copyOf(estado[i], estado[i].length);
        }
        this.filaVacia = filaVacia;
        this.colVacia = colVacia;
        this.g = g;
        this.h = calcularHeuristica();
        this.padre = padre;
    }

    // Combina distancia Manhattan y el número de piezas fuera de lugar
    private int calcularHeuristica() {
        int manhattan = 0;
        int misplacedTiles = 0;
        int n = estado.length;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int valor = estado[i][j];
                if (valor != 0) {
                    int filaObjetivo = (valor - 1) / n;
                    int colObjetivo = (valor - 1) % n;
                    manhattan += Math.abs(i - filaObjetivo) + Math.abs(j - colObjetivo);

                    // Piezas fuera de lugar
                    if (i != filaObjetivo || j != colObjetivo) {
                        misplacedTiles++;
                    }
                }
            }
        }
        return manhattan + misplacedTiles;  // Combina ambas heurísticas
    }

    @Override
    public int compareTo(EstadoPuzzle otro) {
        return Integer.compare(this.g + this.h, otro.g + otro.h);  // f = g + h
    }

    public List<EstadoPuzzle> generarSucesores() {
        List<EstadoPuzzle> sucesores = new ArrayList<>();
        int n = estado.length;
        int[][] direcciones = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : direcciones) {
            int nuevaFila = filaVacia + d[0];
            int nuevaCol = colVacia + d[1];
            if (nuevaFila >= 0 && nuevaFila < n && nuevaCol >= 0 && nuevaCol < n) {
                int[][] nuevoEstado = new int[n][n];
                for (int i = 0; i < n; i++) {
                    nuevoEstado[i] = Arrays.copyOf(estado[i], n);
                }
                nuevoEstado[filaVacia][colVacia] = nuevoEstado[nuevaFila][nuevaCol];
                nuevoEstado[nuevaFila][nuevaCol] = 0;
                sucesores.add(new EstadoPuzzle(nuevoEstado, nuevaFila, nuevaCol, this.g + 1, this));
            }
        }
        return sucesores;
    }

    public boolean esMeta(int[][] objetivo) {
        return Arrays.deepEquals(this.estado, objetivo);
    }

    public String serializar() {
        StringBuilder sb = new StringBuilder();
        for (int[] fila : estado) {
            for (int val : fila) {
                sb.append(val).append(",");
            }
        }
        return sb.toString();
    }
}
