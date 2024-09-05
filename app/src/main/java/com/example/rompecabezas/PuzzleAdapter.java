package com.example.rompecabezas;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class PuzzleAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> pieces;

    public PuzzleAdapter(Context context, List<Bitmap> pieces) {
        this.context = context;
        this.pieces = pieces;
    }

    @Override
    public int getCount() {
        return pieces.size();
    }

    @Override
    public Object getItem(int position) {
        return pieces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        if (pieces.get(position) != null) {
            imageView.setImageBitmap(pieces.get(position));
        } else {
            imageView.setImageResource(android.R.color.transparent); // Representa la pieza vacía
        }

        return imageView;
    }
}

