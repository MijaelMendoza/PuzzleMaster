package com.example.rompecabezas.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rompecabezas.MainActivity;
import com.example.rompecabezas.R;
import com.example.rompecabezas.controller.EditFieldAdapter;
import com.example.rompecabezas.controller.UserController;
import com.example.rompecabezas.model.EditField;
import com.example.rompecabezas.model.UserModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_GALLERY = 2;

    private UserController userController;
    private ImageView ivProfile;
    private Button btnUpdate, btnDelete;
    private RecyclerView recyclerView;
    private EditFieldAdapter editFieldAdapter;
    private UserModel currentUser;
    private Bitmap profileImage;
    private String imagePath; // Ruta de la imagen guardada
    private Uri selectedImageUri;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        userController = new UserController(getActivity());

        // Obtener el ID del usuario desde el Intent
        Intent intent = getActivity().getIntent();
        int userId = intent.getIntExtra("user_id", -1);

        currentUser = userController.obtenerUsuarioPorId(userId);

        ivProfile = rootView.findViewById(R.id.ivProfile);
        btnUpdate = rootView.findViewById(R.id.btnUpdate);
        btnDelete = rootView.findViewById(R.id.btnDelete);
        recyclerView = rootView.findViewById(R.id.recyclerViewForm);

        if (currentUser != null && currentUser.getFoto_perfil() != null) {
            ivProfile.setImageURI(Uri.parse(currentUser.getFoto_perfil()));
        }

        // Listener para seleccionar la foto de perfil
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog(); // Mostrar diálogo de selección de imagen
            }
        });

        // Configurar campos de edición para el perfil
        List<EditField> editFields = new ArrayList<>();
        editFields.add(new EditField("Nombre de usuario", currentUser.getNombre_usuario()));
        editFields.add(new EditField("Correo electrónico", currentUser.getCorreo()));
        editFields.add(new EditField("Password", currentUser.getContrasena()));
        editFields.add(new EditField("Pregunta de seguridad", currentUser.getPregunta_seguridad()));
        editFields.add(new EditField("Respuesta de seguridad", currentUser.getRespuesta_seguridad()));

        // Configurar el adaptador y RecyclerView
        editFieldAdapter = new EditFieldAdapter(getActivity(), editFields);
        recyclerView.setAdapter(editFieldAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarInformacionUsuario(); // Actualizar la información del usuario y la imagen
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarCuenta();
            }
        });

        return rootView;
    }

    // Mostrar cuadro de diálogo para seleccionar entre cámara o galería
    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getActivity());
        pictureDialog.setTitle("Seleccionar una opción");
        String[] pictureDialogItems = {
                "Tomar foto desde la cámara",
                "Seleccionar de la galería" };
        pictureDialog.setItems(pictureDialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        takePhotoFromCamera(); // Tomar una foto
                        break;
                    case 1:
                        choosePhotoFromGallery(); // Seleccionar una imagen de la galería
                        break;
                }
            }
        });
        pictureDialog.show();
    }

    // Método para tomar una foto desde la cámara
    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                profileImage = (Bitmap) extras.get("data");
                ivProfile.setImageBitmap(profileImage); // Mostrar la imagen tomada
            } else if (requestCode == PICK_IMAGE_GALLERY && data != null) {
                selectedImageUri = data.getData();
                try {
                    profileImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                    ivProfile.setImageBitmap(profileImage); // Mostrar la imagen seleccionada
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Guardar imagen en almacenamiento y devolver la ruta
    private String saveImage(Bitmap image) {
        String savedImagePath = null;
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = new File(storageDir, imageFileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            savedImagePath = imageFile.getAbsolutePath();
            Toast.makeText(getActivity(), "Imagen guardada", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savedImagePath;
    }

    // Método para actualizar la información del usuario
    private void actualizarInformacionUsuario() {
        String nombreUsuario = editFieldAdapter.getFieldValue(0);
        String correo = editFieldAdapter.getFieldValue(1);
        String contrasena = editFieldAdapter.getFieldValue(2);
        String preguntaSeguridad = editFieldAdapter.getFieldValue(3);
        String respuestaSeguridad = editFieldAdapter.getFieldValue(4);

        if (nombreUsuario.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(getActivity(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar la imagen si fue seleccionada
        if (profileImage != null) {
            imagePath = saveImage(profileImage);
            currentUser.setFoto_perfil(imagePath); // Guardar la ruta de la nueva imagen
        }

        // Actualizar los datos del usuario
        currentUser.setNombre_usuario(nombreUsuario);
        currentUser.setCorreo(correo);
        currentUser.setPregunta_seguridad(preguntaSeguridad);
        currentUser.setRespuesta_seguridad(respuestaSeguridad);
        currentUser.setContrasena(contrasena);

        int result = userController.actualizarUsuario(
                currentUser.getContrasena(),
                currentUser.getId(),
                nombreUsuario,
                correo,
                currentUser.getFoto_perfil(),
                preguntaSeguridad,
                respuestaSeguridad,
                currentUser.getFecha_creacion(),
                currentUser.getNivel(),
                currentUser.getExperiencia_acumulada()
        );

        if (result > 0) {

            Toast.makeText(getActivity(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarCuenta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eliminar cuenta");
        builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userController.eliminarUsuario(currentUser.getId());

                SharedPreferences sharedPref = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish(); // Cerrar la actividad actual
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }



}
