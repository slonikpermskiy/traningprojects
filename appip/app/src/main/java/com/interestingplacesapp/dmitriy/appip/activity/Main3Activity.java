package com.interestingplacesapp.dmitriy.appip.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.PlusShare;
import com.interestingplacesapp.dmitriy.appip.R;
import com.interestingplacesapp.dmitriy.appip.helpers.DBHelper;
import com.interestingplacesapp.dmitriy.appip.helpers.JSONParser;
import com.interestingplacesapp.dmitriy.appip.helpers.NiceSupportMapFragment;
import com.interestingplacesapp.dmitriy.appip.helpers.TextViewLinkHandler;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import com.facebook.FacebookSdk;


public class Main3Activity extends BaseActivity
        implements GestureDetector.OnGestureListener, OnMapReadyCallback, TextToSpeech.OnInitListener, BaseResponse {

    // Переменные для получения Intent.
    String country = "Empty";
    String place = "Empty";
    String key = "Empty";
    String country_state, place_state;
    int count = 0;
    // Разные переменные.
    ImageView photo, photo2, icontitle;
    TextView textView, label, label1, label2, labeltitle, label0;
    double latitude, longitude;
    // Переменная карты (для сохранения скриншота).
    GoogleMap mymap;
    // Остальные переменные карты.
    NiceSupportMapFragment mapFragment;
    ImageButton home;
    // Переключатель фотографий.
    private ImageSwitcher mImageSwitcher;
    // Переменная распознавания жестов (для фото).
    private GestureDetector gestureDetector;
    // Объект класса JSONParser.
    JSONParser jsonParser = new JSONParser();
    // Путь к REST API.
    private static final String url_sight1 = "http://www.edu-sam.com/restapiinterestingplaces/api/get_sight.php";
    private static final String url_sight2 = "http://www.edu-sam.com/restapiinterestingplaces/api/randomplace.php";
    private static final String url_translate = "http://www.edu-sam.com/restapiinterestingplaces/api/translate.php";
    // Переменные JSON.
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SIGHT = "sight";
    private static final String TAG_COUNTRY = "Country";
    private static final String TAG_PLACE = "Place";
    private static final String TAG_Text = "Text";
    private static final String TAG_Howmuch = "Howmuch";
    private static final String TAG_Howtoget = "Howtoget";
    private static final String TAG_Photo = "Photo";
    private static final String TAG_Photo2 = "Photo2";
    private static final String TAG_LATITUDE = "Latitude";
    private static final String TAG_LONGITUDE = "Longitude";
    private static final String TAG_STATUS = "status";
    private static final String TAG_RESULT = "results";
    private static final String TAG_ADDRESS = "formatted_address";

    // Переменные для распознавания жеста "свайп" (для смены фотографии).
    private static final int SWIPE_MIN_DISTANCE = 40;
    private static final int SWIPE_MAX_OFF_PATH = 450;
    private static final int SWIPE_THRESHOLD_VELOCITY = 500;

    // Переменные Universal Image Loader.
    DisplayImageOptions options;
    ImageLoaderConfiguration config;
    // Сворачивающийся ToolBar.
    private CollapsingToolbarLayout collapsingToolbar;
    // Переменная Jsoup для скачивания html.
    org.jsoup.nodes.Document doc;
    // Плавающая кнопка.
    FloatingActionButton fab, fab1;
    // Массивы  и переменные для данных по достопримечательности.
    ArrayList<String> Sight = new ArrayList();
    ArrayList<String> Photos = new ArrayList();
    String htmltext;
    String address = "There is no address.";
    String translated, translated1, translated2;
    // Переменная БД.
    DBHelper DB;
    // Переменные Facebook.
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    // Переменные TTS.
    TextToSpeech mTTS;
    String langset = "en";
    // Переменые хранения счетчиков.
    SharedPreferences countsave;
    final String TRANSLATE = "translate";
    final String TRANSLATECOUNT = "translatecount";
    final String SPEECH = "speech";
    final String SPEECHCOUNT = "speechcount";
    final String ADDCOUNT = "addcount";
    // Полноэкранная реклама.
    InterstitialAd mInterstitialAd;
    // Счетчик (для рекламы).
    CountDownTimer timer;
    // Переменная загрузчика фотографий.
    ImageLoader imageLoader;
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
        View activityView = layoutInflater.inflate(R.layout.activity_main3, null,false);
        frameLayout.addView(activityView);
        // Назначаем объекту интерфейса в суперклассе значение нашей Activity.
        response = Main3Activity.this;
        // Получение Intent.
        Intent mintent = getIntent();
        country = mintent.getStringExtra("Country");
        place = mintent.getStringExtra("Place");
        key = mintent.getStringExtra("Key");
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
        // Запуск параллельного потока для загрузки достопримечательности.
        mAsyncTask = new GetSight().execute();
        if (!purchases.getString(ADPURCHASE, "").equals("yes")) {
        // Полноэкранная реклама.
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7438405886273904/6614307473");
        requestNewInterstitial();}
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
                    .memoryCacheExtraOptions(640, 480) // width, height
                    .threadPoolSize(5)
                    .threadPriority(Thread.MIN_PRIORITY + 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .memoryCache(new UsingFreqLimitedMemoryCache(20 * 1024 * 1024)) // 20 Mb
                    .imageDownloader(new BaseImageDownloader(this, 5000, 30000)) // connectTimeout (5 s), readTimeout (30 s)
                    .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                    .build();
            ImageLoader.getInstance().init(config);
            // END - UNIVERSAL IMAGE LOADER SETUP

            // Переменная класса отображающего карту без проблем со скроллингом и нажатиями.
            mapFragment = (NiceSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            // Регистрация кнопки location.
            home = (ImageButton) findViewById(R.id.imageButton);
            // Переменные звездочки и кнопки сохранения.
            icontitle = (ImageView) findViewById(R.id.icontitle);
            fab = (FloatingActionButton) findViewById(R.id.fab);
            // Скрываем Float Button.
            fab.setVisibility(View.GONE);
            // Кнопка стоп.
            fab1 = (FloatingActionButton) findViewById(R.id.fabstop);
            fab1.setVisibility(View.GONE);
            // Остальные View.
            textView = (TextView) findViewById(R.id.textView);
            mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
            mImageSwitcher.setVisibility(View.GONE);
            photo = (ImageView) findViewById(R.id.photo);
            photo2 = (ImageView) findViewById(R.id.photo2);
            label0 = (TextView) findViewById(R.id.label0);
            label0.setMaxLines(maxlines);
            labeltitle = (TextView) findViewById(R.id.labeltitle);
            label = (TextView) findViewById(R.id.label);
            label1 = (TextView) findViewById(R.id.label1);
            label2 = (TextView) findViewById(R.id.label2);
            expcol = (ImageButton) findViewById(R.id.expcol);
            // For appropriate work CollapsingToolbar, cause it don't collapsing in normal way without it.
            ViewGroup mContentContainer = (ViewGroup) findViewById(R.id.content_container);
            mContentContainer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                }
            });

            // Переменные и диалог Facebook.
            FacebookSdk.sdkInitialize(getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            shareDialog = new ShareDialog(this);
            // Text to speech.
            mTTS = new TextToSpeech(Main3Activity.this, Main3Activity.this);
            // Инициализация места хранения счетчиков перевода и прослушивания.
            countsave = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (!purchases.getString(ADPURCHASE, "").equals("yes")) {
            // Инкримент счетчика рекламы.
            int countadd1 = countsave.getInt(ADDCOUNT, 0);
            countadd1++;
            countsave.edit().putInt(ADDCOUNT, countadd1).commit();
            }
        }

    // OnStop.
    @Override
    protected void onStop() {
        super.onStop();
        // Останавливаем уже загруженные в QUEUE_ADD тексты.
        if (Build.VERSION.SDK_INT >= 21) {
            mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
        }
        fab1.setVisibility(View.GONE);
    }

    // OnDestroy.
    @Override
    public void onDestroy() {
        if (mTTS != null) {
        mTTS.stop();
        mTTS.shutdown();
        }
        super.onDestroy();
    }


    // Метод заполнения данных по достопримечательности.
    public void CreateSight() {
        // Узнаем сохранена ли достопримечательность.
        int thereis = DB.ifthereissight(Sight.get(0), Sight.get(1));
        // Если достопримечательности нет, то отображаем кнопку, срываем звездочку, если есть, то наоборот.
        if (thereis == 0) {
            // Скрываем звездочку, если достопримечательность не сохранена.
            icontitle.setImageBitmap(null);
            // Показываем Floating Button.
            fab.setVisibility(View.VISIBLE);
            // Регистрация кнопки Add и назначение слушателя.
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ArrayList<String> Places = new ArrayList();
                    Places = DB.dataforplaces();
                    if (!purchases.getString(LIMITSPURCHASE, "").equals("yes")) {
                        if (Sight.get(0).equals("Empty") && Sight.get(1).equals("Empty")) {
                            showSnack("There is no data for saving. Please check your connection.");
                        } else if (Places.size() >= 5) {
                            // Запуск диалога сохранения достопримечательности.
                            savesight("Saving the sight", "You can't save more than 5 sights for free. Do you want to remove limitations?", "limitsave");
                        } else {
                            if (isOnline()) {
                                // Запуск диалога сохранения достопримечательности.
                                savesight("Saving the sight", "Do you want to save this sight and read it offline?", "save");
                            } else {
                                showSnack("There is no internet connection.");
                            }
                        }
                    } else {
                        if (isOnline()) {
                            // Запуск диалога сохранения достопримечательности.
                            savesight("Saving the sight", "Do you want to save this sight and read it offline ?", "save");
                        } else {
                            showSnack("There is no internet connection.");
                        }
                    }
                }
            });
        } else if (thereis == 1) {
            // Скрываем Float Button, если достопримечательность сохранена.
            fab.setVisibility(View.GONE);
            // Отображаем звездочку.
            icontitle.setImageResource(R.drawable.mystar);
        }

        // Загрузка картинок при помощи библиотеки Universal Image Loader.
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        // Скачиваем фото и отображаем.
        imageLoader.displayImage(Photos.get(0), photo, options);
        imageLoader.displayImage(Photos.get(1), photo2, options);
        mImageSwitcher.setVisibility(View.VISIBLE);
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
        labeltitle.setText(Sight.get(0) + ", " + Sight.get(1));
        labeltitle.setTypeface(typeFace);
        label.setText(Sight.get(4));
        label.setTypeface(typeFace);
        label1.setText(Sight.get(3));
        label1.setTypeface(typeFace);
        if (!Sight.get(5).equals("Empty")) {
        latitude = Double.valueOf(Sight.get(5));}
        else {latitude = 0;}
        if (!Sight.get(6).equals("Empty")) {
        longitude = Double.valueOf(Sight.get(6));}
        else {latitude = 0;}
        label2.setText(address);
        label2.setTypeface(typeFace);
        mapFragment.getMapAsync(this);
        // Используется для отображения текста без сдвига  после перехода по ссылке и возвращения.
        LinearLayout focus = (LinearLayout) findViewById(R.id.content_container1);
        focus.getParent().requestChildFocus(focus,focus);
        if (!purchases.getString(ADPURCHASE, "").equals("yes")) {
            // Если счетчик больше или равен 3, то показываем рекламу и сбрасываем счетчик.
            int countadd = countsave.getInt(ADDCOUNT, 0);
            if (countadd >= 3) {
                // Создание таймера для показа рекламы (5 сек., шаг 1 сек.).
                timer = new CountDownTimer(9000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        // Запуск рекламы.
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }
                        countsave.edit().putInt(ADDCOUNT, 0).commit();
                    }
                }.start();
            }
        }
        // Слушатель кнопки Location.
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapFragment.getMapAsync(Main3Activity.this);
            }
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
        // Слушатель кнопки стоп.
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Останавливаем уже загруженные в QUEUE_ADD тексты.
                if (Build.VERSION.SDK_INT >= 21) {
                    mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
                }
                fab1.setVisibility(View.GONE);
            }
        });
    }


    // Метод подготовки и отображения карты.
    @Override
    public void onMapReady(final GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Переменная для сохранения.
        mymap = map;
        if (latitude==0 && longitude==0) {

            CameraPosition googlePlex = CameraPosition.builder()
                    .target(new LatLng(58.0013, 56.2282))
                    .zoom(0)
                    .bearing(0)
                    .tilt(0)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(googlePlex));
            map.clear();
        } else {
            CameraPosition googlePlex = CameraPosition.builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(10)
                    .bearing(0)
                    .tilt(0)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(googlePlex));

            map.clear();
            LatLng PERTH = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions()
                    .position(PERTH));
        }
    }

    // Метод сохранения скриншота карты.
    public String CaptureMapScreen() {

        SnapshotReadyCallback callback = new SnapshotReadyCallback() {

            String FileMapName = "/" + Sight.get(0) + "_" + Sight.get(1);
            Bitmap bitmap = null;
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                bitmap = snapshot;
                try {
                    FileOutputStream out = new FileOutputStream(String.valueOf(getExternalFilesDir(null)) + FileMapName + ".png");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    bitmap.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        mymap.snapshot(callback);
        String filenamereturn = String.valueOf(getExternalFilesDir(null)) + "/" + Sight.get(0) + "_" + Sight.get(1) + ".png";
        return filenamereturn;
    }

    // Диалог сохранения достопримечательности.
    private void savesight(String titledial, String textdial, final String key) {

        // Получаем Layout формы ввода (write_form.xml).
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.inform_dialog, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Устанавливаем write_form.xml в строитель диалога.
        alertDialogBuilder.setView(promptsView);
        final TextView title = (TextView) promptsView
                .findViewById(R.id.alerttitle);
        title.setText(titledial);
        alertDialogBuilder.setView(promptsView);
        final TextView title1 = (TextView) promptsView
                .findViewById(R.id.title1);
        title1.setText(textdial);

        if (key.equals("save")) {
            // Устанавливаем позитивную кнопку.
            alertDialogBuilder.setPositiveButton("Save", null);
        } else  {
            alertDialogBuilder.setPositiveButton("Remove", null);
        }
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // Создаем диалог.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Отображаем диалог.
        alertDialog.show();
        // Устанавливаем цвет текста кнопок.
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_button_text));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_button_text));

        // Слушатель позитивной кнопки.
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isOnline()) {
                if (key.equals("save")) {
                    // Запуск параллельного потока для сохранения достопримечательности.
                    mAsyncTask = new SaveSight().execute();
                } else  {
                    if (purchases.getString(LIMITSPURCHASE, "").equals("yes")) {
                        showSnack("You have already removed limitations.");
                    } else {
                        mHelper.launchPurchaseFlow(Main3Activity.this, SKU_LIMITS, 1, mPurchaseFinishedListener, null);
                        //purchases.edit().putString(LIMITSPURCHASE, "yes").commit();
                    }
                }
                } else {
                    showSnack("There is no internet connection.");
                }
                alertDialog.dismiss();
            }
        });
    }


    // Первое создание меню toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu2, menu);
        menu.findItem(R.id.action_back).setVisible(true);
        return true;
    }


    // Слушатель нажатия кнопок toolbar.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            onBackPressed();
        } else if (id == R.id.action_share) {
            if (Sight.get(0).equals("Empty") && Sight.get(1).equals("Empty")) {
                showSnack("There is no data for sharing. Please check your connection.");
            } else {
                if (isOnline()) {
                    shareToFacebook();
                } else {
                    showSnack("There is no internet connection.");
                }
            }
        } else if (id == R.id.action_share1) {
            if (Sight.get(0).equals("Empty") && Sight.get(1).equals("Empty")) {
                showSnack("There is no data for sharing. Please check your connection.");
            } else {
                if (isOnline()) {
                    shareToGoogle();
                } else {
                    showSnack("There is no internet connection.");
                }
            }
        } else if (id == R.id.translate) {
            long translatecount1 = countsave.getLong(TRANSLATECOUNT, 0);
            long currenttime = Long.valueOf(new SimpleDateFormat("yyyyMMddHHmm").format(Calendar.getInstance().getTime()));
            int count2 = countsave.getInt(TRANSLATE, 0);
            if (!purchases.getString(LIMITSPURCHASE, "").equals("yes")) {
                if (currenttime - 100 >= translatecount1) {
                    countsave.edit().putLong(TRANSLATECOUNT, Long.valueOf(currenttime)).commit();
                    countsave.edit().putInt(TRANSLATE, 0).commit();
                    if (isOnline()) {
                        chooselanguagedialog();
                    } else {
                        showSnack("There is no internet connection.");
                    }
                } else if (currenttime - 100 < translatecount1 && count2 >= 2) {
                    savesight("Translating", "You can't translate more than 2 texts in one hour for free. Do you want to remove limitations?", "limittrans");
                } else if (currenttime - 100 < translatecount1 && count2 < 2) {
                    if (isOnline()) {
                        chooselanguagedialog();
                    } else {
                        showSnack("There is no internet connection.");
                    }
                }
            } else {
                if (isOnline()) {
                    chooselanguagedialog();
                } else {
                    showSnack("There is no internet connection.");
                }
            }
        } else if (id == R.id.speech) {
            long speechcount1 = countsave.getLong(SPEECHCOUNT, 0);
            long currenttime1 = Long.valueOf(new SimpleDateFormat("yyyyMMddHHmm").format(Calendar.getInstance().getTime()));
            int count3 = countsave.getInt(SPEECH, 0);
            if (!purchases.getString(LIMITSPURCHASE, "").equals("yes")) {
            if (currenttime1 - 100 >= speechcount1) {
                countsave.edit().putLong(SPEECHCOUNT, Long.valueOf(currenttime1)).commit();
                countsave.edit().putInt(SPEECH, 0).commit();
                TextToSpeechFunction();
            } else if (currenttime1 - 100 < speechcount1 && count3 >= 2) {
                savesight("Listening", "You can't listen to speech more than 2 times in one hour for free. Do you want to remove limitations?", "limitspeech");
            } else if (currenttime1 - 100 < speechcount1 && count3 < 2) {
                TextToSpeechFunction();
            }
            } else {
                TextToSpeechFunction();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    // Метод загрузки полноэкранной рекламы.
    public void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mInterstitialAd.loadAd(adRequest);
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
            if (isOnline()) {
             if (key.equals("mustsee"))   {
                 finish();
                 final Intent myintent = new Intent(Main3Activity.this, Main2Activity.class);
                 myintent.putExtra("Country", "mustsee");
                 myintent.putExtra("Count", count);
                 myintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 startActivity(myintent);
             } else {
                 finish();
                final Intent myintent = new Intent(Main3Activity.this, Main2Activity.class);
                myintent.putExtra("Country", Sight.get(0));
                myintent.putExtra("Count", count);
                startActivity(myintent); }
            } else {
                finish();
                showToast("There is no internet connection.");
                final Intent intentmain = new Intent(getBaseContext(), MainActivity.class);
                intentmain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentmain);
            }
            System.exit(0);
        }
    }


    // Обязательный метод проигрывания текста.
    @Override
    public void onInit(int Text2SpeechCurrentStatus) {

        if (Text2SpeechCurrentStatus == TextToSpeech.SUCCESS) {
            // Устанавливаем язык, скорость речи и пауз.
            mTTS.setLanguage(Locale.ENGLISH);
            mTTS.setSpeechRate((float) 0.8);
            mTTS.setPitch((float) 0.8);
        }
    }

    // Проигрывание текста.
    public void TextToSpeechFunction()
    {
        // Останавливаем уже загруженные в QUEUE_ADD тексты.
        if (Build.VERSION.SDK_INT >= 21) {
            mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
        }
        // Устанавливаем необходимый язык.
        int res = 0;
        res = mTTS.setLanguage(Locale.ENGLISH);
        if (langset.equals("ru")) {
            Locale locale = new Locale("ru");
            res = mTTS.setLanguage(locale);
        } else if (langset.equals("en")) {
           res =  mTTS.setLanguage(Locale.ENGLISH);
        } else if (langset.equals("fr")) {
            res =  mTTS.setLanguage(Locale.FRENCH);
        } else if (langset.equals("de")) {
            res =  mTTS.setLanguage(Locale.GERMAN);
        } else if (langset.equals("es")) {
            Locale locale = new Locale("es");
            res = mTTS.setLanguage(locale);
        } else if (langset.equals("pt")) {
            Locale locale = new Locale("pt");
            res = mTTS.setLanguage(locale);
        } else if (langset.equals("ja")) {
            res =  mTTS.setLanguage(Locale.JAPANESE);
        } else if (langset.equals("zh")) {
            res =  mTTS.setLanguage(Locale.CHINESE);
        }

        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            showSnack("Sorry, this language is not supported.");
        } else {
            showSnack("Speech is loading ... You have to have Google TTS App installed.");
            String textholder = label0.getText().toString();
            //textToSpeech can only cope with Strings with < 4000 characters
            int dividerLimit = 3900;
            if(textholder.length() >= dividerLimit) {
                int textLength = textholder.length();
                ArrayList<String> texts = new ArrayList<String>();
                int count = textLength / dividerLimit + ((textLength % dividerLimit == 0) ? 0 : 1);
                int start = 0;
                int end = textholder.indexOf(" ", dividerLimit);
                for(int i = 1; i<=count; i++) {
                    texts.add(textholder.substring(start, end));
                    start = end;
                    if((start + dividerLimit) < textLength) {
                        end = textholder.indexOf(" ", start + dividerLimit);
                    } else {
                        end = textLength;
                    }
                }
                for(int i=0; i<texts.size(); i++) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        mTTS.speak(texts.get(i), TextToSpeech.QUEUE_ADD, null, null);
                    } else {
                        mTTS.speak(texts.get(i), TextToSpeech.QUEUE_ADD, null);
                    }
                }
                // Отображаем кнопку стоп.
                fab1.setVisibility(View.VISIBLE);
                // Прибавляем 1 к счетчику.
                int count2 = countsave.getInt(SPEECH, 0);
                count2++;
                countsave.edit().putInt(SPEECH, count2).commit();

            } else {
                if (Build.VERSION.SDK_INT >= 21) {
                    mTTS.speak(String.valueOf(textholder), TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    mTTS.speak(String.valueOf(textholder), TextToSpeech.QUEUE_FLUSH, null);
                }
                // Отображаем кнопку стоп.
                fab1.setVisibility(View.VISIBLE);
                // Прибавляем 1 к счетчику.
                int count2 = countsave.getInt(SPEECH, 0);
                count2++;
                countsave.edit().putInt(SPEECH, count2).commit();
            }
        }
    }

    // Метод интерфейса для завершения Activity.
    @Override
    public void FinishActivity() {
        finish();
    }


    // Класс параллельного потока (загрузка достопримечательности).
    class GetSight extends AsyncTask<String, String, String> {

        // Метод запускается до старта параллельного потока.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Запуск диалога загрузки.
            showprogress();
        }

        // Основной метод параллельного потока.
        protected String doInBackground(String... params) {

            int success;
            try {
                // Задаем параметры для запроса.
                JSONObject json = null;
            if (key.equals("random")) {
                String param = "";
                // Получение JSON.
                json = jsonParser.makeHttpRequest(url_sight2, "GET", param);
            } else {
                String param = "Country=" + country + "&Place=" + place;
                // Получение JSON.
                json = jsonParser.makeHttpRequest(url_sight1, "GET", param);
            }
                // Проверка на успешность запроса (возвращается из REST API).
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // Получение массива с данными.
                    JSONArray productObj = json.getJSONArray(TAG_SIGHT); // JSON Array
                    JSONObject country = productObj.getJSONObject(0);

                    // Заполнение списка нашими данными.
                    Sight.add(country.getString(TAG_COUNTRY));
                    Sight.add(country.getString(TAG_PLACE));
                    Sight.add(country.getString(TAG_Text));
                    Sight.add(country.getString(TAG_Howmuch));
                    Sight.add(country.getString(TAG_Howtoget));
                    Sight.add(country.getString(TAG_LATITUDE));
                    Sight.add(country.getString(TAG_LONGITUDE));
                    Photos.add(country.getString(TAG_Photo));
                    Photos.add(country.getString(TAG_Photo2));
                    try{
                    // Сброс переменной, для дальнейшего использования в условии (см. ниже), в целях проверки корректности ссылки (см. ниже).
                    doc=null;
                    // Получение html-кода с помощью библиотеки Jsoup. Необходимо скачать ее и положить в папку: app/libs.
                    String link = null;
                    link = Sight.get(2);
                    doc = Jsoup.connect(link).maxBodySize(0)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                            .timeout(0)
                            .get();
                        // Запись html-кода в строку.
                        htmltext = doc.html();
                    } catch (Exception ex) {
                        doc=null;
                    }
                    // Задаем параметры для запроса.
                    String param1 = "latlng="+country.getString(TAG_LATITUDE)+","+country.getString(TAG_LONGITUDE)+"&key=AIzaSyD7TSB-6ZOvULdYiNjm85SfC8xZnOtpj20";
                    // Получение JSON.
                    JSONObject json2 = null;
                    json2 = jsonParser.makeHttpRequest("https://maps.googleapis.com/maps/api/geocode/json", "GET", param1);
                    String status = json2.getString(TAG_STATUS);
                    if (status.equals("OK")) {
                    JSONArray productObj2 = json2.getJSONArray(TAG_RESULT); // JSON Array
                    JSONObject result = productObj2.getJSONObject(0);
                    address = result.getString(TAG_ADDRESS);}
                    else {address = "There is no address.";}
                } else {
                    Sight.clear();
                    for (int x = 0; x <= 6; x++) {
                        Sight.add("Empty");
                    }
                    Photos.clear();
                    for (int x = 0; x <= 1; x++) {
                        Photos.add("Empty");}
                    address = "There is no address.";
                    cancel(true);
                    if (isCancelled()) return null;
                }
            } catch (Exception e) {
                Sight.clear();
                for (int x=0; x<=6; x++) {
                Sight.add("Empty");}
                Photos.clear();
                for (int x=0; x<=1; x++) {
                    Photos.add("Empty");}
                address = "There is no address.";
                cancel(true);
                if (isCancelled()) return null;
            }
            return null;
        }

        // Метод выполняется после выполнения параллельного потока.
        protected void onPostExecute(String result) {
            if (doc==null) {
                htmltext = "Can't get data. Try later.";}
            // Вызываем метод создания и заполнения RecycleView.
            CreateSight();
            // Прячем диалог загрузки.
           closeprogress();
        }

        // Метод выполняется если произошла отмена выполнения основного метода.
        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (doc==null) {
            htmltext = "Can't get data. Try later.";}
            // Уведомление о невозможности сохранить информацию.
            showToast("It's impossible to open the sight in a normal way. Please check your connection or try later.");
            // Вызываем метод создания и заполнения RecycleView.
            CreateSight();
            // Прячем диалог загрузки.
            closeprogress();
        }
    }

    // Класс параллельного потока (сохранение достопримечательности).
    class SaveSight extends AsyncTask<String, String, String> {

        ArrayList<String> Sight1 = new ArrayList();
        ArrayList<String> Photos1 = new ArrayList();
        String path = String.valueOf(getExternalFilesDir(null));

        // Метод запускается до старта параллельного потока.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Основной метод параллельного потока.
        protected String doInBackground(String... params) {

            try {
                // Сохранение фото.
                for (int x=0; x<Photos.size(); x++) {
                    int pos = Photos.get(x).lastIndexOf("/");
                    String FileName = Photos.get(x).substring(pos);
                    String imageUrl = Photos.get(x);
                    File fileForImage = new File(path + FileName);

                    InputStream sourceStream;
                    File cachedImage = imageLoader.getInstance().getDiskCache().get(imageUrl);
                    if (cachedImage != null && cachedImage.exists()) { // if image was cached by UIL
                        sourceStream = new FileInputStream(cachedImage);
                    } else { // otherwise - download image
                        ImageDownloader downloader = new BaseImageDownloader(Main3Activity.this);
                        sourceStream = downloader.getStream(imageUrl, null);
                    }
                    if (sourceStream != null) {
                        try {
                            OutputStream targetStream = new FileOutputStream(fileForImage);
                            try {
                                IoUtils.copyStream(sourceStream, targetStream, null);
                            } finally {
                                targetStream.close();
                            }
                        } finally {
                            sourceStream.close();
                        }
                    }
                    Photos1.add("file://" + path + FileName);
                }
                // Сохранение html в файл.
                int pos1 = Sight.get(2).lastIndexOf("/");
                String FileName1 = Sight.get(2).substring(pos1);
                File file = new File(path + FileName1);
                // Запись html-кода в файл. Метод doc.charset();  устанавливает шрифт как указано в meta-данных сайта.
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(path + FileName1), doc.charset()));
                if (translated != null) {
                    writer.print(translated);
                }else {
                writer.print(htmltext);}
                writer.close();
                // Скриншот карты.
                String mapcapture = CaptureMapScreen();
                // Подготовка данных для записи в БД.
                Sight1.add(Sight.get(0));
                Sight1.add(Sight.get(1));
                Sight1.add(path + FileName1);
                if (translated2 != null) {
                    Sight1.add(translated1);
                }else {
                    Sight1.add(Sight.get(3));}
                if (translated1 != null) {
                    Sight1.add(translated2);
                }else {
                    Sight1.add(Sight.get(4));}
                Sight1.add(Sight.get(5));
                Sight1.add(Sight.get(6));
                Sight1.add(address);
                Sight1.add(Photos1.get(0));
                Sight1.add(Photos1.get(1));
                Sight1.add("file://" + mapcapture);
                // Запись в БД.
                DB.savesight(Sight1);

        }catch(Exception e){
             cancel(true);
             if (isCancelled()) return null;
        }
             return null;
        }

            // Метод выполняется после выполнения параллельного потока.
        protected void onPostExecute(String result) {
            // Уведомление о сохранении.
            showSnack("The sight was successfully saved.");
            // Скрываем Float Button, если достопримечательность сохранена.
            fab.setVisibility(View.GONE);
            // Отображаем звездочку.
            icontitle.setImageResource(R.drawable.mystar);
        }

        // Метод выполняется если произошла отмена выполнения основного метода.
        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Уведомление о невозможности сохранить информацию.
            showToast("Data was not saved. Please check your connection, delete the previous data and try to save it again.");
        }
    }

    // Диалог перевода текста.
    private void chooselanguagedialog() {

        ArrayList<String> languages = new ArrayList();
        languages.add("English");
        languages.add("Russian");
        languages.add("French");
        languages.add("German");
        languages.add("Spanish");
        languages.add("Portuguese");
        languages.add("Japanese");
        languages.add("Chinese");
        // Получаем Layout формы ввода.
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.choose_country_form, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Устанавливаем форму в строитель диалога.
        alertDialogBuilder.setView(promptsView);

        final TextView title = (TextView) promptsView
                .findViewById(R.id.alerttitle);
        title.setText("Choose language");

        final ListView lvMain = (ListView) promptsView.findViewById(R.id.lvMain);
        // Настраиваем и вызываем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.mylist, languages);
        lvMain.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        // Устанавливаем цвет селектора.
        lvMain.setSelector(R.drawable.layout_toggle);
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // Создаем диалог.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Отображаем диалог.
        alertDialog.show();
        // Устанавливаем цвет текста кнопки.
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_button_text));
        // Слушатель нажатий на созданные элементы ListView.
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (isOnline()) {
                    // Получаем текст с нажатого элемента.
                    String Title = ((TextView) view).getText().toString();
                    if (Title.equals("English")) {
                        translated=null; translated1=null; translated2=null;
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
                        label.setText(Sight.get(4));
                        label1.setText(Sight.get(3));
                        langset = "en";
                        //mTTS.setLanguage(Locale.ENGLISH);
                    }
                    if (Title.equals("Russian")) {mAsyncTask = new Translate().execute("ru");}
                    if (Title.equals("French")) {mAsyncTask = new Translate().execute("fr");}
                    if (Title.equals("German")) {mAsyncTask = new Translate().execute("de");}
                    if (Title.equals("Spanish")) {mAsyncTask = new Translate().execute("es");}
                    if (Title.equals("Portuguese")) {mAsyncTask = new Translate().execute("pt");}
                    if (Title.equals("Japanese")) {mAsyncTask = new Translate().execute("ja");}
                    if (Title.equals("Chinese")) {mAsyncTask = new Translate().execute("zh");}
                    alertDialog.dismiss();
                } else {
                    showToast("There is no internet connection.");
                }
            }
        });
    }

    // Класс параллельного потока (перевод текста).
    class Translate extends AsyncTask<String, String, String> {

        int success;
        String language;
        String link;

        // Метод запускается до старта параллельного потока.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Запуск диалога.
            showprogress();
        }

        // Основной метод параллельного потока.
        protected String doInBackground(String... params) {

            try {
                language = params[0];
                // Задаем параметры для запроса.
                String param = "Country="+Sight.get(0)+"&Place="+Sight.get(1)+"&Language="+language;
                // Получение JSON.
                JSONObject json = null;
                json = jsonParser.makeHttpRequest(url_translate, "GET", param);

                // Проверка на успешность запроса (возвращается из REST API).
                success = json.getInt("success");
                if (success == 1) {
                    // Получение массива с данными.
                    JSONArray productObj = json.getJSONArray(TAG_SIGHT); // JSON Array
                    JSONObject country = productObj.getJSONObject(0);
                    // Заполнение списка нашими данными.
                    link = country.getString("Text");
                    translated1 = country.getString("Howmuch");
                    translated2 = country.getString("Howtoget");
                    try{
                        // Сброс переменной, для дальнейшего использования в условии (см. ниже), в целях проверки корректности ссылки (см. ниже).
                        doc=null;
                        // Получение html-кода с помощью библиотеки Jsoup. Необходимо скачать ее и положить в папку: app/libs.
                        doc = Jsoup.connect(link).maxBodySize(0)
                                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                                .timeout(0)
                                .get();
                        // Запись html-кода в строку.
                        translated = doc.html();
                    } catch (Exception ex) {
                        // Перевод не получен.
                        cancel(true);
                        if (isCancelled()) return null;
                    }
                } else {
                    // Перевод не получен.
                    cancel(true);
                    if (isCancelled()) return null;
                }
            } catch (Exception e) {
                cancel(true);
                if (isCancelled()) return null;
            }
            return null;
        }

        // Метод выполняется после выполнения параллельного потока.
        protected void onPostExecute(String result) {
            // Меняем текст в поле. Разный метод отображения html в TextView в зависимости от версии ОС.
            if (Build.VERSION.SDK_INT >= 24) {
                label0.setText(Html.fromHtml(translated, 0)); // for 24 api and more
            } else {
                label0.setText(Html.fromHtml(translated)); // or for older api
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
            label.setText(translated2);
            label1.setText(translated1);
            langset = language;
            // Прибавляем 1 к счетчику.
            int count1 = countsave.getInt(TRANSLATE, 0);
            count1++;
            countsave.edit().putInt(TRANSLATE, count1).commit();
            showSnack("The text was successfully translated");
            // Прячем диалог.
            closeprogress();
        }

        // Метод выполняется если произошла отмена выполнения основного метода.
        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Уведомление о невозможности загрузить страны.
            showSnack("It's impossible to translate the text. Please check your connection or try later.");
            // Прячем диалог загрузки и завершаем Activity.
            closeprogress();
        }
    }

    // Поделиться на Facebook.
    private void shareToFacebook(){
        String placenew = Sight.get(1).replaceAll(" ","%20");
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("http://interestingplaces.edu-sam.com/open_sight.php?Country="+Sight.get(0)+"&Place="+placenew))
                .build();
        shareDialog.show(Main3Activity.this, content);

    }

    // Поделиться на Google+.
    private void shareToGoogle(){

    String placenew = Sight.get(1).replaceAll(" ","%20");
    // Launch the Google+ share dialog with attribution to your app.
    Intent shareIntent = new PlusShare.Builder(this)
            .setType("text/plain")
            .setText("Interesting Places App")
            .setContentUrl(Uri.parse("http://interestingplaces.edu-sam.com/open_sight.php?Country="+Sight.get(0)+"&Place="+placenew))
            .getIntent();

    startActivityForResult(shareIntent, 0);
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