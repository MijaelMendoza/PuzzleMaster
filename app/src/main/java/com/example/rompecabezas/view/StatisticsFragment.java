package com.example.rompecabezas.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rompecabezas.R;
import com.example.rompecabezas.controller.JuegoController;
import com.example.rompecabezas.controller.UserController;
import com.example.rompecabezas.model.JuegoModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private BarChart barChartMovimientos, barChartExperiencia, barChartTiempoPromedio;
    private PieChart pieChartResultados, pieChartDificultad;
    private JuegoController juegoController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar el controlador de juegos
        juegoController = new JuegoController(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Inicializar gráficos
        barChartMovimientos = view.findViewById(R.id.barChartMovimientos);
        pieChartResultados = view.findViewById(R.id.pieChartResultados);
        barChartExperiencia = view.findViewById(R.id.barChartExperiencia);
        pieChartDificultad = view.findViewById(R.id.pieChartDificultad);
        barChartTiempoPromedio = view.findViewById(R.id.barChartTiempoPromedio);

        // Obtener el ID del usuario desde las SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("user_id", -1);

        if (userId != -1) {
            List<JuegoModel> juegos = juegoController.obtenerJuegos();

            // Filtrar los juegos del usuario
            List<JuegoModel> userJuegos = new ArrayList<>();
            for (JuegoModel juego : juegos) {
                if (juego.getUsuarioId() == userId) {
                    userJuegos.add(juego);
                }
            }

            // Configurar gráficos con datos de la base de datos
            setupBarChartMovimientos(userJuegos);
            setupPieChartResultados(userJuegos);
            setupBarChartExperiencia(userJuegos);  // Solo experiencia ganada y perdida
            setupPieChartDificultad(userJuegos);
            setupBarChartTiempoPromedio(userJuegos);
        }

        return view;
    }

    private void setupBarChartMovimientos(List<JuegoModel> juegos) {
        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (JuegoModel juego : juegos) {
            entries.add(new BarEntry(i++, juego.getCantidadMovimientos()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Movimientos por juego");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(dataSet);
        barChartMovimientos.setData(data);

        // Configurar la descripción del gráfico
        Description description = new Description();
        description.setText("Movimientos en cada juego");
        barChartMovimientos.setDescription(description);

        barChartMovimientos.animateY(1000);
        barChartMovimientos.invalidate();
    }


    private void setupPieChartResultados(List<JuegoModel> juegos) {
        int wins = 0;
        int losses = 0;

        for (JuegoModel juego : juegos) {
            if (juego.isResultado()) {
                wins++;
            } else {
                losses++;
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(wins, "Ganados"));
        entries.add(new PieEntry(losses, "Perdidos"));

        PieDataSet dataSet = new PieDataSet(entries, "Resultados");
        dataSet.setColors(new int[]{ColorTemplate.COLORFUL_COLORS[1], ColorTemplate.COLORFUL_COLORS[0]}); // Verde para ganados, rojo para perdidos
        PieData data = new PieData(dataSet);
        pieChartResultados.setData(data);

        Description description = new Description();
        description.setText("Resultados de juegos");
        pieChartResultados.setDescription(description);

        pieChartResultados.animateY(1000);
        pieChartResultados.invalidate();
    }

    private void setupBarChartExperiencia(List<JuegoModel> juegos) {
        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (JuegoModel juego : juegos) {
            // Incluir solo experiencia ganada y perdida
            entries.add(new BarEntry(i++, juego.getExperienciaGanada()));
            entries.add(new BarEntry(i++, juego.getExperienciaPerdida()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Experiencia por juego");
        dataSet.setColors(new int[]{ColorTemplate.MATERIAL_COLORS[2], ColorTemplate.MATERIAL_COLORS[3]}); // Diferentes colores para ganada y perdida
        BarData data = new BarData(dataSet);
        barChartExperiencia.setData(data);

        Description description = new Description();
        description.setText("Experiencia Ganada y Perdida");
        barChartExperiencia.setDescription(description);

        // Indicar significado de cada color
        barChartExperiencia.getLegend().setEnabled(true);
        barChartExperiencia.getLegend().setTextSize(12f);
        barChartExperiencia.getLegend().setFormSize(12f);
        barChartExperiencia.getLegend().setXEntrySpace(10f);
        barChartExperiencia.getLegend().setExtra(new int[]{ColorTemplate.MATERIAL_COLORS[2], ColorTemplate.MATERIAL_COLORS[3]}, new String[]{"Ganada", "Perdida"});

        barChartExperiencia.animateY(1000);
        barChartExperiencia.invalidate();
    }

    private void setupPieChartDificultad(List<JuegoModel> juegos) {
        HashMap<String, Integer> dificultadCount = new HashMap<>();
        for (JuegoModel juego : juegos) {
            String dificultad = juego.getDificultad();
            dificultadCount.put(dificultad, dificultadCount.getOrDefault(dificultad, 0) + 1);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (String dificultad : dificultadCount.keySet()) {
            entries.add(new PieEntry(dificultadCount.get(dificultad), dificultad));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Juegos por dificultad");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(dataSet);
        pieChartDificultad.setData(data);

        Description description = new Description();
        description.setText("Juegos completados por dificultad");
        pieChartDificultad.setDescription(description);

        pieChartDificultad.animateY(1000);
        pieChartDificultad.invalidate();
    }

    private void setupBarChartTiempoPromedio(List<JuegoModel> juegos) {
        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (JuegoModel juego : juegos) {
            entries.add(new BarEntry(i++, juego.getTiempo()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tiempo promedio por juego");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        BarData data = new BarData(dataSet);
        barChartTiempoPromedio.setData(data);

        Description description = new Description();
        description.setText("Tiempo promedio de cada juego");
        barChartTiempoPromedio.setDescription(description);

        barChartTiempoPromedio.animateY(1000);
        barChartTiempoPromedio.invalidate();
    }
}
