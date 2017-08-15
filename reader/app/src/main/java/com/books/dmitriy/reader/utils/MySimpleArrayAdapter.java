package com.books.dmitriy.reader.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.books.dmitriy.reader.R;

import java.util.ArrayList;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> list1;
    private final ArrayList<String> list2;
    TextView textView, textView2;
    View rowView;

    // Конструктор. Принимаем значения.
    public MySimpleArrayAdapter(Context context, ArrayList<String> list1, ArrayList<String> list2) {
        super(context, R.layout.listv, list1);
        this.context = context;
        this.list1 = list1;
        this.list2 = list2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Устанавливаем наш элемент ListView и его поля.
        rowView = inflater.inflate(R.layout.listv, parent, false);
        textView = (TextView) rowView.findViewById(R.id.label);
        textView2 = (TextView) rowView.findViewById(R.id.label2);
        // Заполнение текстовых полей.
        textView.setText(list1.get(position));
        textView2.setText(list2.get(position));
        return rowView;
    }
}
