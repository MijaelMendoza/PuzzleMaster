package com.example.rompecabezas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rompecabezas.controller.FormAdapter;
import com.example.rompecabezas.controller.UserController;
import com.example.rompecabezas.model.FormField;
import com.example.rompecabezas.model.UserModel;
import com.example.rompecabezas.view.HomeActivity;
import com.example.rompecabezas.view.RegisterView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnLogin, btnRegister;
    FormAdapter formAdapter;
    UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si hay una sesión de usuario activa
        SharedPreferences sharedPref = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = sharedPref.getInt("user_id", -1);

        if (userId != -1) {
            // Usuario ya está logueado, redirigir a HomeActivity
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("nombre_usuario", sharedPref.getString("nombre_usuario", ""));
            intent.putExtra("correo", sharedPref.getString("correo", ""));
            intent.putExtra("ruta_imagen", sharedPref.getString("ruta_imagen", ""));
            startActivity(intent);
            finish();  // Cerrar la pantalla de login
            return;  // Detener la ejecución de más código en onCreate()
        }

        // Configurar la vista del login si no hay sesión activa
        setContentView(R.layout.activity_main);

        // Inicializar el controlador de usuario
        userController = new UserController(this);

        recyclerView = findViewById(R.id.recyclerViewForm);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Configurar campos de formulario
        List<FormField> formFields = new ArrayList<>();
        formFields.add(new FormField("Correo electrónico", android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS));
        formFields.add(new FormField("Contraseña", android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD));

        // Configurar adaptador del RecyclerView
        formAdapter = new FormAdapter(this, formFields);
        recyclerView.setAdapter(formAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Listener para el botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = formAdapter.getFormData(0); // Correo
                String password = formAdapter.getFormData(1); // Contraseña

                // Validar que los campos no estén vacíos
                if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
                    // Verificar credenciales a través del controlador
                    UserModel user = userController.obtenerUsuarioPorCorreoYContrasena(email, password);

                    if (user != null) {
                        // Guardar los datos del usuario en SharedPreferences
                        SharedPreferences sharedPref = getSharedPreferences("user_session", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("user_id", user.getId());
                        editor.putString("nombre_usuario", user.getNombre_usuario());
                        editor.putString("correo", user.getCorreo());
                        editor.putString("ruta_imagen", user.getFoto_perfil()); // Guardar la ruta de la imagen
                        editor.apply();  // Aplicar los cambios

                        // Login exitoso, pasar los datos del usuario a HomeActivity
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra("user_id", user.getId());
                        intent.putExtra("nombre_usuario", user.getNombre_usuario());
                        intent.putExtra("correo", user.getCorreo());
                        intent.putExtra("ruta_imagen", user.getFoto_perfil());
                        startActivity(intent);
                        finish(); // Cerrar la actividad de login
                    } else {
                        Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Por favor, complete ambos campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener para el botón de registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterView.class);
                startActivity(intent);
            }
        });
    }
}
