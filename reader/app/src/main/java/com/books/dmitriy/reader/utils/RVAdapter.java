package com.books.dmitriy.reader.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.books.dmitriy.reader.R;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    private List<String> Titles, Authors, Times, Likes;
    private List<Integer> select;
    private RVClickListener clicklistener = null;

    public RVAdapter(List<String> Titles, List<String> Authors, List<String> Times, List<String> Likes, List<Integer> select, RVClickListener listener) {
        this.Titles = Titles;
        this.Authors = Authors;
        this.Times = Times;
        this.Likes = Likes;
        this.select = select;
        clicklistener = listener;
    }


    @Override
    public RVAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_card, viewGroup, false);
        return new ViewHolder(v);
    }

    public void setClickListener(RVClickListener clicklistener) {
        this.clicklistener = clicklistener;
    }

    @Override
    public void onBindViewHolder(RVAdapter.ViewHolder Viewholder, int i) {

        // Заполнение элементов RecycleView данными.
        Viewholder.label.setText(Titles.get(i));
        Viewholder.label2.setText(Authors.get(i));
        Viewholder.label3.setText(Times.get(i));
        // Проверка на книги в БД.
        if (Likes.get(i).equals("no")) {
            Viewholder.icon.setImageResource(R.drawable.star);
        } else {
            Viewholder.icon.setImageResource(R.drawable.star2);
        }

        // Установка выделения. Если - 0, не выделяем, если - 1, выделяем. Список постоянно меняется.
        if (select.get(i).equals(0)) {
            Viewholder.cardlayout.setBackgroundResource(R.drawable.layout_toggle);
        } else if (select.get(i).equals(1)) {
            Viewholder.cardlayout.setBackgroundResource(R.color.cardbackground);
        }
    }

    @Override
    public int getItemCount() {

        return Titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView label, label2, label3;
        ImageView icon;
        RelativeLayout cardlayout;

        public ViewHolder(View view) {

            super(view);
            // Layout для слушателя нажатий.
            cardlayout = (RelativeLayout) itemView.findViewById(R.id.lvMain2);
            label = (TextView) itemView.findViewById(R.id.label);
            label2 = (TextView) itemView.findViewById(R.id.label2);
            label3 = (TextView) itemView.findViewById(R.id.label3);
            icon = (ImageView) itemView.findViewById(R.id.icon);

            cardlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clicklistener.itemClicked(itemView);
                }
            });

            // Слушатель длинных нажатий на элементы RecyclerView.
            cardlayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    clicklistener.itemlongClicked(itemView);
                    return true;
                }
            });

            icon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    clicklistener.starClicked(itemView);
                }

            });
        }

        @Override
        public void onClick(View v) {
            if (clicklistener != null) {
                clicklistener.itemClicked(v);
            }
        }

    }
}