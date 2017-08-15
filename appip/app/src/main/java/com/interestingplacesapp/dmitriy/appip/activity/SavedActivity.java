package com.interestingplacesapp.dmitriy.appip.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.interestingplacesapp.dmitriy.appip.R;
import com.interestingplacesapp.dmitriy.appip.helpers.DBHelper;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SavedActivity extends BaseActivity implements BaseResponse {

    // Переменная для запоминания позиции списка.
    int count = 0;
    // Переменные текстовых полей.
    TextView EmptyText, title;
    // Переменные для RecycleView.
    RecyclerView rv;
    SavedActivity.RVAdapter adapter;
    LinearLayoutManager llm;
    // Экземпляр AsyncTask.
    private AsyncTask mAsyncTask;
    // Переменные Universal Image Loader.
    DisplayImageOptions options;
    ImageLoaderConfiguration config;
    // Скрывающийся ToolBar.
    AppBarLayout appBar;
    // Переменная БД.
    DBHelper DB;
    // Переменная для смены меню Toolbar.
    String formenu="zero";
    CollapsingToolbarLayout collapse;
    // Массив для запоминания выбранных элементов списка.
    ArrayList<Integer> select = new ArrayList<Integer>();
    FloatingActionButton fab, fab1;
    RelativeLayout mapcont;
    RadioGroup tog;
    ImageButton home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_main2, null,false);
        frameLayout.addView(activityView);
        // Назначаем объекту интерфейса в суперклассе значение нашей Activity.
        response = SavedActivity.this;
        // Получаем интент.
        Intent intent = getIntent();
        count = intent.getIntExtra("Count", 0);
        // Установка Toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appBar = (AppBarLayout) findViewById(R.id.main_appbar);
        // Установка кнопки открытия Navigation Drawer в Toolbar.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Collapsing Toolbar.
        collapse = (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);
        // Регистрация в классе БД.
        DB = new DBHelper(this);
        // Тектовое поле для отображения если список пуст.
        EmptyText = (TextView) findViewById(R.id.EmptyText);
        EmptyText.setVisibility(View.GONE);
        // Переключатель
        tog = (RadioGroup) findViewById(R.id.toggle);
        tog.setVisibility(View.GONE);
        // Toolbar Title.
        title = (TextView) findViewById(R.id.title);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "georgiai.ttf");
        title.setTypeface(typeFace);
        title.setText("Saved places");

        mapcont = (RelativeLayout) findViewById(R.id.mapcont);
        mapcont.setVisibility(View.GONE);
        home = (ImageButton) findViewById(R.id.imageButton);
        home.setVisibility(View.GONE);

          // UNIVERSAL IMAGE LOADER SETUP
          options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageForEmptyUri(R.drawable.no_photo)
                .showImageOnFail(R.drawable.no_photo)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
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
        // RecyclerView.
        rv = (RecyclerView)findViewById(R.id.rv);
        // Запускаем создание списка.
        CreateRecycleView();
        // Скрываем Collapsing Toolbar, если список будет сдвинут.
        if (count > 2) {
            appBar.setExpanded(false);
            toolbar.setVisibility(View.VISIBLE);
        }
    }


    // Метод создания и обновления RecyclerView.
    public void CreateRecycleView() {

        ArrayList<String> Countries = new ArrayList();
        ArrayList<String> Places = new ArrayList();
        ArrayList<String> Photos = new ArrayList();
        Countries = DB.dataforcountries();
        Places = DB.dataforplaces();
        Photos = DB.dataforphotos();
        // Перечень для отображения выделенных элементов.
        select.clear();
        for (int x = 0; x<=Countries.size()-1; x++) {
                select.add(0);
        }
        // Подготовка RecycleView.
        rv.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        // Инициализируем заполнение RecycleView.
        adapter = new SavedActivity.RVAdapter(Countries, Places, Photos, select);
        rv.setAdapter(adapter);
        rv.scrollToPosition(count);
        // Если список пуст, то скрываем RecycleView и поазываем TextView.
        if (Countries.isEmpty() & Places.isEmpty() & Photos.isEmpty()) {
            EmptyText.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);}
        else {EmptyText.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);}
    }


    // Метод удаления всех данных (файлов и записей из БД).
    public void datadeleter() {
        if (rv.getChildCount()!=0) {
        DB.deleteAll();
        try {
        File file = new File(String.valueOf(getExternalFilesDir(null)));
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                File f = new File(file, children[i]);
                if (f.exists()) { f.delete(); }
            }
        }
        } catch (Exception e) {
            showToast("It's impossible to delete the sights's files in a normal way. Please try to delete it by hand.");
        }
        showSnack("All sights were successfully deleted.");
        } else {
            showSnack("You don't have any saved sights.");
        }
    }

    // Метод удаления одной достопримечательности (файлы и запись в БД).
    public void deletesight(String country, String place) {

        ArrayList<String> sightdata = new ArrayList<String>();
        sightdata = DB.dataforsight(country, place);
        int pos = sightdata.get(2).lastIndexOf("/");
        String html = sightdata.get(2).substring(pos);
        deletefile(html);
        int pos1 = sightdata.get(8).lastIndexOf("/");
        String photo = sightdata.get(8).substring(pos1);
        deletefile(photo);
        int pos2 = sightdata.get(9).lastIndexOf("/");
        String photo1 = sightdata.get(9).substring(pos2);
        deletefile(photo1);
        int pos3 = sightdata.get(10).lastIndexOf("/");
        String map = sightdata.get(10).substring(pos3);
        deletefile(map);
        // Удаление записи из БД.
        DB.deletesight(country, place);
    }

    // Метод удаления файла.
    public void deletefile(String path) {
        try {
            File file = new File(String.valueOf(getExternalFilesDir(null))+path);
            if (file.exists()) { file.delete(); }
        } catch (Exception e) {
            showToast("It's impossible to delete some files in a normal way. Please try to delete it by hand.");
        }
    }

    // Диалог удаления достопримечательностей.
    private void deletesights(String maintitle, String subtitle, final String key) {

        // Получаем Layout формы ввода (write_form.xml).
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.inform_dialog, null);
        // Строитель диалога.
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Устанавливаем write_form.xml в строитель диалога.
        alertDialogBuilder.setView(promptsView);
        final TextView title = (TextView) promptsView
                .findViewById(R.id.alerttitle);
        title.setText(maintitle);
        alertDialogBuilder.setView(promptsView);
        final TextView title1 = (TextView) promptsView
                .findViewById(R.id.title1);
        title1.setText(subtitle);

        // Устанавливаем позитивную кнопку.
        alertDialogBuilder.setPositiveButton("Yes", null);
        // Устанавливаем негативную кнопку.
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                if (key.equals("all")) {
                datadeleter();
                CreateRecycleView();
                } else if (key.equals("one")) {
                    // Создаем дополнительный массив для обмена.
                ArrayList<Integer> newone = new ArrayList<Integer>();
                // Записываем в новый массив значения в которых установлена "1".
                for (int x = 0; x <= select.size() - 1; x++) {
                    if (select.get(x)==1){
                    newone.add(x);
                    }
                }
                ArrayList<String> countrydel = new ArrayList<String>();
                ArrayList<String> placedel = new ArrayList<String>();
                for (int x = 0; x <= newone.size() - 1; x++) {
                    countrydel.add((String) DB.dataforcountries().get(newone.get(x)));
                    placedel.add((String) DB.dataforplaces().get(newone.get(x)));
                }
                for (int x = 0; x <= countrydel.size() - 1; x++) {
                    deletesight(countrydel.get(x), placedel.get(x));
                }
                // Очищаем массив для обмена.
                countrydel.clear();
                placedel.clear();
                newone.clear();
                showSnack("Sights were successfully deleted.");
                // Возвращаем меню по умолчанию.
                formenu = "zero";
                invalidateOptionsMenu();
                CreateRecycleView();
                }
                alertDialog.dismiss();
            }
        });
    }

    // Первое создание меню toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu1, menu);

        menu.findItem(R.id.action_back).setVisible(true);
        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_deleteone).setVisible(false);
        menu.findItem(R.id.action_close).setVisible(false);
        return true;
    }

    // Метод обновления меню toolbar. Вызывается методом invalidateOptionsMenu();
    @Override
    public boolean  onPrepareOptionsMenu(Menu menu) {
        if (formenu=="one") {
            // Отображаем некоторые значки.
            menu.findItem(R.id.action_back).setVisible(false);
            menu.findItem(R.id.action_settings).setVisible(false);
            menu.findItem(R.id.action_deleteone).setVisible(true);
            menu.findItem(R.id.action_close).setVisible(true);
        } else if (formenu=="zero") {
            // Скрываем некоторые значки.
            menu.findItem(R.id.action_back).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(true);
            menu.findItem(R.id.action_deleteone).setVisible(false);
            menu.findItem(R.id.action_close).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Слушатель нажатия кнопок toolbar.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            onBackPressed();
        } else if (id == R.id.action_delete) {
            deletesights("Delete all sights", "Press 'Yes' to delete all saved sights. If you want to choose your deleting, make a long tap on every sight.", "all");
        } else if (id == R.id.action_deleteone) {
            deletesights("Delete sights", "Are you sure ?", "one");
        } else if (id == R.id.action_close) {
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
            for (int x = 0; x <= DB.dataforcountries().size() - 1; x++) {
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
            finish();
            final Intent myint = new Intent(getBaseContext(), MainActivity.class);
            myint.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(myint);
            System.exit(0);
        }
    }

    // Завершение Activity с очисткой памяти (перезапись метода).
    public void end(String key, String Country, String Place, String Title) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);}
        super.onBackPressed();

        if (key.equals("forward")) {
            finish();
            final Intent myint3 = new Intent(SavedActivity.this, SavedSight.class);
            myint3.putExtra("Country", Country);
            myint3.putExtra("Place", Place);
            myint3.putExtra("Count", count);
            myint3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(myint3);
        }
        System.exit(0);
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

    // Метод интерфейса для завершения Activity.
    @Override
    public void FinishActivity() {
        finish();
    }


    // Класс определения элементов CardView для построения RecyclerView.
    public class CountriesViewHolder extends RecyclerView.ViewHolder {

        TextView label;
        TextView label2;
        ImageView icon;
        ImageView icon2;
        LinearLayout cardlayout;

        // Конструктор.
        public CountriesViewHolder(final View itemView) {

            super(itemView);
            itemView.setLongClickable(true);
            // Layout для слушателя нажатий.
            cardlayout = (LinearLayout) itemView.findViewById(R.id.cardlayout5);
            label = (TextView)itemView.findViewById(R.id.label);
            label2 = (TextView)itemView.findViewById(R.id.label2);
            icon = (ImageView)itemView.findViewById(R.id.icon);
            icon2 = (ImageView)itemView.findViewById(R.id.icon2);

            // Слушатель нажатий на элементы RecyclerView.
            cardlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Place = label.getText().toString();
                    String Country = label2.getText().toString();
                    // Получаем номер нажатого элемента и передаем его в Intent.
                    count = rv.getChildAdapterPosition(itemView);
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
                    for (int x = 0; x <= DB.dataforcountries().size() - 1; x++) {
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

                    end ("forward", Country, Place, null);
                }
            });
            // Слушатель длинных нажатий на элементы RecyclerView.
            cardlayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Получаем порядковый номер выбранного элемента.
                    count = rv.getChildAdapterPosition(itemView);
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

                    return true;
                }
            });
        }
    }

    // Класс построения RecyclerView.
    public class RVAdapter extends RecyclerView.Adapter<SavedActivity.CountriesViewHolder>{

        private List<String> Countries, Places, Photos;
        private List<Integer> select;

        // Конструктор.
        RVAdapter(List<String> Countries, List <String> Places, List<String> Photos, List<Integer> select){
            this.Countries = Countries;
            this.Places = Places;
            this.Photos = Photos;
            this.select = select;
        }

        @Override
        public int getItemCount() {
            return Countries.size();
        }

        @Override
        public SavedActivity.CountriesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.places_card_view, viewGroup, false);
            SavedActivity.CountriesViewHolder pvh = new SavedActivity.CountriesViewHolder(v);
            return pvh;
        }


        // Передача данных в элементы CardView, установка слушателя нажатий.
        @Override
        public void onBindViewHolder(final SavedActivity.CountriesViewHolder personViewHolder, int i) {
            // Заполнение элементов RecycleView данными.
            personViewHolder.label.setText(Places.get(i));
            personViewHolder.label2.setText(Countries.get(i));
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "georgiai.ttf");
            personViewHolder.label.setTypeface(typeFace);
            personViewHolder.label2.setTypeface(typeFace);
            // Загрузка картинок при помощи библиотеки Universal Image Loader.
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(SavedActivity.this));
            // Скачиваем и отображаем.
            imageLoader.displayImage(Photos.get(i), personViewHolder.icon, options);
            // Установка выделения. Если - 0, не выделяем, если - 1, выделяем. Список постоянно меняется.
            if (select.get(i).equals(0)) {personViewHolder.cardlayout.setBackgroundResource(R.drawable.layout_toggle);}
            else if (select.get(i).equals(1)) {personViewHolder.cardlayout.setBackgroundResource(R.color.cardbackground);}

        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }
}