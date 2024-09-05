package com.example.rompecabezas;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.PriorityQueue;

public class QuickPlay extends AppCompatActivity {

    TextView[] puzzleTiles;
    TextView[] sampleTiles;
    Button btAleatorio, btSolver, btSalir;
    int pivot;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_play);

        btAleatorio = findViewById(R.id.aleatorio);
        btSolver = findViewById(R.id.solver);
        btSalir = findViewById(R.id.btsalir);

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

        pivot = 4;

        for (int i = 0; i < puzzleTiles.length; i++) {
            final int index = i;
            puzzleTiles[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Move(index);
                }
            });
        }

        btAleatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> randomTilesForSample = generateSolvableTileList();
                List<String> randomTilesForPuzzle = generateSolvableTileList();
                setTiles(sampleTiles, randomTilesForSample);
                setTiles(puzzleTiles, randomTilesForPuzzle);
                pivot = findPivot();
            }
        });

        btSolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ejecutar la resolución en un hilo separado
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        solvePuzzle();
                    }
                }).start();
            }
        });

        btSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        int gridWidth = 3; // Asumiendo un puzzle 3x3

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
            final int index = i;
            handler.post(() -> {
                tileViews[index].setText(tile);
                if (tile.equals("X")) {
                    tileViews[index].setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    tileViews[index].setBackgroundColor(getResources().getColor(R.color.blue));
                }
            });
        }
    }

    private int findPivot() {
        for (int i = 0; i < puzzleTiles.length; i++) {
            if (puzzleTiles[i].getText().toString().equals("X")) {
                return i;
            }
        }
        return -1;
    }

    private void Move(int index) {
        if (isAdjacent(index, pivot)) {
            swap(index, pivot);
            pivot = index;

            if (isWin()) {
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

    private void swap(int index1, int index2) {
        String tempText = puzzleTiles[index1].getText().toString();
        Drawable tempBackground = puzzleTiles[index1].getBackground();

        puzzleTiles[index1].setText(puzzleTiles[index2].getText().toString());
        puzzleTiles[index1].setBackground(puzzleTiles[index2].getBackground());

        puzzleTiles[index2].setText(tempText);
        puzzleTiles[index2].setBackground(tempBackground);
    }

    private boolean isWin() {
        for (int i = 0; i < puzzleTiles.length; i++) {
            if (!puzzleTiles[i].getText().toString().equals(sampleTiles[i].getText().toString())) {
                return false;
            }
        }
        return true;
    }

    private void showWinMessage() {
        // Añadir 500 puntos al usuario actual
        addPointsToCurrentUser(500);

        new AlertDialog.Builder(this)
                .setTitle("¡Ganaste!")
                .setMessage("Has completado el rompecabezas, Obtuviste 500 puntos")
                .setPositiveButton("OK", null)
                .show();
    }

    private void addPointsToCurrentUser(int points) {
        SharedPreferences sharedPreferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("currentUser", null);

        if (currentUser != null) {
            int currentScore = sharedPreferences.getInt(currentUser, 0);
            int newScore = currentScore + points;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(currentUser, newScore);
            editor.apply();
        } else {
            Toast.makeText(this, "No se ha seleccionado ningún usuario", Toast.LENGTH_SHORT).show();
        }
    }

    // Algoritmo A* para encontrar la solución óptima
    private void solvePuzzle() {
        List<String> initial = new ArrayList<>();
        for (TextView tile : puzzleTiles) {
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
                final int delay = i * 250;
                handler.postDelayed(() -> {
                    setTiles(puzzleTiles, step);
                    pivot = findPivot();
                }, delay);
            }
        }
    }

    // Algoritmo A* modificado para encontrar la solución óptima
    private List<List<String>> aStar(List<String> initial, List<String> goal) {
        PriorityQueue<Nodes> openSet = new PriorityQueue<>();
        Set<List<String>> closedSet = new HashSet<>();
        openSet.add(new Nodes(initial, 0, heuristic(initial, goal), null));

        while (!openSet.isEmpty()) {
            Nodes currentNode = openSet.poll();
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
                        Nodes neighborNode = new Nodes(neighbor, tentativeGScore, tentativeGScore + heuristic(neighbor, goal), currentNode);
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return null;
    }

    // Método para reconstruir la ruta desde el nodo final
    private List<List<String>> reconstructPath(Nodes node) {
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

    // Encontrar el índice de inserción para mantener el orden de prioridad
    private int findInsertionIndex(Queue<List<String>> openSet, Queue<List<List<String>>> paths, int priority, List<String> goal) {
        int index = 0;
        for (List<String> state : openSet) {
            List<List<String>> path = ((LinkedList<List<List<String>>>) paths).get(index);
            int currentPriority = path.size() + heuristic(state, goal);
            if (priority < currentPriority) {
                return index;
            }
            index++;
        }
        return index;
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
