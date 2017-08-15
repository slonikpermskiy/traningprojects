package com.interestingplacesapp.dmitriy.appip.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.interestingplacesapp.dmitriy.appip.R;
import com.interestingplacesapp.dmitriy.appip.helpers.DBHelper;
import com.interestingplacesapp.dmitriy.appip.helpers.JSONParser;
import com.interestingplacesapp.dmitriy.appip.helpers.NiceSupportMapFragment;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Main2Activity extends BaseActivity implements OnMapReadyCallback, BaseResponse {

    String country;
    // Переменная для запоминания позиции списка.
    int count = 0;
    TextView EmptyText, title;
    // Объект класса JSONParser.
    JSONParser jsonParser = new JSONParser();
    // Путь к REST API.
    private static final String url_sight1 = "http://www.edu-sam.com/restapiinterestingplaces/api/get_sights_bycountry.php";
    private static final String url_sight2 = "http://www.edu-sam.com/restapiinterestingplaces/api/get_mustsee_sights.php";
    // Переменные JSON.
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_COUNTRIES = "countries";
    private static final String TAG_COUNTRY = "Country";
    private static final String TAG_PLACE = "Place";
    private static final String TAG_PHOTO = "Photo";
    private static final String TAG_LATITUDE = "Latitude";
    private static final String TAG_LONGITUDE = "Longitude";

    // Переменные для RecycleView.
    RecyclerView rv;
    RVAdapter adapter;
    LinearLayoutManager llm;
    // Переменные Universal Image Loader.
    DisplayImageOptions options;
    ImageLoaderConfiguration config;
    // Скрывающийся ToolBar.
    AppBarLayout appBar;
    // Переменная БД.
    DBHelper DB;

    ArrayList<String> Countries = new ArrayList();
    ArrayList<String> Places = new ArrayList();
    ArrayList<String> Photos = new ArrayList();
    ArrayList<Double> Latitudes = new ArrayList();
    ArrayList<Double> Longitudes = new ArrayList();

    // Переменные карты.
    NiceSupportMapFragment mapFragment;
    GoogleMap mymap;
    RelativeLayout mapcont;
    // Остальные переменные.
    ImageButton home;
    CollapsingToolbarLayout coltoolbar;
    Toolbar toolbar;
    AppBarLayout.LayoutParams params;
    RadioButton but1, but2;
    RadioGroup group;
    ImageLoader imageLoader;
    Bitmap mybitmap, mybitmap2;
    // Индикатор для загрузки маркеров только один раз.
    int countmap = 0;
    // Переменная опций маркеров.
    MarkerOptions marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Установка контента во FrameLayout класса BaseActivity.
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_main2, null,false);
        frameLayout.addView(activityView);
        // Назначаем объекту интерфейса в суперклассе значение нашей Activity.
        response = Main2Activity.this;
        // Объект RecyclerView.
        rv = (RecyclerView)findViewById(R.id.rv);
        // Установка Toolbar.
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Установка кнопки открытия Navigation Drawer в Toolbar.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Получение Intent.
        Intent intent = getIntent();
        country = intent.getStringExtra("Country");
        if (country.equals("Empty") | country == null) {
            country = "mustsee";
        }
        count = intent.getIntExtra("Count", 0);
        // Скрывающийся Toolbar.
        appBar = (AppBarLayout) findViewById(R.id.main_appbar);
        // Запуск параллельного потока для загрузки списка мест по стране.
        mAsyncTask = new GetSights().execute();
        // Скрываем карту.
        mapcont = (RelativeLayout) findViewById(R.id.mapcont);
        mapcont.setVisibility(View.GONE);
        group = (RadioGroup) findViewById(R.id.toggle);
        group.setVisibility(View.GONE);
        // Кнопки-переключатели карта-список.
        but1 = (RadioButton) findViewById(R.id.list);
        but1.setChecked(true);
        but2 = (RadioButton) findViewById(R.id.map);
        coltoolbar = (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);
        params = (AppBarLayout.LayoutParams) coltoolbar.getLayoutParams();
        // Вызывается если нет контента для загрузки.
        EmptyText = (TextView) findViewById(R.id.EmptyText);
        EmptyText.setVisibility(View.GONE);
        // Toolbar Title.
        title = (TextView) findViewById(R.id.title);
        // Регистрация в классе БД.
        DB = new DBHelper(this);

        // UNIVERSAL IMAGE LOADER SETUP
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
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
                .memoryCache(new UsingFreqLimitedMemoryCache(20 * 1024 * 1024)) // 2 Mb
                .imageDownloader(new BaseImageDownloader(this, 5000, 30000)) // connectTimeout (5 s), readTimeout (30 s)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();
        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP
        // Инициализация ImageLoader.
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(Main2Activity.this));
        // Переменная класса отображающего карту без проблем со скроллингом и нажатиями.
        mapFragment = (NiceSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
        // Регистрация кнопки location.
        home = (ImageButton) findViewById(R.id.imageButton);
        home.setVisibility(View.GONE);
    }


    // Метод создания и обновления RecycleView.
    public void CreateRecycleView() {
        // Toolbar Title.
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "georgiai.ttf");
        title.setTypeface(typeFace);
        if (country.equals("mustsee")) {
            title.setText("Must see");
        } else {
            title.setText(country); }
        // Проверка на наличие в БД, для отображения звездочек.
        ArrayList<String> thereis = new ArrayList();
        for (int x=0; x<=Countries.size()-1; x++) {
         if  (DB.ifthereissight(Countries.get(x), Places.get(x)) == 1) {
             thereis.add("saved");
         } else thereis.add("");
        }
        // Подготовка RecycleView.
        rv.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        // Инициализируем заполнение RecycleView.
        adapter = new RVAdapter(Countries, Places, Photos, thereis);
        rv.setAdapter(adapter);
        // Устанавливаем список на нужную позицию.
        rv.scrollToPosition(count-2);
        // Анимация кнопок карта-лист.
        CountDownTimer timer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                group.setVisibility(View.VISIBLE);
                Animation shake = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.shakebutton);
                group.setAnimation(shake);
                group.startAnimation(shake);
            }
        }.start();

        // Кнопки-переключатели карта-список.
        but1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapcont.setVisibility(View.GONE);
                home.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                // Возвращаем настройки меню - разрешаем скрытие toolbar.
                invalidateOptionsMenu();
                // Сдвигаем список на начало ближайшего элемента.
                LinearLayoutManager layoutManager = ((LinearLayoutManager)rv.getLayoutManager());
                rv.scrollToPosition(layoutManager.findFirstVisibleItemPosition());
                rv.setScrollbarFadingEnabled(true);
            }
        });

        but2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rv.setVisibility(View.GONE);
                mapcont.setVisibility(View.VISIBLE);
                home.setVisibility(View.VISIBLE);
                // Запрещаем скрытие toolbar.
                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                // Загрузка маркеров (только один раз).
                if (countmap == 0) {
                    setmarkers();
                    countmap++;
                } else {}
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                maphome();
            }
        });
        // Если список пуст, то скрываем RecycleView и поазываем TextView.
        if (Countries.isEmpty() & Places.isEmpty() & Photos.isEmpty()) {
            EmptyText.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            group.setVisibility(View.GONE);
            but1.setVisibility(View.GONE);
            but2.setVisibility(View.GONE);
            mapcont.setVisibility(View.GONE);
            home.setVisibility(View.GONE);
        }
    }

    // Первое создание меню toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        menu.findItem(R.id.action_back).setVisible(true);
        // Разрешаем скрытие ToolBar.
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL|AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
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
        } else {
            super.onBackPressed();
            finish();
            final Intent myint = new Intent(getBaseContext(), MainActivity.class);
            myint.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(myint);
            System.exit(0);
        }
    }

    // Завершение Activity с очисткой памяти (перезапись метода).
    public void end(String key, String Country, String Place) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();

        if (key.equals("forward")) {
            finish();
            final Intent myint3 = new Intent(Main2Activity.this, Main3Activity.class);
            myint3.putExtra("Country", Country);
            myint3.putExtra("Place", Place);
            myint3.putExtra("Count", count);
            if (country.equals("mustsee")) {
                myint3.putExtra("Key", "mustsee");
            } else {
                myint3.putExtra("Key", "no");
            }
            myint3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(myint3);
        }
        System.exit(0);
    }

    // Метод интерфейса для завершения Activity.
    @Override
    public void FinishActivity() {
        finish();
    }

    // Метод подготовки и отображения карты.
    @Override
    public void onMapReady(final GoogleMap map) {
        mymap = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Чистим мусор и карту при каждой первой загрузке.
        map.clear();
        System.gc();

        // Слушатель нажатий на маркер.
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
        marker.hideInfoWindow();
        end("forward", marker.getSnippet(), marker.getTitle());
        return false;
              }
        });
    }

    // Метод используется для нормальной загрузки маркеров на карту.
    @Override
    public void onLowMemory () {
        super.onLowMemory();
        System.gc();
    }

    // Установка и отображение маркеров.
    public void setmarkers () {
        final View markerLayout = getLayoutInflater().inflate(R.layout.marker_layout, null);
        final ImageView markerImage = (ImageView) markerLayout.findViewById(R.id.marker_image);
        final TextView markerText = (TextView) markerLayout.findViewById(R.id.marker_text);
        File cachedImage;
        ActivityManager.MemoryInfo memoryInfo;

        // Создаем маркеры. Многоступенчатая проверка: ловим исключения, проверяем доступную память, ловим ошибку OOM, постоянно чистим мусор.
        for (int x=0; x!=Latitudes.size(); x++) {
          try{
            memoryInfo = getAvailableMemory();
            if (!memoryInfo.lowMemory) {
            // Проверка существует ли в кэше изображение.
            cachedImage = imageLoader.getInstance().getDiskCache().get(Photos.get(x));
            if (cachedImage != null && cachedImage.exists()) {

                Handler mainHandler = new Handler (getBaseContext().getMainLooper());
                final File finalCachedImage = cachedImage;
                final int y = x;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LatLng PERTH = new LatLng(Latitudes.get(y), Longitudes.get(y));
                            // Создаем маркер.
                            marker = new MarkerOptions().position(PERTH).title(Places.get(y)).snippet(Countries.get(y));
                            String filePath = finalCachedImage.getPath();
                            // Берем изображение из кэша.
                            mybitmap = BitmapFactory.decodeFile(filePath);
                            // Уменьшаем размер изображения.
                            mybitmap = Bitmap.createScaledBitmap(mybitmap, 140, 105, true);
                            // Загружаем текст и изображение в View.
                            markerImage.setImageBitmap(mybitmap);
                            markerText.setText(Places.get(y));
                            // Определяем размер Layout.
                            markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                            markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());
                            // Создаем Bitmap.
                            mybitmap2 = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(mybitmap2);
                            markerLayout.draw(canvas);
                            marker.icon(BitmapDescriptorFactory.fromBitmap(mybitmap2));
                            // Добавляем маркер.
                            mymap.addMarker(marker);
                            // Чистим bitmap.
                            if (mybitmap != null) {
                                mybitmap.recycle();
                            }
                            if (mybitmap2 != null) {
                                mybitmap2.recycle();
                            }
                            // Чистим мусор.
                            System.gc();
                        } catch (Exception e) {
                            showSnack("There are some problems with markers's loading. Not all of them were loaded. Please, try later.");
                            System.gc();
                        }
                    }
                };
                mainHandler.post(runnable);

            } else { // Если в кэше изображения нет, то загружаем его.
                try {
                    final int y = x;
                    // Загрузка изображения.
                    imageLoader.loadImage(Photos.get(x), new SimpleImageLoadingListener() {
                        // Слушатель окончания загрузки изображения.
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            LatLng PERTH = new LatLng(Latitudes.get(y), Longitudes.get(y));
                            // Создаем маркер.
                            MarkerOptions marker = new MarkerOptions().position(PERTH).title(Places.get(y)).snippet(Countries.get(y));
                            // Уменьшаем размер изображения.
                            mybitmap = Bitmap.createScaledBitmap(loadedImage, 140, 105, true);
                            // Загружаем текст и изображение в View.
                            markerImage.setImageBitmap(mybitmap);
                            markerText.setText(Places.get(y));
                            // Определяем размер Layout.
                            markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                            markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());
                            // Создаем Bitmap.
                            mybitmap2 = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(mybitmap2);
                            markerLayout.draw(canvas);
                            marker.icon(BitmapDescriptorFactory.fromBitmap(mybitmap2));
                            // Добавляем маркер.
                            mymap.addMarker(marker);
                            // Чистим bitmap.
                            if (mybitmap != null) {
                                mybitmap.recycle();
                            }
                            if (mybitmap2 != null) {
                                mybitmap2.recycle();
                            }
                            // Чистим мусор.
                            System.gc();
                        }
                    });
                } catch (Exception e) {
                    showSnack("There are some problems with markers's loading. Not all of them were loaded. Please, try later.");
                    System.gc();
                }
            }
                } else {
                    showSnack("There are some problems with markers's loading. Not all of them were loaded. Please, try later.");
                    System.gc();
                }
        } catch(OutOfMemoryError e) {
                // Прерываем цикл. Чистим мусор.
                System.gc();
                break;
            } finally {
                showSnack("There are some problems with markers's loading. Please, try later.");
                System.gc();
            }
        }
        maphome();
    }

    // Проверка на наличие достаточного количества памяти.
    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    // Центрирование карты.
    public void maphome () {
        // Добавляем все маркеры в границы изображения.
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int x=0; x!=Latitudes.size(); x++) {
            LatLng PERTH = new LatLng(Latitudes.get(x), Longitudes.get(x));
            builder.include(PERTH);
        }
        // Двигаем карту на нужную позицию с учетом добавленных маркеров.
        LatLngBounds bounds = builder.build();
        int padding = 80; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mymap.moveCamera(cu);

    }


    // Класс параллельного потока (загрузка достопримечательностей).
    class GetSights extends AsyncTask<String, String, String> {

        // Метод запускается до старта параллельного потока.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Запуск диалога.
           showprogress();
        }

        // Основной метод параллельного потока.
        protected String doInBackground(String... params) {

            int success;
            try {
                // Получение JSON.
                JSONObject json = null;
                if (country.equals("mustsee")) {
                json = jsonParser.makeHttpRequest(url_sight2, "GET", "");
                } else {
                // Задаем параметры для запроса.
                String param = "Country="+country;
                json = jsonParser.makeHttpRequest(url_sight1, "GET", param);}
                // Проверка на успешность запроса (возвращается из REST API).
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // Получение массивов с данными.
                    JSONArray productObj = json.getJSONArray(TAG_COUNTRIES); // JSON Array
                    // Сортировка массива по тегу (см. метод ниже).
                    JSONArray sights = sortArray(productObj, TAG_PLACE);
                    for (int x=0; x!=productObj.length(); x++) {
                        JSONObject country = sights.getJSONObject(x);
                        Countries.add(country.getString(TAG_COUNTRY));
                        Places.add(country.getString(TAG_PLACE));
                        Photos.add(country.getString(TAG_PHOTO));
                        Latitudes.add(Double.valueOf(country.getString(TAG_LATITUDE)));
                        Longitudes.add(Double.valueOf(country.getString(TAG_LONGITUDE)));
                    }
                }else{
                    Countries.clear();
                    Places.clear();
                    Photos.clear();
                    Latitudes.clear();
                    Longitudes.clear();
                    // Достопримечательности не найдены. Запуск метода завершения потока.
                    cancel(true);
                    if (isCancelled()) return null;
                }
            } catch (Exception e) {
                Countries.clear();
                Places.clear();
                Photos.clear();
                Latitudes.clear();
                Longitudes.clear();
                cancel(true);
                if (isCancelled()) return null;
            }
            return null;
        }

        // Метод выполняется после выполнения параллельного потока.
        protected void onPostExecute(String result) {
            // Вызываем метод создания и заполнения RecycleView.
            CreateRecycleView();
            // Заполняем карту данными.
            mapFragment.getMapAsync(Main2Activity.this);
            // Прячем диалог загрузки и завершаем Activity.
            closeprogress();
        }

        // Метод выполняется если произошла отмена выполнения основного метода.
        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Вызываем метод создания и заполнения RecycleView.
            CreateRecycleView();
            // Уведомление о невозможности сохранить информацию.
            showToast("It's impossible to load the sights. Please check your connection or try later.");
            // Прячем диалог загрузки и завершаем Activity.
            closeprogress();
        }
    }


    // Метод сортировки JSON-массива (по алфавиту). Принимаем массив и параметр сортировки. Возвращаем отсортированный массив.
    public static JSONArray sortArray(JSONArray jsonArr, final String sortBy){
        JSONArray sortedJsonArray = new JSONArray();
        try
        { List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonValues.add(jsonArr.getJSONObject(i)); }
            Collections.sort( jsonValues, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();
                    try {
                        valA = (String) a.get(sortBy);
                        valB = (String) b.get(sortBy);
                    }
                    catch (JSONException e) {
                    }
                    return valA.compareTo(valB);
                    //if you want to change the sort order, simply use the following: return -valA.compareTo(valB);
                }
            });
            for (int i = 0; i < jsonArr.length(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } return sortedJsonArray;
    }

    // Класс определения элементов CardView для построения RecyclerView.
    public class CountriesViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        TextView label2;
        ImageView icon;
        ImageView icon2;
        LinearLayout cardlayout;
        // Конструктор.
        CountriesViewHolder(final View itemView) {
            super(itemView);
            // Layout для слушателя нажатий.
            cardlayout = (LinearLayout) itemView.findViewById(R.id.cardlayout5);
            label = (TextView)itemView.findViewById(R.id.label);
            label2 = (TextView)itemView.findViewById(R.id.label2);
            icon = (ImageView)itemView.findViewById(R.id.icon);
            icon2 = (ImageView)itemView.findViewById(R.id.icon2);
            // Слушатель нажатий на элементы RecycleView.
            cardlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Place = label.getText().toString();
                    String Country = label2.getText().toString();
                    // Получаем номер нажатого элемента и передаем его в Intent.
                    count = rv.getChildAdapterPosition(itemView);
                    if (isOnline()) {

                    end("forward", Country, Place);
                    } else {
                        showSnack("There is no internet connection.");
                    }
                }
            });
        }
    }

    // Класс построения RecyclerView.
    public class RVAdapter extends RecyclerView.Adapter<CountriesViewHolder>{

        private List<String> Countries, Places, Photos, thereis;

        // Конструктор.
        RVAdapter(List<String> Countries, List <String> Places, List<String> Photos, List<String> thereis){
            this.Countries = Countries;
            this.Places = Places;
            this.Photos = Photos;
            this.thereis = thereis;
        }

        @Override
        public int getItemCount() {
            return Countries.size();
        }

        @Override
        public CountriesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.places_card_view, viewGroup, false);
            CountriesViewHolder pvh = new CountriesViewHolder(v);
            return pvh;
        }

        // Передача данных в элементы CardView, установка слушателя нажатий.
        @Override
        public void onBindViewHolder(final CountriesViewHolder personViewHolder, int i) {
            // Заполнение элементов RecycleView данными.
            personViewHolder.label.setText(Places.get(i));
            personViewHolder.label2.setText(Countries.get(i));
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "georgiai.ttf");
            personViewHolder.label.setTypeface(typeFace);
            personViewHolder.label2.setTypeface(typeFace);
            // Загрузка картинок при помощи библиотеки Universal Image Loader.
            imageLoader.displayImage(Photos.get(i), personViewHolder.icon, options);
            // Проверка на наличие страны и места в БД. Если есть ставим картинку - звездочка.
            if (thereis.get(i).equals("saved")) {personViewHolder.icon2.setImageResource(R.drawable.mystar);}
            else {personViewHolder.icon2.setImageBitmap(null);}

        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }
}