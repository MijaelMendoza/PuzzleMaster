package com.example.rompecabezas.model;

public class FormField {
    private String hint;
    private int inputType;

    public FormField(String hint, int inputType) {
        this.hint = hint;
        this.inputType = inputType;
    }

    public String getHint() {
        return hint;
    }

    public int getInputType() {
        return inputType;
    }
}
