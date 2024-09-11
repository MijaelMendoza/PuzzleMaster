package com.example.rompecabezas.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.rompecabezas.MainActivity;
import com.example.rompecabezas.R;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar el DrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Obtener datos del usuario desde el Intent
        String nombreUsuario = getIntent().getStringExtra("nombre_usuario");
        String correo = getIntent().getStringExtra("correo");
        String rutaImagen = getIntent().getStringExtra("ruta_imagen");

        // Actualizar el header del NavigationView
        actualizarHeader(nombreUsuario, correo, rutaImagen);

        // Cargar el HomeFragment por defecto
        if (savedInstanceState == null) {
            cargarFragmento(new HomeFragment());  // Cargar HomeFragment al inicio
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Listener para manejar el evento de clic en los elementos del menú del drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    fragment = new HomeFragment();  // Cargar el fragmento de inicio
                } else if (id == R.id.nav_perfil) {
                    fragment = new ProfileFragment();  // Cargar el fragmento "Mi Perfil"
                } else if (id == R.id.nav_estadisticas) {
                    fragment = new StatisticsFragment();  // Cargar el fragmento "Mis Estadísticas"
                } else if (id == R.id.nav_cerrar_sesion) {
                    // Cerrar sesión y redirigir a MainActivity
                    SharedPreferences sharedPref = getSharedPreferences("user_session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.clear();  // Borrar todos los datos almacenados
                    editor.apply();

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                if (fragment != null) {
                    cargarFragmento(fragment);  // Cambiar al fragmento seleccionado
                }

                drawerLayout.closeDrawers();  // Cerrar el drawer después de seleccionar una opción
                return true;
            }
        });

    }

    // Método para actualizar los datos del usuario en el header del NavigationView
    private void actualizarHeader(String nombreUsuario, String correo, String rutaImagen) {
        View headerView = navigationView.getHeaderView(0); // Obtener el header del NavigationView
        TextView navUserName = headerView.findViewById(R.id.nav_user_name);
        TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
        ImageView navProfileImage = headerView.findViewById(R.id.profile_image);

        navUserName.setText(nombreUsuario);
        navUserEmail.setText(correo);

        // Usar Glide para cargar la imagen
        if (rutaImagen != null && !rutaImagen.isEmpty()) {
            Glide.with(this)
                    .load(rutaImagen)  // Cargar la imagen desde la ruta
                    .placeholder(R.drawable.ic_menu_profile)  // Imagen por defecto mientras carga
                    .into(navProfileImage);
        } else {
            navProfileImage.setImageResource(R.drawable.ic_menu_profile); // Imagen por defecto si no hay ruta
        }
    }

    // Método para cargar el fragmento
    private void cargarFragmento(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, fragment);  // Reemplazar el contenido con el fragmento seleccionado
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
