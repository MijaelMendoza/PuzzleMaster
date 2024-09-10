package com.example.rompecabezas.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rompecabezas.R;
import com.example.rompecabezas.model.FormField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormAdapter extends RecyclerView.Adapter<FormAdapter.FormViewHolder> {

    private List<FormField> formFields;
    private Context context;
    private Map<Integer, String> formData; // Almacenar datos de entrada

    public FormAdapter(Context context, List<FormField> formFields) {
        this.context = context;
        this.formFields = formFields;
        this.formData = new HashMap<>();
    }

    @NonNull
    @Override
    public FormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_form_field, parent, false);
        return new FormViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FormViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FormField field = formFields.get(position);
        holder.etFormField.setHint(field.getHint());
        holder.etFormField.setInputType(field.getInputType());

        // Añadir un TextWatcher para capturar los cambios en tiempo real
        holder.etFormField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Actualizar los datos del campo en el Map cuando se detecten cambios
                formData.put(position, charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public int getItemCount() {
        return formFields.size();
    }

    // Obtener datos de un campo en una posición específica
    public String getFormData(int position) {
        return formData.get(position);
    }

    public static class FormViewHolder extends RecyclerView.ViewHolder {
        EditText etFormField;

        public FormViewHolder(@NonNull View itemView) {
            super(itemView);
            etFormField = itemView.findViewById(R.id.etFormField);
        }
    }
}
