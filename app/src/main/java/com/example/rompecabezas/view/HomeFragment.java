package com.example.rompecabezas.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rompecabezas.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar los botones en el fragmento
        Button btquickplay = rootView.findViewById(R.id.btQuickPlay);
        Button btastar = rootView.findViewById(R.id.btAstar);
        Button btbfs = rootView.findViewById(R.id.btBfs);
        Button btimagenes = rootView.findViewById(R.id.btImagenes);
        Button btversus = rootView.findViewById(R.id.btVersus);

        btquickplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes iniciar la actividad de juego rápido
                Intent intent = new Intent(getActivity(), QuickPlay.class);
                startActivity(intent);
            }
        });

        btimagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), image_puzzle.class);
                startActivity(intent);
            }
        });

        btversus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Versus.class);
                startActivity(intent);
            }
        });


        return rootView;
    }
}
