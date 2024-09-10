package com.example.rompecabezas.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rompecabezas.MainActivity;
import com.example.rompecabezas.R;
import com.example.rompecabezas.controller.FormAdapter;
import com.example.rompecabezas.controller.UserController;
import com.example.rompecabezas.model.FormField;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterView extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_GALLERY = 2;

    RecyclerView recyclerView;
    ImageView ivProfile;
    Button btnRegister;
    Bitmap profileImage;
    String imagePath; // Ruta de la imagen guardada
    FormAdapter formAdapter;

    String username, email, password, securityQuestion, answer;

    // Instancia de UserController para manejar el registro
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_view);

        // Inicializar el controlador
        userController = new UserController(this);

        ivProfile = findViewById(R.id.ivProfile);
        btnRegister = findViewById(R.id.btnRegister);
        recyclerView = findViewById(R.id.recyclerViewForm);

        // Configurar campos de formulario para el registro
        List<FormField> formFields = new ArrayList<>();
        formFields.add(new FormField("Nombre de usuario", InputType.TYPE_CLASS_TEXT));
        formFields.add(new FormField("Correo electrónico", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS));
        formFields.add(new FormField("Contraseña", InputType.TYPE_TEXT_VARIATION_PASSWORD));
        formFields.add(new FormField("Pregunta de seguridad", InputType.TYPE_CLASS_TEXT));
        formFields.add(new FormField("Respuesta", InputType.TYPE_CLASS_TEXT));

        // Configurar adaptador del RecyclerView
        formAdapter = new FormAdapter(this, formFields);
        recyclerView.setAdapter(formAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Listener para seleccionar la foto de perfil
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        // Listener para el botón de registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los campos
                username = formAdapter.getFormData(0);
                email = formAdapter.getFormData(1);
                password = formAdapter.getFormData(2);
                securityQuestion = formAdapter.getFormData(3);
                answer = formAdapter.getFormData(4);

                // Obtener la fecha actual
                String fechaActual = obtenerFechaActual();

                // Verificar que ninguno de los campos sea nulo o esté vacío
                if (isValidInput(username, email, password, securityQuestion, answer)) {
                    if (profileImage != null) {
                        // Guardar la imagen en almacenamiento y obtener la ruta
                        imagePath = saveImage(profileImage);
                    } else {
                        imagePath = ""; // No se seleccionó imagen
                    }

                    // Registrar usuario en la base de datos utilizando el controlador
                    long id = userController.registrarUsuario(
                            password,
                            username,
                            email,
                            imagePath, // Ruta de la imagen guardada
                            securityQuestion,
                            answer,
                            fechaActual, // Fecha de creación (podrías obtener la fecha actual)
                            1, // Nivel inicial del usuario
                            0  // Experiencia acumulada inicial
                    );

                    if (id != -1) {
                        Toast.makeText(RegisterView.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterView.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterView.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterView.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Verificar que los campos no sean nulos o vacíos
    private boolean isValidInput(String username, String email, String password, String securityQuestion, String answer) {
        return username != null && !username.isEmpty() &&
                email != null && !email.isEmpty() &&
                password != null && !password.isEmpty() &&
                securityQuestion != null && !securityQuestion.isEmpty() && answer != null && !answer.isEmpty();
    }

    public String obtenerFechaActual() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    // Guardar imagen en almacenamiento y devolver la ruta
    private String saveImage(Bitmap image) {
        String savedImagePath = null;
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = new File(storageDir, imageFileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            savedImagePath = imageFile.getAbsolutePath();
            Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savedImagePath;
    }

    // Mostrar el cuadro de diálogo para seleccionar entre Cámara o Galería
    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Seleccionar una opción");
        String[] pictureDialogItems = {
                "Tomar foto desde la cámara",
                "Seleccionar de la galería" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                takePhotoFromCamera();
                                break;
                            case 1:
                                choosePhotoFromGallery();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    // Método para tomar una foto desde la cámara
    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Método para elegir una foto de la galería
    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                profileImage = (Bitmap) extras.get("data");
                ivProfile.setImageBitmap(profileImage); // Mostrar la imagen tomada
            } else if (requestCode == PICK_IMAGE_GALLERY && data != null) {
                Uri selectedImageUri = data.getData();
                try {
                    profileImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    ivProfile.setImageBitmap(profileImage); // Mostrar la imagen seleccionada de la galería
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
