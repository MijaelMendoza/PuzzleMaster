package com.example.rompecabezas;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btquickplay, btimagenes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btquickplay = findViewById(R.id.btQuickPlay);
        btimagenes = findViewById(R.id.btImagenes);

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
                Intent ip = new Intent(MainActivity.this, ImagePlay.class);
                startActivity(ip);
            }
        });


    }
}
