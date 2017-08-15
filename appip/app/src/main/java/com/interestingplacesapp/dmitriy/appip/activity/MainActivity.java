package com.interestingplacesapp.dmitriy.appip.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.interestingplacesapp.dmitriy.appip.R;
import java.io.File;


public class MainActivity extends BaseActivity implements BaseResponse{

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Установка контента во FrameLayout класса Base
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_main, null,false);
        frameLayout.addView(activityView);
        // Назначаем объекту интерфейса в суперклассе значение нашей Activity.
        response = MainActivity.this;
        // Установка Toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Установка кнопки открытия Navigation Drawer в Toolbar.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Инициализация класса-слушателя CardView.
        CardView card = (CardView) findViewById(R.id.card);
        MyViewHolder clickAdapter = new MyViewHolder(card);
        CardView card1 = (CardView) findViewById(R.id.card1);
        MyViewHolder clickAdapter1 = new MyViewHolder(card1);
        CardView card2 = (CardView) findViewById(R.id.card2);
        MyViewHolder clickAdapter2 = new MyViewHolder(card2);
        CardView card3 = (CardView) findViewById(R.id.card3);
        MyViewHolder clickAdapter3 = new MyViewHolder(card3);
        // Замена шрифта в TextView.
        TextView textView = (TextView) findViewById(R.id.label);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "georgiai.ttf");
        textView.setTypeface(typeFace);
        TextView textView3 = (TextView) findViewById(R.id.label3);
        textView3.setTypeface(typeFace);
        TextView textView1 = (TextView) findViewById(R.id.label1);
        textView1.setTypeface(typeFace);
        TextView textView2 = (TextView) findViewById(R.id.label2);
        textView2.setTypeface(typeFace);

        // Создание папки программы.
        File dirpar = new File(String.valueOf(getExternalFilesDir(null)));
        if (!dirpar.exists()) {
            dirpar.mkdir();
        }
        // Подписываемся на топик "news" Firebase. Для получения уведомлений.
        FirebaseMessaging.getInstance().subscribeToTopic("news");
    }


    // Слушатель кнопки "назад".
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            end("exit", null);
        }
    }

    // Метод интерфейса для завершения Activity.
    @Override
    public void FinishActivity() {
        finish();
    }


    // Класс-слушатель нажатий CardView.
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView image, image1, image2, image3;
        private LinearLayout cardlayout, cardlayout1, cardlayout2, cardlayout3;

        // Конструктор класса.
        public MyViewHolder(View view) {
            super(view);
            // Условие - какой элемент передан в класс.
            if (view.getId() == R.id.card) {
                image = (ImageView) view.findViewById(R.id.image);
                cardlayout = (LinearLayout) view.findViewById(R.id.cardlayout);
                cardlayout.setOnClickListener(this);
            } else if (view.getId() == R.id.card1) {
                image1 = (ImageView) view.findViewById(R.id.image1);
                cardlayout1 = (LinearLayout) view.findViewById(R.id.cardlayout1);
                cardlayout1.setOnClickListener(this);
            } else if (view.getId() == R.id.card2) {
                image2 = (ImageView) view.findViewById(R.id.image2);
                cardlayout2 = (LinearLayout) view.findViewById(R.id.cardlayout2);
                cardlayout2.setOnClickListener(this);
            } else if (view.getId() == R.id.card3) {
                image3 = (ImageView) view.findViewById(R.id.image3);
                cardlayout3 = (LinearLayout) view.findViewById(R.id.cardlayout3);
                cardlayout3.setOnClickListener(this);
            }
        }

        // Слушатель нажатий.
        @Override
        public void onClick(View v) {
            // Условие - какой элемент нажат.
            if (v.getId() == R.id.cardlayout) {
                // Проверка подключения к интернет.
                if (isOnline()) {
                    // Запуск параллельного потока для загрузки достопримечательности.
                    mAsyncTask = new GetCountries().execute();
                } else {
                    showSnack("There is no internet connection.");
                }
            } else if (v.getId() == R.id.cardlayout1) {
                // Проверка подключения к интернет.
                if (isOnline()) {
                    end ("mustsee", null);
                } else {
                    showSnack("There is no internet connection.");
                }
            } else if (v.getId() == R.id.cardlayout2) {
                end ("saved", null);
            } else if (v.getId() == R.id.cardlayout3) {
                if (isOnline()) {
                    end ("random", null);
                } else {
                    showSnack("There is no internet connection.");
                }
            }
        }
    }
}