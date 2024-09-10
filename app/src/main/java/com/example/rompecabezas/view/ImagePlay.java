package com.example.rompecabezas.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rompecabezas.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ImagePlay extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TARGET_WIDTH = 240;  // Anchura objetivo
    private static final int TARGET_HEIGHT = 240; // Altura objetivo

    ImageView[] puzzleTiles;
    ImageView[] sampleTiles;
    Button btAleatorio, btSolver, btSalir, btCargarImagen;
    int pivot;
    Handler handler = new Handler();
    Bitmap originalImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_play);

        btAleatorio = findViewById(R.id.aleatorio);
        btSolver = findViewById(R.id.solver);
        btSalir = findViewById(R.id.btsalir);
        btCargarImagen = findViewById(R.id.btCargarImagen);

        puzzleTiles = new ImageView[]{
                findViewById(R.id.tvA), findViewById(R.id.tvB), findViewById(R.id.tvC),
                findViewById(R.id.tvD), findViewById(R.id.tvX), findViewById(R.id.tvE),
                findViewById(R.id.tvF), findViewById(R.id.tvG), findViewById(R.id.tvH)
        };

        sampleTiles = new ImageView[]{
                findViewById(R.id.sampleA), findViewById(R.id.sampleB), findViewById(R.id.sampleC),
                findViewById(R.id.sampleD), findViewById(R.id.sampleE), findViewById(R.id.sampleF),
                findViewById(R.id.sampleG), findViewById(R.id.sampleH), findViewById(R.id.sampleX)
        };

        pivot = 4;

        // Cargar imagen predeterminada de manera optimizada
        new LoadImageTask().execute(R.drawable.default_image);

        btAleatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalImage != null) {
                    List<Bitmap> randomTilesForSample = generateSolvableTileList(originalImage);
                    List<Bitmap> randomTilesForPuzzle = generateSolvableTileList(originalImage);
                    setTiles(sampleTiles, randomTilesForSample);
                    setTiles(puzzleTiles, randomTilesForPuzzle);
                    pivot = findPivot();
                }
            }
        });

        // Añadir eventos para los clics en las piezas del rompecabezas
        for (int i = 0; i < puzzleTiles.length; i++) {
            final int index = i;
            puzzleTiles[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAdjacent(index, pivot)) {
                        swapTiles(index, pivot);
                        pivot = index;

                        if (isWin()) {
                            showWinMessage();
                        }
                    }
                }
            });
        }

        btSolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        btCargarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                new LoadImageTask().execute(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private List<Bitmap> generateSolvableTileList(Bitmap image) {
        List<Bitmap> tiles = divideImage(image);
        Collections.shuffle(tiles);

        return tiles;
    }
    private class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            if (params[0] instanceof Integer) {
                bitmap = decodeSampledBitmapFromResource((Integer) params[0], TARGET_WIDTH, TARGET_HEIGHT);
            } else if (params[0] instanceof Uri) {
                try {
                    bitmap = decodeSampledBitmapFromUri((Uri) params[0], TARGET_WIDTH, TARGET_HEIGHT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                originalImage = bitmap;
                setTiles(sampleTiles, divideImage(originalImage));
                setTiles(puzzleTiles, divideImage(originalImage));
                pivot = findPivot();
            }
        }
    }

    private Bitmap decodeSampledBitmapFromResource(int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(getResources(), resId, options);
    }

    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private List<Bitmap> divideImage(Bitmap image) {
        int rows = 3;
        int cols = 3;
        int pieceWidth = image.getWidth() / cols;
        int pieceHeight = image.getHeight() / rows;
        List<Bitmap> pieces = new ArrayList<>(rows * cols);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * pieceWidth;
                int y = row * pieceHeight;
                Bitmap piece = Bitmap.createBitmap(image, x, y, pieceWidth, pieceHeight);
                pieces.add(piece);
            }
        }

        return pieces;
    }

    private void setTiles(ImageView[] tileViews, List<Bitmap> tiles) {
        for (int i = 0; i < tileViews.length; i++) {
            final Bitmap tile = tiles.get(i);
            final int index = i;
            handler.post(() -> {
                if (index == pivot) {
                    tileViews[index].setImageDrawable(null);
                } else {
                    tileViews[index].setImageBitmap(tile);
                }
            });
        }
    }

    private int findPivot() {
        for (int i = 0; i < puzzleTiles.length; i++) {
            if (puzzleTiles[i].getDrawable() == null) {
                return i;
            }
        }
        return -1;
    }

    private boolean isAdjacent(int index1, int index2) {
        int row1 = index1 / 3;
        int col1 = index1 % 3;
        int row2 = index2 / 3;
        int col2 = index2 % 3;

        return (Math.abs(row1 - row2) == 1 && col1 == col2) || (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    private void swapTiles(int index1, int index2) {
        Bitmap tempBitmap = ((BitmapDrawable) puzzleTiles[index1].getDrawable()).getBitmap();

        puzzleTiles[index1].setImageBitmap(((BitmapDrawable) puzzleTiles[index2].getDrawable()).getBitmap());
        puzzleTiles[index2].setImageBitmap(tempBitmap);
    }

    private boolean isWin() {
        for (int i = 0; i < puzzleTiles.length; i++) {
            if (((BitmapDrawable) puzzleTiles[i].getDrawable()).getBitmap() !=
                    ((BitmapDrawable) sampleTiles[i].getDrawable()).getBitmap()) {
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

    private void solvePuzzle() {
        List<Bitmap> initial = new ArrayList<>();
        for (ImageView tile : puzzleTiles) {
            BitmapDrawable drawable = (BitmapDrawable) tile.getDrawable();
            initial.add(drawable == null ? null : drawable.getBitmap());
        }

        List<Bitmap> goal = new ArrayList<>();
        for (ImageView tile : sampleTiles) {
            BitmapDrawable drawable = (BitmapDrawable) tile.getDrawable();
            goal.add(drawable == null ? null : drawable.getBitmap());
        }

        List<List<Bitmap>> solutionPath = bfs(initial, goal);

        if (solutionPath != null && !solutionPath.isEmpty()) {
            for (int i = 1; i < solutionPath.size(); i++) {
                List<Bitmap> step = solutionPath.get(i);
                final int delay = i * 300;
                handler.postDelayed(() -> {
                    setTiles(puzzleTiles, step);
                    pivot = findPivot();
                }, delay);
            }
        }
    }

    private List<List<Bitmap>> bfs(List<Bitmap> initial, List<Bitmap> goal) {
        Queue<List<Bitmap>> queue = new LinkedList<>();
        Queue<List<List<Bitmap>>> paths = new LinkedList<>();
        Set<List<Bitmap>> visited = new HashSet<>();
        queue.add(initial);
        paths.add(new ArrayList<>(List.of(initial)));
        visited.add(initial);

        while (!queue.isEmpty()) {
            List<Bitmap> current = queue.poll();
            List<List<Bitmap>> path = paths.poll();

            if (current.equals(goal)) {
                return path;
            }

            int emptyIndex = current.indexOf(null);
            int[] directions = {-3, 3, -1, 1};

            for (int dir : directions) {
                int newIndex = emptyIndex + dir;

                if (newIndex >= 0 && newIndex < 9 && isValidMove(emptyIndex, newIndex)) {
                    List<Bitmap> neighbor = new ArrayList<>(current);
                    Collections.swap(neighbor, emptyIndex, newIndex);

                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);

                        List<List<Bitmap>> newPath = new ArrayList<>(path);
                        newPath.add(neighbor);
                        paths.add(newPath);

                        if (neighbor.equals(goal)) {
                            return newPath;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidMove(int emptyIndex, int newIndex) {
        int emptyRow = emptyIndex / 3;
        int emptyCol = emptyIndex % 3;
        int newRow = newIndex / 3;
        int newCol = newIndex % 3;

        return (Math.abs(emptyRow - newRow) + Math.abs(emptyCol - newCol)) == 1;
    }
}
