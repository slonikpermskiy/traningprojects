package com.books.dmitriy.reader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.books.dmitriy.reader.asynctasks.AsyncResponse;
import com.books.dmitriy.reader.asynctasks.GetBooks;
import com.books.dmitriy.reader.asynctasks.GetOneBook;
import com.books.dmitriy.reader.utils.DBHelper;
import com.books.dmitriy.reader.utils.EncodeDecode;
import com.books.dmitriy.reader.utils.FileDialog;
import com.books.dmitriy.reader.utils.MySimpleArrayAdapter;
import com.books.dmitriy.reader.utils.RVAdapter;
import com.books.dmitriy.reader.utils.RVClickListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class MainActivity extends BaseActivity implements AsyncResponse, RVClickListener {

    int count = 0;
    String key;
    TextView EmptyText, title;
    RecyclerView rv;
    RVAdapter adapter;
    LinearLayoutManager llm;
    AppBarLayout appBar;
    CollapsingToolbarLayout coltoolbar;
    Toolbar toolbar;
    DBHelper DB;
    FloatingActionButton fab;
    String formenu="zero";
    // Массив для запоминания выбранных элементов списка.
    ArrayList<Integer> select = new ArrayList<Integer>();
    public AsyncTask mAsyncTask;
    FileDialog fileDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Установка контента во FrameLayout класса BaseActivity.
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_main, null,false);
        frameLayout.addView(activityView);
        rv = (RecyclerView)findViewById(R.id.rv);
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
        key = intent.getStringExtra("key");
        count = intent.getIntExtra("Count", 0);
        if (key == null) key = "";
        // Скрывающийся Toolbar.
        appBar = (AppBarLayout) findViewById(R.id.main_appbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        coltoolbar = (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);
        // Вызывается если нет контента для загрузки.
        EmptyText = (TextView) findViewById(R.id.EmptyText);
        EmptyText.setVisibility(View.GONE);
        // Toolbar Title.
        title = (TextView) findViewById(R.id.title);
        // Регистрация в классе БД.
        DB = new DBHelper(this);
        // Создание папки программы.
        File dirpar = new File(String.valueOf(getExternalFilesDir(null)));
        if (!dirpar.exists()) {
            dirpar.mkdir();
        }
        CreateRecycleView();
    }


    // Метод создания и обновления RecycleView.
    public void CreateRecycleView() {
        ArrayList<String> Titles = new ArrayList();
        ArrayList<String> Authors = new ArrayList();
        ArrayList<String> Time = new ArrayList();
        ArrayList<String> Favorite = new ArrayList();
        if (key!=null && key.equals("like")) {
            // Понравившиеся книги
            title.setText("Любимые книги");
            ArrayList<String>[] request = DB.likebooks();
            Titles = request[0];
            Authors = request[1];
            Time = request[2];
            Favorite = request[3];
            EmptyText.setText("У Вас нет любимых книг. Вернитесь в общий список и нажмите 'звездочку', чтобы добавить книгу в список любимых.");
            fab.setVisibility(View.GONE);
        } else {
                // стандартный запуск
                title.setText("Все книги");
                ArrayList<String>[] request = DB.dataforbooks();
                Titles = request[0];
                Authors = request[1];
                Time = request[2];
                Favorite = request[3];
                EmptyText.setText("У Вас нет сохраненных книг. Нажмите кнопку в нижней части экрана, чтобы добавить книгу.");
        }
        // Подготовка RecycleView.
        rv.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        // Перечень для отображения выделенных элементов.
        select.clear();
        for (int x = 0; x<=Titles.size()-1; x++) {
            select.add(0);
        }
        // Инициализируем заполнение RecycleView.
        adapter = new RVAdapter(Titles, Authors, Time, Favorite, select, MainActivity.this);
        rv.setAdapter(adapter);
        // Устанавливаем список на нужную позицию.
        rv.scrollToPosition(count-2);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               choosedialog();
            }
        });
        if (Titles.isEmpty() & Authors.isEmpty()) {
            EmptyText.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
        else {EmptyText.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
    }

    // Первое создание меню toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        menu.findItem(R.id.action_deleteone).setVisible(false);
        menu.findItem(R.id.action_close).setVisible(false);
        return true;
    }

    // Метод обновления меню toolbar. Вызывается методом invalidateOptionsMenu();
    @Override
    public boolean  onPrepareOptionsMenu(Menu menu) {
        if (formenu=="one") {
            // Отображаем некоторые значки.
            menu.findItem(R.id.action_deleteone).setVisible(true);
            menu.findItem(R.id.action_close).setVisible(true);
        } else if (formenu=="zero") {
            // Скрываем некоторые значки.

            menu.findItem(R.id.action_deleteone).setVisible(false);
            menu.findItem(R.id.action_close).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Слушатель нажатия кнопок toolbar.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_deleteone) {
            bookdeleter();
        } else if (id == R.id.action_close) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    // Метод удаления данных.
    public void bookdeleter() {
        // Создаем дополнительный массив для обмена.
        ArrayList<Integer> newone = new ArrayList<Integer>();
        // Записываем в новый массив значения в которых установлена "1".
        for (int x = 0; x <= select.size() - 1; x++) {
            if (select.get(x)==1){
                newone.add(x);
            }
        }
        ArrayList<String> titledel = new ArrayList<String>();
        ArrayList<String> authordel = new ArrayList<String>();
        ArrayList<String>[] request = DB.dataforbooks();
        ArrayList<String> Titles = request[0];
        ArrayList<String> Authors = request[1];

        for (int x = 0; x <= newone.size() - 1; x++) {
            titledel.add((String) Titles.get(newone.get(x)));
            authordel.add((String) Authors.get(newone.get(x)));
        }
        for (int x = 0; x <= titledel.size() - 1; x++) {
            deletefile(DB.dataforonebook(titledel.get(x), authordel.get(x)));
            DB.deletebook(titledel.get(x), authordel.get(x));
        }
        // Очищаем массив для обмена.
        titledel.clear();
        authordel.clear();
        newone.clear();
        showToast("Книги удалены.");
        // Возвращаем меню по умолчанию.
        formenu = "zero";
        invalidateOptionsMenu();
        CreateRecycleView();
    }

    // Метод удаления файла.
    public void deletefile(String path) {
        try {
            File file = new File(String.valueOf(getExternalFilesDir(null))+"/"+path+".html");
            if (file.exists()) { file.delete(); }
        } catch (Exception e) {
            showToast("Невозможно удалить некоторые файлы. Попробуйте сделать это вручную.");
        }
    }


    // Слушатель кнопки "назад".
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            // Если открыто меню "удаления", то снимаем все выделения и возвращаем стандартное меню.
        } else if (formenu=="one") {
            // Создаем дополнительный массив для обмена.
            ArrayList<Integer> newone = new ArrayList<Integer>();
            // Записываем в новый массив значения в которых установлена "1".
            for (int x = 0; x <= select.size() - 1; x++) {
                if (select.get(x)==1){
                    newone.add(x);
                }
            }
            // Перезаписываем старый массив нулями.
            select.clear();
            ArrayList<String>[] request = DB.dataforbooks();
            ArrayList<String> Titles = request[0];
            for (int x = 0; x <= Titles.size() - 1; x++) {
                select.add(0);
            }
            // Оповещаем адаптер, что данные изменились (толко те поля, где были "1").
            for (int x=0; x <= newone.size() - 1; x++ ) {
                adapter.notifyItemChanged(newone.get(x));
            }
            // Очищаем массив для обмена.
            newone.clear();
            // Возвращаем меню по умолчанию.
            formenu = "zero";
            invalidateOptionsMenu();
            // Очищаем все данные и закрываем Activity.
        } else {
            super.onBackPressed();
        }
    }


    // Диалог выбора книги (загрузка из внешнего хранилища).
    public void choosebookdialog(final ArrayList titles, ArrayList authors, final ArrayList links) {

        // Получаем Layout формы ввода.
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.choose_book, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Устанавливаем форму в строитель диалога.
        alertDialogBuilder.setView(promptsView);
        final TextView title = (TextView) promptsView.findViewById(R.id.alerttitle);
        title.setText("Выберите книгу");
        final ListView lvMain = (ListView) promptsView.findViewById(R.id.lvMain);
        // Создание адаптера, присваивание ему значений, установка адаптера в ListView.
        MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this, titles, authors);
        lvMain.setAdapter(adapter);
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // Создаем диалог.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Отображаем диалог.
        alertDialog.show();
        // Устанавливаем цвет текста кнопки.
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        // Слушатель нажатий на созданные элементы ListView.
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                // Сохраняем
                if (isOnline()) {
                    alertDialog.dismiss();
                    // Получаем текст с нажатого элемента.
                    String Title = ((TextView) view.findViewById(R.id.label)).getText().toString();
                    String Author = ((TextView) view.findViewById(R.id.label2)).getText().toString();
                    String Link = (String) links.get(titles.indexOf(Title));
                    if (DB.ifthereisbook(Title, Author) != 1) {
                    if (isOnline()) {
                        // Запуск параллельного потока для загрузки списка мест по стране.
                        mAsyncTask = new GetOneBook(MainActivity.this).execute(Title, Author, Link);
                    } else {
                        showToast("Нет интернет соединения.");}
                    } else {
                        showToast("У Вас уже есть такая книга.");}
                } else {
                    alertDialog.dismiss();
                    showToast("Нет интернет соединения.");
                }
            }
        });
    }


    // Диалог выбора способа загрузки.
    public void choosedialog() {
        ArrayList <String> choose = new ArrayList();
        choose.add("Из внешнего хранилища");
        choose.add("По ссылке");
        choose.add("Из памяти телефона");
        // Получаем Layout формы ввода.
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.choose_book, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Устанавливаем форму в строитель диалога.
        alertDialogBuilder.setView(promptsView);
        final TextView title = (TextView) promptsView.findViewById(R.id.alerttitle);
        title.setText("Выберите вариант загрузки книги");
        final ListView lvMain = (ListView) promptsView.findViewById(R.id.lvMain);
        // Настраиваем и вызываем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listv1, choose);
        lvMain.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        // Устанавливаем цвет селектора.
        lvMain.setSelector(R.drawable.layout_toggle);
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // Создаем диалог.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Отображаем диалог.
        alertDialog.show();
        // Устанавливаем цвет текста кнопки.
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        // Слушатель нажатий на созданные элементы ListView.
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                alertDialog.dismiss();
                // Получаем текст с нажатого элемента.
                String Title = ((TextView) view).getText().toString();
                if (Title.equals("Из внешнего хранилища")) {
                    if (isOnline()) {
                        // Запуск параллельного потока для загрузки списка мест по стране.
                        mAsyncTask = new GetBooks(MainActivity.this).execute();
                    } else {
                        alertDialog.dismiss();
                        showToast("Нет интернет соединения.");
                    }
                } else if (Title.equals("По ссылке")) {
                    if (isOnline()) {
                        linkdialog();
                    } else {
                        alertDialog.dismiss();
                        showToast("Нет интернет соединения.");
                    }
                } else if (Title.equals("Из памяти телефона")) {
                        filedialog();
                }

            }
        });
    }

    // Диалог загрузки по ссылке.
    public void linkdialog() {
        // Получаем Layout формы ввода (write_form.xml).
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.write_form1, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.edit);
        final EditText userInput2 = (EditText) promptsView.findViewById(R.id.edit1);
        final EditText userInput3 = (EditText) promptsView.findViewById(R.id.edit2);
        final TextView title = (TextView) promptsView.findViewById(R.id.alerttitle);
        title.setText("Загрузка книги по ссылке");
        final TextView title1 = (TextView) promptsView.findViewById(R.id.title1);
        title1.setText("Заполните все поля.");
        // Устанавливаем позитивную кнопку.
        alertDialogBuilder.setPositiveButton("Загрузить", null);
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
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
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        // Отображаем клавиатуру.
        userInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        // Слушатель позитивной кнопки.
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Проверка на пустое поле.
                if (TextUtils.isEmpty(userInput.getText()) | TextUtils.isEmpty(userInput2.getText()) | TextUtils.isEmpty(userInput3.getText())) {
                    showToast("Вы не заполнили поля.");
                    return;
                } else {
                    if (isOnline()) {
                    mAsyncTask = new GetOneBook(MainActivity.this).execute(String.valueOf(userInput.getText()), String.valueOf(userInput2.getText()), String.valueOf(userInput3.getText()));
                    } else {
                        showToast("Нет интернет соединения.");}
                    }
                // Прячем клавиатуру.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                alertDialog.dismiss();
            }
        });
    }


    // Диалог загрузки файла.
    public void filedialog() {
        // Получаем Layout формы ввода (write_form.xml).
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.write_form2, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.edit);
        final EditText userInput2 = (EditText) promptsView.findViewById(R.id.edit1);
        final RadioButton but1 = (RadioButton) promptsView.findViewById(R.id.txt);
        final RadioButton but2 = (RadioButton) promptsView.findViewById(R.id.html);
        final TextView title = (TextView) promptsView.findViewById(R.id.alerttitle);
        title.setText("Загрузка книги из файла");
        final TextView title1 = (TextView) promptsView.findViewById(R.id.title1);
        title1.setText("Заполните все поля.");
        // Устанавливаем позитивную кнопку.
        alertDialogBuilder.setPositiveButton("Загрузить", null);
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
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
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        userInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        // Слушатель позитивной кнопки.
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Проверка на пустое поле.
                if (TextUtils.isEmpty(userInput.getText()) | TextUtils.isEmpty(userInput2.getText())) {
                    showToast("Вы не заполнили поля.");
                    return;
                } else {
                    String filetype = "";
                    if (but1.isChecked()) {filetype = "TXT";}
                    if (but2.isChecked()) {filetype = "HTML";}
                    filechoosedialog(userInput.getText().toString(), userInput2.getText().toString(), filetype);
                }
                // Прячем клавиатуру.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                alertDialog.dismiss();
            }
        });
    }

    // Диалог выбора файла.
    public void filechoosedialog(final String title, final String author, String key) {

        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        if (key.equals("TXT")) {fileDialog = new FileDialog(this, mPath, ".txt");
        }  else if (key.equals("HTML")) {fileDialog = new FileDialog(this, mPath, ".html");}
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                bookfromfilesaver(title, author, file);
            }
        });
        fileDialog.showDialog();
    }

    // Метод загрузки текста из файла.
    public void bookfromfilesaver (String title, String author, File file) {
        // Метод чтения текста из файла.
            String text;
            try{
                int length = (int) file.length();
                byte[] bytes = new byte[length];
                FileInputStream in = new FileInputStream(file);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }
                text = new String(bytes);
                EncodeDecode ed = new EncodeDecode();
                ArrayList<String> okresult = ed.encode(text);
                // Сохраняем.
                String filename = title+"_"+author;
                filewriter(filename, okresult.get(1));
                DB.savebook(title, author, okresult.get(0), filename);
                CreateRecycleView();
            } catch (Exception e) {
                showToast("Нет возможности открыть файл. Возможно он поврежден.");
            }
    }


    public String filewriter (String filename, String text) {
        String response = null;
        String path = String.valueOf(getExternalFilesDir(null));
        try {
        // Сохранение html в файл.
        File file = new File(path + "/" + filename+".html");
        // Запись html-кода в файл.
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(file)));
            writer.print(text);
        writer.close();
            response = "ok";
        }catch(Exception e){
            showToast("Не удалось сохранить книгу. Попробуйте еще раз.");
            return response;
        }
        return response;
    }


    // Результат загрузки списка книг через AsyncTask - через интерфейс.
    @Override
    public void GetBooksFinish(ArrayList<String> Titles, ArrayList<String> Authors, ArrayList<String> Links) {
        choosebookdialog(Titles, Authors, Links);
        closeprogress();
    }

    // Результат загрузки книги через AsyncTask - через интерфейс.
    @Override
    public void GetOneBookFinish(String title, String author, String key, String text) {
        String filename = title+"_"+author;
        if (filewriter(filename, text) != null) {
        DB.savebook(title, author, key, filename);}
        CreateRecycleView();
        // Прячем диалог загрузки и завершаем Activity.
        closeprogress();
    }

    // Неудачная загрузка AsyncTask - через интерфейс.
    @Override
    public void GetBooksNotFinish() {
        showToast("Невозможно загрузить. Проверьте Ваше соединение, ссылку или попробуйте позже.");
        closeprogress();
    }

    // Показываем диалог загрузки.
    @Override
    public void LoadingShower() {
        showprogress();
    }

    // Слушатель нажаий RecyclerView - через интерфейс.
    @Override
    public void itemClicked(View view) {
        TextView label = (TextView)view.findViewById(R.id.label);
        TextView label2 = (TextView)view.findViewById(R.id.label2);
        String Title = label.getText().toString();
        String Author = label2.getText().toString();
        // Получаем номер нажатого элемента и передаем его в Intent.
        count = rv.getChildAdapterPosition(view);
        // Очищаем выделения элементов. Создаем дополнительный массив для обмена.
        ArrayList<Integer> newone = new ArrayList<Integer>();
        // Записываем в новый массив значения в которых установлена "1".
        for (int x = 0; x <= select.size() - 1; x++) {
            if (select.get(x)==1){
                newone.add(x);
            }
        }
        // Перезаписываем старый массив нулями.
        select.clear();
        ArrayList<String>[] request = DB.dataforbooks();
        ArrayList<String> Titles = request[0];
        for (int x = 0; x <= Titles.size() - 1; x++) {
            select.add(0);
        }
        // Оповещаем адаптер, что данные изменились (толко те поля, где были "1").
        for (int x=0; x <= newone.size() - 1; x++ ) {
            adapter.notifyItemChanged(newone.get(x));
        }
        // Очищаем массив для обмена.
        newone.clear();
        // Возвращаем меню по умолчанию.
        formenu = "zero";
        invalidateOptionsMenu();
        final Intent intent = new Intent(getBaseContext(), Main2Activity.class);
        intent.putExtra("title", Title);
        intent.putExtra("author", Author);
        startActivity(intent);

    }

    // Слушатель длинных нажаий RecyclerView - через интерфейс.
    @Override
    public void itemlongClicked(View view) {
        // Получаем порядковый номер выбранного элемента.
        count = rv.getChildAdapterPosition(view);
        // Выделение элемента. Создаем дополнительный массив для обмена.
        ArrayList<Integer> newone = new ArrayList<Integer>();
        // Перезаписываем наш массив в новый.
        for (int x = 0; x <= select.size() - 1; x++) {
            newone.add(select.get(x));
        }
        // Очищаем наш массив.
        select.clear();
        // Записываем наш массив заново. Все данные старые, кроме выделенного элемента, его меняем на противоположный.
        for (int x = 0; x <= newone.size() - 1; x++) {
            if (x==count){
                if (newone.get(x) == 1) {
                    select.add(0);
                } else {
                    select.add(1);
                }
            } else {
                select.add(newone.get(x));}
        }
        // Очищаем новый массив.
        newone.clear();
        // Устанавливаем меню. Если в масииве есть хоть одна "1", то меню "удаление", если нет, то обычное.
        formenu = "zero";
        for (int x = 0; x <= select.size() - 1; x++) {
            if (select.get(x)==1) {formenu = "one";}
        }
        invalidateOptionsMenu();
        // Оповещаем адптер, что изменился выбранный элемент.
        adapter.notifyItemChanged(count);
    }

    // Слушатель нажаий на звездочку в RecyclerView - через интерфейс.
    @Override
    public void starClicked(View view) {
        if (key!=null && !key.equals("like")) {
            count = rv.getChildAdapterPosition(view);
            ArrayList<String>[] request = DB.dataforbooks();
            ArrayList<String> Titles = request[0];
            ArrayList<String> Authors = request[1];
            DB.Like(Titles.get(count), Authors.get(count));
            CreateRecycleView();
        }
    }
}
