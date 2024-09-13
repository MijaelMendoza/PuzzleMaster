// Clase principal de la vista del puzzle
package com.example.rompecabezas.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.Manifest;

import com.example.rompecabezas.controller.JuegoController;
import com.example.rompecabezas.controller.PuzzleAdapter;
import com.example.rompecabezas.R;
import com.example.rompecabezas.controller.SolverImage;
import com.example.rompecabezas.model.JuegoModel;
import com.example.rompecabezas.model.Node;

public class image_puzzle extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int ANIMATION_DELAY = 200; // 500ms delay for each move

    private GridView gridView, gridViewSolved;
    private ArrayList<Bitmap> pieces, solvedPieces;
    private PuzzleAdapter puzzleAdapter, solvedPuzzleAdapter;
    private SolverImage solver;
    private Handler handler;
    private Random random;

    private TextView tvTimer, tvMoves;
    private int movesCount = 0;
    private long startTime = 0;
    private boolean isTimerRunning = false;
    private Runnable timerRunnable;

    private int userId;
    private JuegoController juegoController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_puzzle);

        // Inicializar GridViews
        gridView = findViewById(R.id.gridView);
        gridViewSolved = findViewById(R.id.gridViewSolved);
        tvTimer = findViewById(R.id.tvTimer);  // Cronómetro
        tvMoves = findViewById(R.id.tvMoves);

        pieces = new ArrayList<>();
        solvedPieces = new ArrayList<>();

        // Inicializar los adaptadores para ambos GridViews
        puzzleAdapter = new PuzzleAdapter(this, pieces);
        solvedPuzzleAdapter = new PuzzleAdapter(this, solvedPieces);

        gridView.setAdapter(puzzleAdapter);
        gridViewSolved.setAdapter(solvedPuzzleAdapter);

        // Configurar clics en el GridView para que el usuario pueda mover las piezas
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (!isTimerRunning) {
                startTimer();
            }
            movePiece(position);  // Mover la pieza seleccionada
        });

        // Botones de interacción
        Button btnChooseImage = findViewById(R.id.btnChooseImage);
        btnChooseImage.setOnClickListener(view -> chooseImageFromGallery());

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(view -> {
            if (checkAndRequestPermissions()) {
                takePhoto();
            }
        });

        Button btnSolver = findViewById(R.id.btnSolver);
        btnSolver.setOnClickListener(view -> {
            resetTimerAndMoves();  // Reiniciar tiempo y movimientos
            startTimer();  // Iniciar el cronómetro cuando el solver se use
            try {
                solvePuzzle();
            } catch (Exception e) {
                Log.d("error", e.getMessage());
                Toast.makeText(image_puzzle.this, "Error al resolver el puzzle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        handler = new Handler();
        random = new Random();
        solver = new SolverImage(); // Inicializar solver
        // Inicialización del controlador de juegos
        juegoController = new JuegoController(this);
        obtenerIdUsuario();
    }

    // Método para reiniciar el cronómetro y el contador de movimientos
    private void resetTimerAndMoves() {
        stopTimer();
        movesCount = 0;
        tvMoves.setText("Movimientos: 0");
        tvTimer.setText("Tiempo: 00:00");
    }

    // Método para iniciar el cronómetro
    private void startTimer() {
        startTime = System.currentTimeMillis();
        isTimerRunning = true;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                // Actualizar el cronómetro en pantalla
                tvTimer.setText(String.format("Tiempo: %02d:%02d", minutes, seconds));

                // Ejecutar de nuevo el runnable cada segundo
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    // Método para detener el cronómetro
    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
        isTimerRunning = false;
    }

    // Método para actualizar el contador de movimientos
    private void updateMoveCount() {
        movesCount++;
        tvMoves.setText("Movimientos: " + movesCount);
    }

    // Método para mover la pieza si es posible
    private void movePiece(int position) {
        int emptyPosition = pieces.indexOf(null);

        if (isAdjacent(emptyPosition, position)) {
            Collections.swap(pieces, emptyPosition, position);
            puzzleAdapter.notifyDataSetChanged();
            updateMoveCount();  // Incrementar el contador de movimientos

            if (isSolved()) {
                stopTimer();
                showCompletionDialog(false);  // Mostrar mensaje de victoria para el usuario
            }
        }
    }

    // Mostrar un diálogo con el tiempo y movimientos al completar el puzzle
    private void showCompletionDialog(boolean isSolver) {
        String message;
        long elapsedTime = System.currentTimeMillis() - startTime;
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (isSolver) {
            registrarJuego(false, elapsedTime, movesCount, true);
            message = String.format("El solver resolvió el puzzle en %02d:%02d y %d movimientos.", minutes, seconds, movesCount);
        } else {
            message = String.format("¡Felicidades! Resolviste el puzzle en %02d:%02d y %d movimientos.", minutes, seconds, movesCount);
            // Registrar el juego cuando el usuario gana
            registrarJuego(true, elapsedTime, movesCount, false);
        }

        new AlertDialog.Builder(this)
                .setTitle(isSolver ? "Puzzle Resuelto Automáticamente" : "¡Puzzle Completado!")
                .setMessage(message)
                .setPositiveButton("Nuevo Puzzle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetPuzzle();  // Reiniciar el puzzle cuando se presione "Nuevo Puzzle"
                    }
                })
                .setCancelable(false)
                .show();
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
        String dificultad = "media";
        String tipoJuego = "juego con imagenes";
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

    // Método para reiniciar el puzzle
    private void resetPuzzle() {
        shufflePuzzle();  // Desordenar el puzzle
        resetTimerAndMoves();  // Reiniciar el cronómetro y movimientos
        puzzleAdapter.notifyDataSetChanged();
        solvedPuzzleAdapter.notifyDataSetChanged();
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Los permisos de cámara y almacenamiento son necesarios para esta función", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Seleccionar una imagen de la galería
    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Tomar una foto con la cámara
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_REQUEST);
        } else {
            Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (requestCode == PICK_IMAGE_REQUEST && selectedImageUri != null) {
                try {
                    Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    updatePuzzleWithImage(selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == TAKE_PHOTO_REQUEST && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                updatePuzzleWithImage(photo);
            }
        }
    }


    // Método para desordenar el puzzle
    private void shufflePuzzle() {
        do {
            Collections.shuffle(pieces);
        } while (!isSolvable());
    }

    // Actualizar el puzzle con IDs únicos para cada pieza en lugar de hashCode
    private void updatePuzzleWithImage(Bitmap image) {
        int gridSize = 3;  // Suponiendo un puzzle 3x3
        int pieceWidth = image.getWidth() / gridSize;
        int pieceHeight = image.getHeight() / gridSize;

        pieces.clear();
        solvedPieces.clear();

        int idCounter = 1; // Contador para asignar un ID único a cada pieza

        // Cortar la imagen en piezas para ambos GridViews
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int x = col * pieceWidth;
                int y = row * pieceHeight;
                if (row == gridSize - 1 && col == gridSize - 1) {
                    // La última pieza es la vacía
                    pieces.add(null);
                    solvedPieces.add(null);
                } else {
                    Bitmap piece = Bitmap.createBitmap(image, x, y, pieceWidth, pieceHeight);
                    pieces.add(piece);  // Para el puzzle interactivo
                    solvedPieces.add(piece);  // Para el puzzle resuelto
                    // Asignar un ID único a cada pieza
                    piece.setDensity(idCounter++); // Usamos el ID en lugar del hashCode
                }
            }
        }

        // Desordenar solo el puzzle interactivo
        shufflePuzzle();

        // Actualizar ambos GridViews
        puzzleAdapter.notifyDataSetChanged();
        solvedPuzzleAdapter.notifyDataSetChanged();
    }

    // Método para verificar si el puzzle es resolvible
    private boolean isSolvable() {
        List<Bitmap> puzzlePieces = new ArrayList<>(pieces);
        int gridSize = 3;  // Tamaño del puzzle (3x3)

        // Extraer posiciones de piezas (ignorando el espacio vacío)
        List<Integer> tilePositions = new ArrayList<>();
        for (Bitmap piece : puzzlePieces) {
            if (piece != null) {
                tilePositions.add(piece.getDensity());  // Usar el ID único
            }
        }

        int inversions = 0;
        for (int i = 0; i < tilePositions.size(); i++) {
            for (int j = i + 1; j < tilePositions.size(); j++) {
                if (tilePositions.get(i) > tilePositions.get(j)) {
                    inversions++;
                }
            }
        }

        // Encontrar la posición de la pieza vacía
        int emptyRow = (pieces.indexOf(null)) / gridSize; // Fila donde está el espacio vacío

        // Si el gridSize es impar, el número de inversiones debe ser par.
        if (gridSize % 2 != 0) {
            return inversions % 2 == 0;
        } else {
            // Para gridSize par, el puzzle es resolvible si:
            return (emptyRow % 2 == 0) == (inversions % 2 != 0);
        }
    }

    // Método para resolver el puzzle usando A* y el nuevo sistema de IDs
    private void solvePuzzle() {
        List<String> initialState = new ArrayList<>();
        List<String> goalState = new ArrayList<>();

        // Crear el estado inicial basado en los IDs de las piezas actuales
        for (Bitmap piece : pieces) {
            if (piece != null) {
                initialState.add(String.valueOf(piece.getDensity()));  // Usamos el ID único
            } else {
                initialState.add("0");  // La pieza vacía se representa con "0"
            }
        }

        // Crear el estado objetivo basado en los IDs de las piezas resueltas
        for (Bitmap piece : solvedPieces) {
            if (piece != null) {
                goalState.add(String.valueOf(piece.getDensity()));  // Usamos el ID único
            } else {
                goalState.add("0");  // La última pieza vacía
            }
        }

        Log.d("Initial State", initialState.toString());
        Log.d("Goal State", goalState.toString());

        // Establecer el estado objetivo en el solver
        solver.setGoalState(goalState);

        // Resolver el puzzle
        List<Node> solution = solver.solvePuzzle(initialState);
        if (solution != null) {
            Log.d("Solution", solution.toString());
            animateSolution(solution);
        } else {
            Toast.makeText(this, "No se encontró una solución.", Toast.LENGTH_SHORT).show();
        }
    }

    // Resolver el puzzle automáticamente (solver)
    private void animateSolution(List<Node> solution) {
        Runnable runnable = new Runnable() {
            int stepIndex = 0;

            @Override
            public void run() {
                if (stepIndex < solution.size()) {
                    Node step = solution.get(stepIndex);
                    List<String> currentState = step.state;
                    movePuzzleAccordingToState(currentState);
                    stepIndex++;
                    updateMoveCount();  // Contar cada movimiento del solver
                    handler.postDelayed(this, ANIMATION_DELAY);
                } else {
                    stopTimer();
                    showCompletionDialog(true);  // Mostrar mensaje de finalización para el solver
                }
            }
        };
        handler.post(runnable);
    }

    // Mover las piezas del puzzle de acuerdo con el estado actual
    private void movePuzzleAccordingToState(List<String> targetState) {
        int emptyPosition = pieces.indexOf(null);
        int targetEmptyPosition = targetState.indexOf("0");

        // Verificar si las posiciones son adyacentes
        if (isAdjacent(emptyPosition, targetEmptyPosition)) {
            Collections.swap(pieces, emptyPosition, targetEmptyPosition);
            puzzleAdapter.notifyDataSetChanged();
        }
    }

    // Verificar si dos posiciones son adyacentes
    private boolean isAdjacent(int emptyPosition, int position) {
        int emptyRow = emptyPosition / 3;
        int emptyCol = emptyPosition % 3;
        int selectedRow = position / 3;
        int selectedCol = position % 3;

        return (Math.abs(emptyRow - selectedRow) == 1 && emptyCol == selectedCol) ||
                (Math.abs(emptyCol - selectedCol) == 1 && emptyRow == selectedRow);
    }

    // Verificar si el puzzle está resuelto
    private boolean isSolved() {
        for (int i = 0; i < pieces.size() - 1; i++) {
            if (pieces.get(i) == null || solvedPieces.get(i) == null) return false;

            // Comparar los identificadores únicos de las piezas en lugar del objeto Bitmap
            if (pieces.get(i).getDensity() != solvedPieces.get(i).getDensity()) {
                return false;
            }
        }
        return true;
    }

}
