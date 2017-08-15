package com.interestingplacesapp.dmitriy.appip.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.interestingplacesapp.dmitriy.appip.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import com.interestingplacesapp.dmitriy.appip.helpers.JSONParser;
import com.interestingplacesapp.dmitriy.appip.util.IabHelper;
import com.interestingplacesapp.dmitriy.appip.util.IabResult;
import com.interestingplacesapp.dmitriy.appip.util.Inventory;
import com.interestingplacesapp.dmitriy.appip.util.Purchase;



public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {



    public AsyncTask mAsyncTask;
    // Объект класса JSONParser.
    JSONParser jsonParser = new JSONParser();
    // Путь к REST API.
    private static final String url_sight = "http://www.edu-sam.com/restapiinterestingplaces/api/get_countries.php";
    private static final String url_request = "http://www.edu-sam.com/restapiinterestingplaces/api/requestplace.php";
    // Переменные JSON.
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_COUNTRIES = "countries";
    private static final String TAG_COUNTRY = "Country";
    public DrawerLayout drawer;

    AlertDialog alertDialog;
    // Объект класса покупок.
    IabHelper mHelper;
    // Сохранение статуса покупок.
    SharedPreferences purchases;
    final String ADPURCHASE = "adpurchase";
    final String LIMITSPURCHASE = "limitspurchase";
    public static final String SKU_ADCLOSE = "adclose";
    public static final String SKU_LIMITS = "limits";
    // Объект интерфейса.
    public BaseResponse response = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);
        // Установка Navigation Drawer.
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Принудительная установка цвета бакграунда у боковой шторки.
        //drawer.setScrimColor(ContextCompat.getColor(this, R.color.navdrawscrim));
        // Инициализация объекта класса покупок.
        purchases = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvquxTyxofvSB5w5L4dd5eVlmH8jamPNpCYFCOGLkHU0s/vwMujjPBCTXas64TW+20w7MVL5zUoB/uNvEPBVIXN3k7518RH6U2j7SPTwjNv9OLjb6EvL9veJBZYCKsQvV7N2E0ciy7B80EEcFAx5giw0cQkwsZFDGzEYN1j9+sh+35KHpCmm5a/4BLzUiN4QJrnW76NPNUhr1SqQLvi+EPgGEAIakNmvhgiy9uthaw3dgUStf3yATsowS1EWE3D5KlNF7QRrSB3wJDWCM2mIb3TddWPSUyNuGz1lr5j3+/aoeGVMXBwGDGLlfP+cO+YPi2jraZZQ5LOHyNF1+NpmLIQIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    return; }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                // Setup successful. Querying inventory.
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
        if (!purchases.getString(ADPURCHASE, "").equals("yes")) {
             // Добавление рекламного баннера.
             MobileAds.initialize(getApplicationContext(), "ca-app-pub-7438405886273904/2742511071");
             AdView mAdView = (AdView) findViewById(R.id.my_adView);
             AdRequest adRequest = new AdRequest.Builder().build();
             mAdView.loadAd(adRequest);
        }
    }


    // Проверка уже существующих покупок.
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;
            // Is it a failure?
            if (result.isFailure()) {
                return;
            }
            // Проверка. Были ли ранее сделаны покупки.
            Purchase adcl = inventory.getPurchase(SKU_ADCLOSE);
            if (adcl != null && verifyDeveloperPayload(adcl) && adcl.getPurchaseState()==0) {  // 1 - canseled, 2 - refunded
            purchases.edit().putString(ADPURCHASE, "yes").commit();
            } else {
                if (isOnline()) {
                    purchases.edit().putString(ADPURCHASE, "").commit();
                }
            }

            Purchase lim = inventory.getPurchase(SKU_LIMITS);
            if (lim != null && verifyDeveloperPayload(lim) && lim.getPurchaseState()==0) {
                purchases.edit().putString(LIMITSPURCHASE, "yes").commit();
            } else {
                if (isOnline()) {
                    purchases.edit().putString(LIMITSPURCHASE, "").commit();
                }
            }
        }
    };

    // Обязательный метод класса покупок.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Верификация покупки (обязательный метод).
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }


    // Слушатель произведенной покупки.
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                showSnack("Error purchasing.");
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                showSnack("Error purchasing. Authenticity verification failed.");
                return;
            }
            if (purchase.getSku().equals(SKU_ADCLOSE)) {
                showSnack("Thank you for purchasing.");
                purchases.edit().putString(ADPURCHASE, "yes").commit();
            }
            else if (purchase.getSku().equals(SKU_LIMITS)) {
                showSnack("Thank you for purchasing.");
                purchases.edit().putString(LIMITSPURCHASE, "yes").commit();
            }
        }
    };

    // OnDestroy.
    @Override
    public void onDestroy() {
        super.onDestroy();
        // very important:
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
        response = null;
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

        //Initialize ImageView via FindViewById or programatically
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //Setup anim with desired properties
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE); //Repeat animation indefinitely
        anim.setDuration(3000); //Put desired duration per anim cycle here, in milliseconds
        //Start animation
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

    // Метод создания уведомлений Snack.
    public void showSnack(String text) {
        FrameLayout frame = (FrameLayout) findViewById(R.id.content_frame);
        Snackbar snackbar = Snackbar.make(frame, text, Snackbar.LENGTH_LONG).setAction("", null);
        View sbView = snackbar.getView();
        //sbView.setBackgroundResource(R.color.grey);
        TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }

    // Метод проверки наличия сети.
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    // Завершение Activity с очисткой памяти.
    public void end(String key, String Title) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
            super.onBackPressed();
        if (key.equals("home")) {
            response.FinishActivity();
            final Intent myint1 = new Intent(getBaseContext(), MainActivity.class);
            myint1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(myint1);
            System.exit(0);
        } else if (key.equals("mustsee")) {
            response.FinishActivity();
            final Intent intent = new Intent(getBaseContext(), Main2Activity.class);
            intent.putExtra("Country", "mustsee");
            intent.putExtra("Count", 0);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            System.exit(0);
        } else if (key.equals("saved")) {
            response.FinishActivity();
            final Intent myint2 = new Intent(getBaseContext(), SavedActivity.class);
            myint2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(myint2);
            System.exit(0);
        } else if (key.equals("country")) {
            response.FinishActivity();
            final Intent intent = new Intent(getBaseContext(), Main2Activity.class);
            intent.putExtra("Country", Title);
            intent.putExtra("Count", 0);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            System.exit(0);
        } else if (key.equals("random")) {
            response.FinishActivity();
            final Intent myint3 = new Intent(getBaseContext(), Main3Activity.class);
            myint3.putExtra("Country", "Empty");
            myint3.putExtra("Place", "Empty");
            myint3.putExtra("Count", 0);
            myint3.putExtra("Key", "random");
            myint3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(myint3);
            System.exit(0);
        } else if (key.equals("exit")) {
            if (Build.VERSION.SDK_INT >= 16) {
                finishAffinity();
            } else {
               ActivityCompat.finishAffinity(this);
            }
        }
    }


    // Слушатель нажатий боковой шторки.
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mainpage) {
            end("home", null);
        } else if (id == R.id.nav_interestplaces) {
            // Проверка подключения к интернет.
            if (isOnline()) {
                // Запуск параллельного потока для загрузки списка стран.
                mAsyncTask = new GetCountries().execute();
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_mustsee) {
            if (isOnline()) {
                end ("mustsee", null);
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_savedplaces) {
            end ("saved", null);
        } else if (id == R.id.nav_randomplace) {
            if (isOnline()) {
                end ("random", null);
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_flights) {
            if (isOnline()) {
                final Intent intent = new Intent("com.interestingplacesapp.dmitriy.appip.activity.WebViewActivity");
                intent.setData(Uri.parse("http://partner.onetwotrip.com/dl.rJ9H8LdPW"));
                startActivity(intent);
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_hotels) {
            if (isOnline()) {
                final Intent intent = new Intent("com.interestingplacesapp.dmitriy.appip.activity.WebViewActivity");
                intent.setData(Uri.parse("http://www.booking.com/index.html?aid=1307558"));
                startActivity(intent);
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_request) {
            if (isOnline()) {
                requestalertdialog();
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_rate) {
            if (isOnline()) {
                // Отправка пользователя в Google Play для оценки приложения.
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.interestingplacesapp.dmitriy.appip"));
                startActivity(intent);
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_paid) {
            if (isOnline()) {
                if (purchases.getString(ADPURCHASE, "").equals("yes")) {
                    showSnack("You have already removed ads.");
                } else {
                // Отправка пользователя в Google Play.
                mHelper.launchPurchaseFlow(this, SKU_ADCLOSE, 1, mPurchaseFinishedListener, null);
                    }
            } else {
                showSnack("There is no internet connection.");
            }
        } else if (id == R.id.nav_quit) {
            end ("exit", null);
        }
        // Закрытие боковой шторки после нажатия.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Диалог выбора страны.
    public void choosecountrydialog(ArrayList countries) {

        // Получаем Layout формы ввода.
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.choose_country_form, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Устанавливаем форму в строитель диалога.
        alertDialogBuilder.setView(promptsView);

        final TextView title = (TextView) promptsView
                .findViewById(R.id.alerttitle);
        title.setText("Choose country");

        final ListView lvMain = (ListView) promptsView.findViewById(R.id.lvMain);
        // Настраиваем и вызываем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.mylist, countries);
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
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                if (isOnline()) {
                    alertDialog.dismiss();
                    // Создание таймера для небольшой задержки, что дать закрыться alertDialog (иначе моргает).
                    CountDownTimer timer = new CountDownTimer(100, 100) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            // Получаем текст с нажатого элемента.
                            String Title = ((TextView) view).getText().toString();
                            end ("country", Title);
                        }
                    }.start();
                } else {
                    showToast("There is no internet connection.");
                }
            }
        });
    }


    // Класс параллельного потока (загрузка списка стран).
    public class GetCountries extends AsyncTask<String, String, String> {

        ArrayList<String> Countries = new ArrayList();

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
                JSONObject json=null;
                json = jsonParser.makeHttpRequest(url_sight, "GET", "");
                // Проверка на успешность запроса (возвращается из REST API).
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // Получение массива с данными.
                    JSONArray productObj = json.getJSONArray(TAG_COUNTRIES); // JSON Array
                    for (int x = 0; x != productObj.length(); x++) {
                        JSONObject country = productObj.getJSONObject(x);
                        Countries.add(country.getString(TAG_COUNTRY));
                    }
                    // Исключение повторяющихся элементов из ArrayList.
                    Set<String> hs = new HashSet<>();
                    hs.addAll(Countries);
                    Countries.clear();
                    Countries.addAll(hs);
                    // Сортировка ArrayList по алфавиту.
                    Collections.sort(Countries, String.CASE_INSENSITIVE_ORDER);
                } else {
                    // Страны не найдены. Запуск метода завершения потока.
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
            // Вызываем диалог выбора страны.
            choosecountrydialog(Countries);
            // Прячем диалог.
            closeprogress();
        }

        // Метод выполняется если произошла отмена выполнения основного метода.
        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Уведомление о невозможности загрузить страны.
            showToast("It's impossible to load the countries. Please check your connection or try later.");
            // Прячем диалог загрузки и завершаем Activity.
            closeprogress();
        }
    }

    // Диалог запроса добавления страны и места.
    public void requestalertdialog() {

        // Получаем Layout формы ввода (write_form.xml).
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.write_form, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // Устанавливаем write_form.xml в строитель диалога.
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.edit_country);
        final EditText userInput2 = (EditText) promptsView
                .findViewById(R.id.edit_place);
        final TextView title = (TextView) promptsView
                .findViewById(R.id.alerttitle);
        title.setText("Send request");
        final TextView title1 = (TextView) promptsView
                .findViewById(R.id.title1);
        title1.setText("Fill in all fields and press the 'Send' button. We'll add the place as soon as posible.");

        // Устанавливаем позитивную кнопку.
        alertDialogBuilder.setPositiveButton("Send", null);
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Прячем клавиатуру.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });
        // Создаем диалог.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Отображаем диалог.
        alertDialog.show();
        // Устанавливаем цвет текста кнопок.
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_button_text));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_button_text));
        // Отображаем клавиатуру.
        userInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // Слушатель позитивной кнопки.
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Проверка на пустое поле.
                if (TextUtils.isEmpty(userInput.getText()) | TextUtils.isEmpty(userInput2.getText())) {
                    showToast("There is no data for sending. Fill in all fields.");
                    return;
                } else {
                    // Запуск параллельного потока. Отправка и запись данных в БД.
                    mAsyncTask = new AddRequest().execute(String.valueOf(userInput.getText()), String.valueOf(userInput2.getText()));
                }
                // Прячем клавиатуру.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                alertDialog.dismiss();
            }
        });
    }


    // Класс параллельного потока (отправка запроса на добавление).
    public class AddRequest extends AsyncTask<String, String, String> {

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

                // Building Parameters
                String postparam = "Password=Slon1234&Country="+params[0]+"&Place="+params[1];

                // getting JSON Object
                JSONObject json = null;
                json = jsonParser.makeHttpRequest(url_request, "POST", postparam);
                int success1 = json.getInt(TAG_SUCCESS);
                if (success1 == 1) {

                } else {
                    // failed to create product
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
            showSnack("Your request was successfully sent.");
            // Прячем диалог.
            closeprogress();
        }

        // Метод выполняется если произошла отмена выполнения основного метода.
        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Уведомление о невозможности загрузить страны.
            showSnack("It's impossible to send your request. Please check your connection or try later.");
            // Прячем диалог загрузки и завершаем Activity.
            closeprogress();
        }
    }
}