package com.interestingplacesapp.dmitriy.appip.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.interestingplacesapp.dmitriy.appip.R;
import com.interestingplacesapp.dmitriy.appip.helpers.DBHelper;
import com.interestingplacesapp.dmitriy.appip.helpers.TextViewLinkHandler;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


public class SavedSight extends BaseActivity implements GestureDetector.OnGestureListener, BaseResponse {

    // Переменные для получения Intent.
    String country;
    String place;
    int count = 0;
    // Разные переменные.
    ImageView photo, photo2, map;
    TextView textView, label, label1, label2, labeltitle, label0;
    ImageButton home;
    // Переключатель фотографий.
    private ImageSwitcher mImageSwitcher;
    // Переменная распознавания жестов (для фото).
    private GestureDetector gestureDetector;

    // Переменные для распознавания жеста "свайп" (для смены фотографии).
    private static final int SWIPE_MIN_DISTANCE = 40;
    private static final int SWIPE_MAX_OFF_PATH = 450;
    private static final int SWIPE_THRESHOLD_VELOCITY = 500;

    // Переменные Universal Image Loader.
    DisplayImageOptions options;
    ImageLoaderConfiguration config;
    // Сворачивающийся ToolBar.
    private CollapsingToolbarLayout collapsingToolbar;

    // Переменная БД.
    DBHelper DB;
    // Переменные для GPS-координат.
    String latitude;
    String longitude;
    // Кнопка раскрытия-закрытия TextView.
    ImageButton expcol;
    // Количество строк в основном TextView.
    int maxlines = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Установка контента во FrameLayout класса BaseActivity.
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_saved_sight, null,false);
        frameLayout.addView(activityView);
        // Назначаем объекту интерфейса в суперклассе значение нашей Activity.
        response = SavedSight.this;
        // Получение Intent.
        Intent mintent = getIntent();
        country = mintent.getStringExtra("Country");
        place = mintent.getStringExtra("Place");
        count = mintent.getIntExtra("Count", 0);
        // Установка Toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);
        // Установка кнопки открытия Navigation Drawer в Toolbar.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Разные View.
        textView = (TextView) findViewById(R.id.textView);
        photo = (ImageView) findViewById(R.id.photo);
        photo2 = (ImageView) findViewById(R.id.photo2);
        map = (ImageView) findViewById(R.id.map);
        mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        label0 = (TextView) findViewById(R.id.label0);
        label0.setMaxLines(maxlines);
        labeltitle = (TextView) findViewById(R.id.labeltitle);
        label = (TextView) findViewById(R.id.label);
        label1 = (TextView) findViewById(R.id.label1);
        label2 = (TextView) findViewById(R.id.label2);
        home = (ImageButton) findViewById(R.id.imageButton);
        expcol = (ImageButton) findViewById(R.id.expcol);
        // Регистрация в классе БД.
        DB = new DBHelper(this);

        // UNIVERSAL IMAGE LOADER SETUP
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageForEmptyUri(R.drawable.no_photo)
                .showImageOnFail(R.drawable.no_photo)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .memoryCacheExtraOptions(480, 800) // width, height
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
                .imageDownloader(new BaseImageDownloader(this, 5000, 30000)) // connectTimeout (5 s), readTimeout (30 s)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();
        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        // Запуск метода заполнения данных
        CreateSight();
        // For appropriate work CollapsingToolbar, cause it don't collapsing in normal way without it.
        ViewGroup mContentContainer = (ViewGroup) findViewById(R.id.content_container);
        mContentContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
    }


    // Метод заполнения данных по достопримечательности.
    public void CreateSight() {
        // Массив для данных по достопримечательности.
        ArrayList<String> Sight = new ArrayList();
        Sight = DB.dataforsight(country, place);
        // Загрузка картинок при помощи библиотеки Universal Image Loader.
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        // Скачиваем фото и отображаем.
        imageLoader.displayImage(Sight.get(8), photo, options);
        imageLoader.displayImage(Sight.get(9), photo2, options);
        imageLoader.displayImage(Sight.get(10), map, options);
        // Заполняем номер фото.
        textView.setText("1 of 2");
        // Устанавливаем слушатель жестов для ImageSwitcher.
        gestureDetector = new GestureDetector(this, this);
        mImageSwitcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        // Заполняем остальные данные.
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Garamond.ttf");
        label0.setTypeface(typeFace);
        // Заполняем текст html.
        String htmltext = readFromFile(Sight.get(2));
        // Разный метод отображения html в TextView в зависимости от версии ОС.
        if (Build.VERSION.SDK_INT >= 24) {
            label0.setText(Html.fromHtml(htmltext, 0)); // for 24 api and more
        } else {
            label0.setText(Html.fromHtml(htmltext)); // or for older api
        }
        // Слушатель нажатий на ссылки в TextView.
        label0.setMovementMethod(new TextViewLinkHandler() {
            @Override
            public void onLinkClick(String url) {
                if (isOnline()) {
                    final Intent intent = new Intent("com.interestingplacesapp.dmitriy.appip.activity.WebViewActivity");
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } else {
                    showSnack("There is no internet connection.");
                }
            }
        });
        // Устанавливаем все надписи в TextView.
        labeltitle.setText(Sight.get(0) + ", " +Sight.get(1)+" (saved place)");
        labeltitle.setTypeface(typeFace);
        label.setText(Sight.get(4));
        label.setTypeface(typeFace);
        label1.setText(Sight.get(3));
        label1.setTypeface(typeFace);
        label2.setText(Sight.get(7));
        label2.setTypeface(typeFace);
        //  Получение GPS-координат.
        latitude = Sight.get(5);
        longitude = Sight.get(6);

        // Регистрация кнопки location и назначение слушателя. Открытие оффлайн-карт.
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    String gps = latitude + ", " + longitude;
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:"+gps));
                    startActivity(intent);}
        });
        // Слушатель кнопки открытия-закрытия TextView.
        expcol.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (maxlines== 6) {
                    maxlines = Integer.MAX_VALUE;
                    label0.setMaxLines(maxlines);
                    expcol.setImageResource(R.drawable.col);
                } else {
                    maxlines = 6;
                    label0.setMaxLines(maxlines);
                    expcol.setImageResource(R.drawable.exp);
                }

            }
        });
        // Используется для отображения текста без сдвига  после перехода по ссылке и возвращения.
        LinearLayout focus = (LinearLayout) findViewById(R.id.content_cont);
        focus.getParent().requestChildFocus(focus,focus);
    }

    // Метод чтения текста из файла.
    private String readFromFile(String path) {
        String html = "Can't get text. Try to delete and save this sight again.";
        try{
        File file = new File(path);
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
        } finally {
            in.close();
        }
            html = new String(bytes);
        } catch (Exception e) {
            return  html;
        }
        return html;
    }


    // Первое создание меню toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        menu.findItem(R.id.action_back).setVisible(true);
        return true;
    }

    // Слушатель нажатия кнопок toolbar.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    // Слушатель кнопки "назад".
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            // Сбрасываем все данные и закрываем Activity (если есть интернет соединение).
        } else {
                super.onBackPressed();
                finish();
                final Intent myintent = new Intent(SavedSight.this, SavedActivity.class);
                myintent.putExtra("Count", count);
                myintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(myintent);
                System.exit(0);
        }
    }


    // Метод интерфейса для завершения Activity.
    @Override
    public void FinishActivity() {
        finish();
    }


    // Обязательный метод слушателя жестов.
    @Override
    public boolean onDown(MotionEvent e) {
        // Запрещаем прокрутку фотографии вниз, только вправо-влево (см. ниже).
        collapsingToolbar.requestDisallowInterceptTouchEvent(true);
        return true;
    }
    // Обязательный метод слушателя жестов.
    @Override
    public void onShowPress(MotionEvent e) {
    }
    // Слушатель одиночного нажатия на ImageSwitcher.
    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        // Устанавливаем анимацию.
        Animation inAnimation = new AlphaAnimation(0, 1);
        inAnimation.setDuration(2000);
        Animation outAnimation = new AlphaAnimation(1, 0);
        outAnimation.setDuration(2000);
        mImageSwitcher.setInAnimation(inAnimation);
        mImageSwitcher.setOutAnimation(outAnimation);
        // Показываем следующее фото.
        mImageSwitcher.showNext();
        // Устанавливаем подпись под фото.
        int x = mImageSwitcher.getDisplayedChild();
        if (x == 0) {
            textView.setText("1 of 2");
        }
        else if (x == 1) {
            textView.setText("2 of 2");
        }
        return true;
    }
    // Обязательный метод слушателя жестов.
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return true;
    }
    // Обязательный метод слушателя жестов.
    @Override
    public void onLongPress(MotionEvent e) {

    }
    // Слушатель "свайпа" влево и вправо.
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            float diffAbs = Math.abs(e1.getY() - e2.getY());
            float diff = e1.getX() - e2.getX();
            if (diffAbs > SWIPE_MAX_OFF_PATH)
                return false;

            // Левый "свайп".
            if (diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onLeftSwipe();
                // Правый "свайп".
            } else if (-diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onRightSwipe();
            }
        } catch (Exception e) {
        }
        return true;
    }

    // Метод выполнения левого "свайпа".
    public void onLeftSwipe() {
        // Анимация со сдвигом вбок (справа налево).
        mImageSwitcher.setInAnimation(this, R.anim.slide_in_left);
        mImageSwitcher.setOutAnimation(this, R.anim.slide_out_left);

        //Смена изображения в ImageSwitcher и текста под фото.
        int x = mImageSwitcher.getDisplayedChild();
        if (x == 0) {
            mImageSwitcher.setDisplayedChild(1);
            textView.setText("2 of 2");
        }
        else if (x == 1) {
            mImageSwitcher.setDisplayedChild(0);
            textView.setText("1 of 2");
        }
    }

    // Метод выполнения правого "свайпа".
    public void onRightSwipe() {
        // Анимация со сдвигом вбок (слева направо).
        mImageSwitcher.setInAnimation(this, R.anim.slide_in_right);
        mImageSwitcher.setOutAnimation(this, R.anim.slide_out_right);

        //Смена изображения в ImageSwitcher и текста под фото.
        int x = mImageSwitcher.getDisplayedChild();
        if (x == 0) {
            mImageSwitcher.setDisplayedChild(1);
            textView.setText("2 of 2");
        }
        else if (x == 1) {
            mImageSwitcher.setDisplayedChild(0);
            textView.setText("1 of 2");
        }
    }
}