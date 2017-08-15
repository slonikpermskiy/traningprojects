package com.books.dmitriy.reader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.books.dmitriy.reader.utils.DBHelper;
import com.books.dmitriy.reader.utils.EncodeDecode;
import com.books.dmitriy.reader.utils.TextDivider;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


public class Main2Activity extends BaseActivity implements GestureDetector.OnGestureListener {


    String titleb, author, text, keytext;
    TextView Text, title, textView, EmptyText;
    AppBarLayout appBar;
    CollapsingToolbarLayout coltoolbar;
    Toolbar toolbar;
    DBHelper DB;
    ArrayList<String> list = new ArrayList();
    int currentpage = 0;
    ImageButton fab, fab1;
    TextDivider preptext;
    // Переменные для распознавания жеста "свайп" (для смены фотографии).
    private static final int SWIPE_MIN_DISTANCE = 40;
    private static final int SWIPE_MAX_OFF_PATH = 450;
    private static final int SWIPE_THRESHOLD_VELOCITY = 500;
    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Установка контента во FrameLayout класса BaseActivity.
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_main2, null,false);
        frameLayout.addView(activityView);
        // Установка Toolbar.
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Установка цвета StatusBar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        // Установка кнопки открытия Navigation Drawer в Toolbar.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Получение Intent.
        Intent intent = getIntent();
        titleb = intent.getStringExtra("title");
        author = intent.getStringExtra("author");
        // Скрывающийся Toolbar.
        appBar = (AppBarLayout) findViewById(R.id.main_appbar);
        coltoolbar = (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);
        Text = (TextView) findViewById(R.id.EmptyText);
        // Устанавливаем слушатель жестов.
        gestureDetector = new GestureDetector(this, this);
        Text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        Text.setTextSize(14); // Есть возможность установить шрифт: 16, 18, 20.
        // Toolbar Title.
        title = (TextView) findViewById(R.id.title);
        title.setTextSize(15);
        title.setText(titleb);
        // Регистрация в классе БД.
        DB = new DBHelper(this);
        text = DB.dataforonebook(titleb, author);
        keytext = DB.keyforbook(titleb, author);
        text = bookfromfile(text);
        textView = (TextView) findViewById(R.id.textView);
        EmptyText = (TextView) findViewById(R.id.EmptyText2);
        EmptyText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        fab = (ImageButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!preptext.getpage(currentpage+1).equals("finish")) {
                if (Text.getVisibility()==View.VISIBLE & EmptyText.getVisibility()==View.GONE) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        EmptyText.setText(Html.fromHtml(preptext.getpage(currentpage+1), 0)); // for 24 api and more
                    } else {
                        EmptyText.setText(Html.fromHtml(preptext.getpage(currentpage+1))); // or for older api
                    }
                    Animation shake = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_out_left);
                    Text.setAnimation(shake);
                    Animation shake1 = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_in_left);
                    EmptyText.setAnimation(shake1);
                    EmptyText.setVisibility(View.VISIBLE);
                    Text.setVisibility(View.GONE);
                    currentpage++;
                    textView.setText(String.valueOf(currentpage + 1));
                } else if (Text.getVisibility()==View.GONE & EmptyText.getVisibility()==View.VISIBLE) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        Text.setText(Html.fromHtml(preptext.getpage(currentpage+1), 0)); // for 24 api and more
                    } else {
                        Text.setText(Html.fromHtml(preptext.getpage(currentpage+1))); // or for older api
                    }
                    Animation shake = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_in_left);
                    Text.setAnimation(shake);
                    Animation shake1 = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_out_left);
                    EmptyText.setAnimation(shake1);
                    EmptyText.setVisibility(View.GONE);
                    Text.setVisibility(View.VISIBLE);
                    currentpage++;
                    textView.setText(String.valueOf(currentpage + 1));
                }
                }
            }
        });
        fab1 = (ImageButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentpage != 0) {
                    if (Text.getVisibility()==View.VISIBLE & EmptyText.getVisibility()==View.GONE) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            EmptyText.setText(Html.fromHtml(preptext.getpage(currentpage-1), 0)); // for 24 api and more
                        } else {
                            EmptyText.setText(Html.fromHtml(preptext.getpage(currentpage-1))); // or for older api
                        }
                        Animation shake = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_out_right);
                        Text.setAnimation(shake);
                        Animation shake1 = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_in_right);
                        EmptyText.setAnimation(shake1);
                        EmptyText.setVisibility(View.VISIBLE);
                        Text.setVisibility(View.GONE);
                        currentpage--;
                        textView.setText(String.valueOf(currentpage + 1));
                    } else if (Text.getVisibility()==View.GONE & EmptyText.getVisibility()==View.VISIBLE) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            Text.setText(Html.fromHtml(preptext.getpage(currentpage-1), 0)); // for 24 api and more
                        } else {
                            Text.setText(Html.fromHtml(preptext.getpage(currentpage-1))); // or for older api
                        }
                        Animation shake = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_in_right);
                        Text.setAnimation(shake);
                        Animation shake1 = AnimationUtils.loadAnimation(Main2Activity.this, R.anim.slide_out_right);
                        EmptyText.setAnimation(shake1);
                        EmptyText.setVisibility(View.GONE);
                        Text.setVisibility(View.VISIBLE);
                        currentpage--;
                        textView.setText(String.valueOf(currentpage + 1));
                    }
                }
            }
        });
        requestalertdialog();
    }


    // Метод загрузки текста из файла.
    public String bookfromfile (String filename) {
        // Метод чтения текста из файла.
        String path = String.valueOf(getExternalFilesDir(null));
        String text = "Невозможно получить текст. Проблемы с файлом.";
        try{
            File file = new File(path+"/"+filename+".html");
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }
            text = new String(bytes);
        } catch (Exception e) {
            return  text;
        }
        return text;
    }

    // Слушатель кнопки "назад".
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            final Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("key", "");
            intent.putExtra("Count", 0);
            startActivity(intent);
        }
    }


    public void requestalertdialog() {
        // Получаем Layout формы ввода (write_form.xml).
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.write_form, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Устанавливаем write_form.xml в строитель диалога.
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.edit_password);
        final TextView title = (TextView) promptsView.findViewById(R.id.alerttitle);
        title.setText("Книга зашифрована.");
        final TextView title1 = (TextView) promptsView.findViewById(R.id.title1);
        title1.setText("Введите пароль (1234), чтобы расшифровать, иначе текст останется зашифрованным.");
        // Устанавливаем позитивную кнопку.
        alertDialogBuilder.setPositiveButton("Send", null);
        // Создаем диалог.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Отображаем диалог.
        alertDialog.show();
        // Устанавливаем цвет текста кнопки.
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        // Отображаем клавиатуру.
        userInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // Слушатель позитивной кнопки.
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Прячем клавиатуру.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                alertDialog.dismiss();
                if (userInput.getText().toString().equals("1234")) {
                    // Расшифровка текста.
                EncodeDecode ed = new EncodeDecode();
                text = ed.decode(keytext, text);
                } else {text = "Книга зашифрована. Пароль неверный.";}
                preptext = new TextDivider(Text.getWidth(), Text.getHeight(), (int) Text.getTextSize(), text);
                EmptyText.setTextSize(14);
                // Разный метод отображения html в TextView в зависимости от версии ОС.
                if (Build.VERSION.SDK_INT >= 24) {
                    Text.setText(Html.fromHtml(preptext.getpage(0), 0)); // for 24 api and more
                } else {
                    Text.setText(Html.fromHtml(preptext.getpage(0))); // or for older api
                }
                textView.setText(String.valueOf(currentpage+1));
                EmptyText.setVisibility(View.GONE);

            }
        });
    }


    // Обязательный метод слушателя жестов.
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }
    // Обязательный метод слушателя жестов.
    @Override
    public void onShowPress(MotionEvent e) {
    }
    // Слушатель одиночного нажатия.
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
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
                fab.performClick();
                // Правый "свайп".
            } else if (-diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                fab1.performClick();
            }
        } catch (Exception e) {
        }
        return true;
    }
}
