package com.books.dmitriy.reader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public DrawerLayout drawer;
    AlertDialog alertDialog;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        // Установка Navigation Drawer.
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    // Спрятать прогреcc загрузки.
    public void closeprogress() {
        alertDialog.dismiss();
    }


    // Показать прогресс загрузки.
    public void showprogress() {
        // Получаем Layout формы ввода.
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.loading_dialog, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.CustomDialog);
        alertDialogBuilder.setCancelable(false);
        // Устанавливаем форму в строитель диалога.
        alertDialogBuilder.setView(promptsView);
        final ImageView iv = (ImageView) promptsView.findViewById(R.id.imageView);
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1000);
        iv.startAnimation(anim);
        // Создаем диалог.
        alertDialog = alertDialogBuilder.create();
        // Отображаем диалог.
        alertDialog.show();
    }


    // Метод создания уведомлений Toast.
    public void showToast(String text) {
        // Создаем и отображаем текстовое уведомление.
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // Метод проверки наличия сети.
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    // Слушатель нажатий боковой шторки.
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mainpage) {

            final Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("key", "");
            intent.putExtra("Count", 0);
            startActivity(intent);

        } else if (id == R.id.nav_likebooks) {

            final Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("key", "like");
            intent.putExtra("Count", 0);
            startActivity(intent);

        } else if (id == R.id.nav_quit) {
            moveTaskToBack(true);
        }
        // Закрытие боковой шторки после нажатия.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
