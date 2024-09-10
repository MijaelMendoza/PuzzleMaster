package com.example.rompecabezas.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rompecabezas.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class bfs extends AppCompatActivity {

    TextView[] puzzleTiles;
    TextView[] sampleTiles;
    Button btAleatorio, btSolver, btSalir;
    int pivot;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bfs);

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
        for (int i = 0; i < tiles.size() - 1; i++) {
            for (int j = i + 1; j < tiles.size(); j++) {
                if (!tiles.get(i).equals("X") && !tiles.get(j).equals("X") && tiles.get(i).compareTo(tiles.get(j)) > 0) {
                    inversions++;
                }
            }
        }
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
        new AlertDialog.Builder(this)
                .setTitle("¡Ganaste!")
                .setMessage("Has completado el rompecabezas")
                .setPositiveButton("OK", null)
                .show();
    }

    // Resolver el puzzle usando BFS
    private void solvePuzzle() {
        List<String> initial = new ArrayList<>();
        for (TextView tile : puzzleTiles) {
            initial.add(tile.getText().toString());
        }

        List<String> goal = new ArrayList<>();
        for (TextView tile : sampleTiles) {
            goal.add(tile.getText().toString());
        }

        List<List<String>> solutionPath = bfs(initial, goal);

        if (solutionPath != null && !solutionPath.isEmpty()) {
            for (int i = 1; i < solutionPath.size(); i++) {
                List<String> step = solutionPath.get(i);
                final int delay = i * 300;
                handler.postDelayed(() -> {
                    setTiles(puzzleTiles, step);
                    pivot = findPivot();
                }, delay);
            }
        }
    }

    // Algoritmo de BFS para encontrar la solución
    private List<List<String>> bfs(List<String> initial, List<String> goal) {
        Queue<List<String>> queue = new LinkedList<>();
        Queue<List<List<String>>> paths = new LinkedList<>();
        Set<List<String>> visited = new HashSet<>();
        queue.add(initial);
        paths.add(new ArrayList<>(List.of(initial)));
        visited.add(initial);

        while (!queue.isEmpty()) {
            List<String> current = queue.poll();
            List<List<String>> path = paths.poll();

            if (current.equals(goal)) {
                return path;
            }

            int emptyIndex = current.indexOf("X");
            int[] directions = {-3, 3, -1, 1};

            for (int dir : directions) {
                int newIndex = emptyIndex + dir;

                // Validar si el nuevo índice es válido y si se está moviendo de manera correcta
                if (newIndex >= 0 && newIndex < 9 && isValidMove(emptyIndex, newIndex)) {
                    List<String> neighbor = new ArrayList<>(current);
                    Collections.swap(neighbor, emptyIndex, newIndex);

                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);

                        // Agregar este movimiento al camino
                        List<List<String>> newPath = new ArrayList<>(path);
                        newPath.add(neighbor);
                        paths.add(newPath);

                        // Si encontramos la solución, devolvemos el camino
                        if (neighbor.equals(goal)) {
                            return newPath;
                        }
                    }
                }
            }
        }
        return null;
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