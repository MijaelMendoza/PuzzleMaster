package com.example.rompecabezas.controller;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rompecabezas.R;
import com.example.rompecabezas.model.EditField;

import java.util.List;

public class EditFieldAdapter extends RecyclerView.Adapter<EditFieldAdapter.EditFieldViewHolder> {

    private Context context;
    private List<EditField> editFields;

    public EditFieldAdapter(Context context, List<EditField> editFields) {
        this.context = context;
        this.editFields = editFields;
    }

    @NonNull
    @Override
    public EditFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_field, parent, false);
        return new EditFieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditFieldViewHolder holder, int position) {
        EditField editField = editFields.get(position);
        holder.tvLabel.setText(editField.getLabel());
        holder.etValue.setText(editField.getValue());

        holder.etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editFields.get(holder.getAdapterPosition()).setValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public int getItemCount() {
        return editFields.size();
    }

    public String getFieldValue(int position) {
        return editFields.get(position).getValue();
    }

    public static class EditFieldViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        EditText etValue;

        public EditFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            etValue = itemView.findViewById(R.id.etValue);
        }
    }
}
