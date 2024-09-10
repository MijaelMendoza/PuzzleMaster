package com.example.rompecabezas.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rompecabezas.MainActivity;
import com.example.rompecabezas.R;

import java.util.ArrayList;
import java.util.Map;

public class UserSelectionActivity extends AppCompatActivity {

    private EditText etUsername;
    private Button btnCreateUser;
    private ListView lvUsers;
    private ArrayList<String> userList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        etUsername = findViewById(R.id.etUsername);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        lvUsers = findViewById(R.id.lvUsers);

        userList = new ArrayList<>();
        loadUsers();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        lvUsers.setAdapter(adapter);

        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                if (!username.isEmpty() && !userList.contains(username)) {
                    saveUser(username, 0);  // Puntaje inicial 0
                    Toast.makeText(UserSelectionActivity.this, "Usuario creado", Toast.LENGTH_SHORT).show();
                    userList.add(username + " - Puntaje: 0");
                    adapter.notifyDataSetChanged();
                    etUsername.setText("");
                } else if (userList.contains(username)) {
                    Toast.makeText(UserSelectionActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserSelectionActivity.this, "Por favor, ingresa un nombre de usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = userList.get(position).split(" - ")[0];  // Obtener solo el nombre del usuario
                selectUser(selectedUser);
                Toast.makeText(UserSelectionActivity.this, "Usuario " + selectedUser + " seleccionado", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            }
        });
    }

    private void loadUsers() {
        SharedPreferences sharedPreferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        Map<String, ?> allUsers = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allUsers.entrySet()) {
            if (!entry.getKey().equals("currentUser")) {
                userList.add(entry.getKey() + " - Puntaje: " + entry.getValue());
            }
        }
    }

    private void saveUser(String username, int score) {
        SharedPreferences sharedPreferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(username, score);
        editor.apply();
    }

    private void selectUser(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentUser", username);
        editor.apply();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(UserSelectionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
