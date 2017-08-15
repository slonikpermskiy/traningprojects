package com.interestingplacesapp.dmitriy.appip.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;


public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "myDB", null, 1);
    }

    public void onCreate(SQLiteDatabase db) {

        // Создаем БД с таблицей.
        db.execSQL("create table mysights ("
                + "country text,"
                + "place text,"
                + "htmltext text,"
                + "howmuch text,"
                + "howtoget text,"
                + "latitude text,"
                + "longitude text,"
                + "address text,"
                + "photo text,"
                + "photo1 text,"
                + "map text"
                + ");");

    }
    // Метод апгрейда БД (у нас не используется).
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // Метод добавления достопримечательности.
    public void savesight(ArrayList <String> sight) {
        // Создаем объект для данных.
        ContentValues cv = new ContentValues();
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        cv.put("country", sight.get(0));
        cv.put("place", sight.get(1));
        cv.put("htmltext", sight.get(2));
        cv.put("howmuch", sight.get(3));
        cv.put("howtoget", sight.get(4));
        cv.put("latitude", sight.get(5));
        cv.put("longitude", sight.get(6));
        cv.put("address", sight.get(7));
        cv.put("photo", sight.get(8));
        cv.put("photo1", sight.get(9));
        cv.put("map", sight.get(10));
        db.insert("mysights", null, cv);
    }


    // Проверка наличия месяца и года в БД.
    public int ifthereissight (String country, String place) {

        int x = 0;
        String countrydb = null;
        String placedb = null;

        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query("mysights", null, null, null, null, null, null);

        if (c.moveToFirst()) {
            // Определяем номера столбцов по имени в выборке.
            int Title = c.getColumnIndex("country");
            int Title2 = c.getColumnIndex("place");
            do     {
                // Назначаем строкам значения.
                countrydb = c.getString(Title);
                placedb = c.getString(Title2);
                // Если строки равны, то присваиваем x значение 1 и прерываем метод, иначе двигаемся на следующее поле и присваиваем строкам countrydb и placedb новые значения.
                if (place.equals(placedb) & country.equals(countrydb)) {
                    x=1;
                    break;
                }
                // Условие позволяет курсору двигаться до конца БД, пока значение не будет найдено.
            } while (c.moveToNext());
        }
        return x;
    }


    // Метод получения заголовков для построения меню (country).
    public ArrayList dataforcountries() {
        // Создаем список.
        ArrayList<String> list = new ArrayList();
        // подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mysights", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номер столбца.
            int TitleColIndex = c.getColumnIndex("country");
            do {
                // Получаем значения по номеру столбца.
                list.add(c.getString(TitleColIndex));
                // Переход на следующую строку, а если следующей нет (текущая - последняя), то выходим из цикла.
            } while (c.moveToNext());
        }
        // Возврат списка.
        return list;
    }

    // Метод получения заголовков для построения меню (place).
    public ArrayList dataforplaces() {
        // Создаем список.
        ArrayList<String> list = new ArrayList();
        // подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mysights", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номер столбца.
            int TitleColIndex = c.getColumnIndex("place");
            do {
                // Получаем значения по номеру столбца.
                list.add(c.getString(TitleColIndex));
                // Переход на следующую строку, а если следующей нет (текущая - последняя), то выходим из цикла.
            } while (c.moveToNext());
        }
        // Возврат списка.
        return list;
    }

    // Метод получения заголовков для построения меню (photo).
    public ArrayList dataforphotos() {
        // Создаем список.
        ArrayList<String> list = new ArrayList();
        // подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mysights", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номер столбца.
            int TitleColIndex = c.getColumnIndex("photo");
            do {
                // Получаем значения по номеру столбца.
                list.add(c.getString(TitleColIndex));
                // Переход на следующую строку, а если следующей нет (текущая - последняя), то выходим из цикла.
            } while (c.moveToNext());
        }
        // Возврат списка.
        return list;
    }


    // Метод удаления достопримечательности.
    public void deletesight(String country, String place) {
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("mysights", "country = ? and place = ?", new String[]{country, place});
    }


    // Метод получения данных по достопримечательности.
    public ArrayList dataforsight(String country, String place) {
        String countrydb = null;
        String placedb = null;
        // Создаем список.
        ArrayList<String> list = new ArrayList();
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mysights", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номера столбцов.
            int Title = c.getColumnIndex("country");
            int Title1 = c.getColumnIndex("place");
            int Title2 = c.getColumnIndex("htmltext");
            int Title3 = c.getColumnIndex("howmuch");
            int Title4 = c.getColumnIndex("howtoget");
            int Title5 = c.getColumnIndex("latitude");
            int Title6 = c.getColumnIndex("longitude");
            int Title7 = c.getColumnIndex("address");
            int Title8 = c.getColumnIndex("photo");
            int Title9 = c.getColumnIndex("photo1");
            int Title10 = c.getColumnIndex("map");

            // Выполнение цикла пока приняте в метод заголовки не будут равны зачениям из БД или до завершения БД.
            do     {
                // Назначаем строкам значения заголовков.
                countrydb = c.getString(Title);
                placedb = c.getString(Title1);
                // Если строки равны, то записываем значение, иначе двигаемся на следующее поле.
                if (country.equals(countrydb) & place.equals(placedb)) {
                    list.add(c.getString(Title));
                    list.add(c.getString(Title1));
                    list.add(c.getString(Title2));
                    list.add(c.getString(Title3));
                    list.add(c.getString(Title4));
                    list.add(c.getString(Title5));
                    list.add(c.getString(Title6));
                    list.add(c.getString(Title7));
                    list.add(c.getString(Title8));
                    list.add(c.getString(Title9));
                    list.add(c.getString(Title10));
                }
                // Условие позволяет курсору двигаться до конца БД, пока значение не будет найдено.
            } while (c.moveToNext());
        }
        // Возврат списка.
        return list;
    }

    // Метод очистки БД.
    public void deleteAll() {
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("mysights", null, null);
    }
}


