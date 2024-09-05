package com.example.rompecabezas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btquickplay, btimagenes, btversus, bfs, astar, btnChangeUser;
    TextView tvUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isUserSelected()) {
            navigateToUserSelection();
            return;
        }

        setContentView(R.layout.activity_main);

        btquickplay = findViewById(R.id.btQuickPlay);
        btimagenes = findViewById(R.id.btImagenes);
        btversus = findViewById(R.id.btVersus);
        bfs = findViewById(R.id.btBfs);
        astar = findViewById(R.id.btAstar);
        btnChangeUser = findViewById(R.id.btnChangeUser);
        tvUserInfo = findViewById(R.id.tvUserInfo);

        loadCurrentUserInfo();

        btquickplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent qp = new Intent(MainActivity.this, QuickPlay.class);
                startActivity(qp);
            }
        });

        btimagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ip = new Intent(MainActivity.this, image_puzzle.class);
                startActivity(ip);
            }
        });

        btversus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vs = new Intent(MainActivity.this, Versus.class);
                startActivity(vs);
            }
        });

        bfs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bfs = new Intent(MainActivity.this, bfs.class);
                startActivity(bfs);
            }
        });

        astar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent astar = new Intent(MainActivity.this, QuickPlay.class);
                startActivity(astar);
            }
        });

        btnChangeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUserSelection();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserInfo(); // Actualizar la información del usuario cuando se reanuda la actividad
    }

    private boolean isUserSelected() {
        SharedPreferences sharedPreferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        return sharedPreferences.contains("currentUser");
    }

    private void navigateToUserSelection() {
        Intent intent = new Intent(MainActivity.this, UserSelectionActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadCurrentUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("currentUser", "N/A");
        int currentScore = sharedPreferences.getInt(currentUser, 0);
        tvUserInfo.setText("Usuario: " + currentUser + " - Puntaje: " + currentScore);
    }
}
