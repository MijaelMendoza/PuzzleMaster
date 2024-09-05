package com.example.rompecabezas;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.Manifest;

public class image_puzzle extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int ANIMATION_DELAY = 500; // 500ms delay for each move

    private GridView gridView;
    private ImageView imageViewOriginal;  // ImageView para mostrar la imagen original
    private Bitmap originalImage;  // Imagen original
    private ArrayList<Bitmap> pieces;
    private PuzzleAdapter puzzleAdapter;
    private SolverImage solver;
    private Handler handler;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_puzzle);

        imageViewOriginal = findViewById(R.id.imageViewOriginal);  // Inicializar ImageView
        gridView = findViewById(R.id.gridView);
        pieces = new ArrayList<>();
        puzzleAdapter = new PuzzleAdapter(this, pieces);
        gridView.setAdapter(puzzleAdapter);

        solver = new SolverImage();  // Inicializar Solver
        handler = new Handler(); // Inicializar el Handler
        random = new Random(); // Inicializar el Random

        Button btnChooseImage = findViewById(R.id.btnChooseImage);
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageFromGallery();
            }
        });

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verificar permisos antes de intentar tomar una foto
                if (checkAndRequestPermissions()) {
                    takePhoto();
                }
            }
        });

        Button btnSolver = findViewById(R.id.btnSolver);
        btnSolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    solvePuzzle();  // Llamar al método solvePuzzle dentro de un bloque try-catch
                } catch (Exception e) {
                    Toast.makeText(image_puzzle.this, "Error al resolver el puzzle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();  // Imprimir el stack trace para depuración
                }
            }
        });

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
                // Permisos concedidos, puedes tomar la foto
                takePhoto();
            } else {
                // Permisos denegados
                Toast.makeText(this, "Los permisos de cámara y almacenamiento son necesarios para esta función", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

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

    private void updatePuzzleWithImage(Bitmap image) {
        originalImage = image;  // Guardar la imagen original
        imageViewOriginal.setImageBitmap(originalImage);  // Mostrar la imagen original

        int gridSize = 3;  // Para un puzzle 3x3
        int pieceWidth = image.getWidth() / gridSize;
        int pieceHeight = image.getHeight() / gridSize;

        pieces.clear();  // Limpiar la lista de piezas existente

        // Cortar la imagen en piezas y añadirlas a la lista de piezas en orden
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (row == gridSize - 1 && col == gridSize - 1) {
                    pieces.add(null); // Añadir la pieza vacía
                } else {
                    int x = col * pieceWidth;
                    int y = row * pieceHeight;
                    Bitmap piece = Bitmap.createBitmap(image, x, y, pieceWidth, pieceHeight);
                    pieces.add(piece);
                }
            }
        }

        // Desordenar el puzzle de manera válida
        shufflePuzzle();

        // Actualizar el adaptador del GridView
        puzzleAdapter.notifyDataSetChanged();

        // Configurar el listener de clics para las piezas del puzzle
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            movePiece(position);
        });
    }

    // Método para desordenar el puzzle utilizando movimientos válidos
    private void shufflePuzzle() {
        int emptyPosition = pieces.indexOf(null);
        int gridSize = 3;  // Tamaño de la cuadrícula 3x3
        int[] dx = {-1, 1, 0, 0};  // Movimientos posibles en X
        int[] dy = {0, 0, -1, 1};  // Movimientos posibles en Y

        for (int i = 0; i < 100; i++) {  // Realiza 100 movimientos aleatorios
            int emptyRow = emptyPosition / gridSize;
            int emptyCol = emptyPosition % gridSize;

            // Escoge una dirección aleatoria
            int direction = random.nextInt(4);
            int newRow = emptyRow + dx[direction];
            int newCol = emptyCol + dy[direction];

            if (newRow >= 0 && newRow < gridSize && newCol >= 0 && newCol < gridSize) {
                int newPosition = newRow * gridSize + newCol;
                // Intercambia la posición vacía con la nueva posición válida
                Collections.swap(pieces, emptyPosition, newPosition);
                emptyPosition = newPosition;  // Actualiza la posición vacía
            }
        }
    }

    // Método para mover la pieza si es posible
    private void movePiece(int position) {
        // Obtener la posición de la pieza vacía
        int emptyPosition = pieces.indexOf(null);

        // Comprobar si la posición seleccionada es adyacente a la vacía
        if (isAdjacent(emptyPosition, position)) {
            // Intercambiar la pieza seleccionada con la vacía
            Collections.swap(pieces, emptyPosition, position);
            puzzleAdapter.notifyDataSetChanged();

            // Verificar si el puzzle se ha resuelto
            if (isSolved()) {
                // Mostrar mensaje de victoria o resetear el juego
                Toast.makeText(this, "¡Puzzle completado!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para verificar si dos posiciones son adyacentes
    private boolean isAdjacent(int emptyPosition, int position) {
        // Diferencia de filas y columnas entre la posición seleccionada y la vacía
        int emptyRow = emptyPosition / 3;
        int emptyCol = emptyPosition % 3;
        int selectedRow = position / 3;
        int selectedCol = position % 3;

        return (Math.abs(emptyRow - selectedRow) == 1 && emptyCol == selectedCol) ||
                (Math.abs(emptyCol - selectedCol) == 1 && emptyRow == selectedRow);
    }

    // Método para verificar si el puzzle es resoluble
    private boolean isSolvable() {
        List<Bitmap> puzzlePieces = new ArrayList<>(pieces);
        puzzlePieces.remove(null); // Eliminar la pieza vacía para calcular las inversiones

        int inversions = 0;
        for (int i = 0; i < puzzlePieces.size(); i++) {
            for (int j = i + 1; j < puzzlePieces.size(); j++) {
                if (pieces.indexOf(puzzlePieces.get(i)) > pieces.indexOf(puzzlePieces.get(j))) {
                    inversions++;
                }
            }
        }
        return inversions % 2 == 0;
    }

    // Método para verificar si el puzzle está resuelto
    private boolean isSolved() {
        for (int i = 0; i < pieces.size() - 1; i++) {
            if (pieces.get(i) == null || pieces.get(i + 1) == null) return false;
            if (pieces.get(i).getGenerationId() > pieces.get(i + 1).getGenerationId()) return false;
        }
        return true;
    }

    private void solvePuzzle() {
        List<String> initialState = new ArrayList<>();
        // Llenar el estado inicial a partir de la posición actual de las piezas en el puzzle
        for (Bitmap piece : pieces) {
            if (piece != null) {
                // Convertir el Bitmap a una representación de String o un identificador único
                initialState.add(String.valueOf(piece.hashCode()));  // Aquí necesitas una forma de representar el estado como un String
            } else {
                initialState.add("0");  // La pieza vacía se representa con "0"
            }
        }

        List<Nodes> solution = solver.solvePuzzle(initialState);
        if (solution != null) {
            animateSolution(solution); // Animar la solución en lugar de aplicar los pasos directamente
        } else {
            Toast.makeText(this, "No se encontró una solución.", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateSolution(List<Nodes> solution) {
        Runnable runnable = new Runnable() {
            int stepIndex = 0;

            @Override
            public void run() {
                if (stepIndex < solution.size() - 1) {
                    Nodes step = solution.get(stepIndex);
                    updatePuzzleWithState(step.state); // Actualizar el puzzle con el estado actual
                    stepIndex++;
                    handler.postDelayed(this, ANIMATION_DELAY); // Continuar con el siguiente paso después de un delay
                } else {
                    Toast.makeText(image_puzzle.this, "Puzzle resuelto automáticamente!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        handler.post(runnable); // Iniciar la animación
    }

    // Método para actualizar el estado del puzzle a partir de una lista de Strings
    private void updatePuzzleWithState(List<String> state) {
        for (int i = 0; i < state.size(); i++) {
            String value = state.get(i);
            if (value.equals("0")) {
                pieces.set(i, null); // La pieza vacía
            } else {
                for (Bitmap piece : pieces) {
                    if (piece != null && String.valueOf(piece.hashCode()).equals(value)) {
                        pieces.set(i, piece); // Colocar la pieza en la posición correcta
                        break;
                    }
                }
            }
        }
        puzzleAdapter.notifyDataSetChanged(); // Actualizar el adaptador para reflejar los cambios
    }
}
