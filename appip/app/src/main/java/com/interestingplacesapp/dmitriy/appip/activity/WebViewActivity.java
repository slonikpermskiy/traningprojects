package com.interestingplacesapp.dmitriy.appip.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.interestingplacesapp.dmitriy.appip.R;


public class WebViewActivity extends BaseActivity implements BaseResponse {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Установка контента во FrameLayout класса BaseActivity.
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_web_view, null,false);
        frameLayout.addView(activityView);
        // Назначаем объекту интерфейса в суперклассе значение нашей Activity.
        response = WebViewActivity.this;
        // Объект VebView.
        final WebView webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Разрешение работы зума.
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        // Запрет показа кнопки плюс-минус.
        webSettings.setDisplayZoomControls(false);
        // Уменьшение открываемого контента до размера экрана при открытии.
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // Принудительная установка кодировки при загрузке контента в WebView.
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("UTF-8");
        Intent mintent = getIntent();
        // Установка WebViewClient нужна для того, чтобы при переходе по ссылке (в т.ч. внутри веб-страницы) она открывалась в нашем приложении, а не браузером по умолчанию.
        webView.setWebViewClient(new WebViewClient());
        Uri data = getIntent().getData();
        String url = data.toString();
        webView.loadUrl(url);
    }

    // Метод интерфейса для завершения Activity.
    @Override
    public void FinishActivity() {
        finish();
    }

    // Слушатель кнопки "назад".
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }
}