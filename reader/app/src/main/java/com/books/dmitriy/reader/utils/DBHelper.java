package com.books.dmitriy.reader.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "myDB", null, 1);
    }

    public void onCreate(SQLiteDatabase db) {

        // Создаем БД с таблицей.
        db.execSQL("create table mybooks ("
                + "title text,"
                + "author text,"
                + "text text,"
                + "key text,"
                + "date text,"
                + "favorite text"
                + ");");

    }
    // Метод апгрейда БД (у нас не используется).
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // Добавление книги.
    public void savebook(String title, String author, String key, String text) {
        // Создаем объект для данных.
        ContentValues cv = new ContentValues();
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        cv.put("title", title);
        cv.put("author", author);
        cv.put("text", text);
        cv.put("key", key);
        cv.put("date", dateString);
        cv.put("favorite", "no");
        db.insert("mybooks", null, cv);
    }


    // Проверка наличия книги.
    public int ifthereisbook (String title, String author) {

        int x = 0;
        String titledb = null;
        String authordb = null;

        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query("mybooks", null, null, null, null, null, null);

        if (c.moveToFirst()) {
            // Определяем номера столбцов по имени в выборке.
            int Title = c.getColumnIndex("title");
            int Title2 = c.getColumnIndex("author");
            do     {
                // Назначаем строкам значения.
                titledb = c.getString(Title);
                authordb = c.getString(Title2);
                // Если строки равны, то присваиваем x значение 1 и прерываем метод, иначе двигаемся на следующее поле и присваиваем строкам countrydb и placedb новые значения.
                if (title.equals(titledb) & author.equals(authordb)) {
                    x=1;
                    break;
                }
                // Условие позволяет курсору двигаться до конца БД, пока значение не будет найдено.
            } while (c.moveToNext());
        }
        return x;
    }


    // Метод получения заголовков для построения списка.
    public ArrayList<String>[] dataforbooks() {
        // Создаем список.
        ArrayList<String> list = new ArrayList();
        ArrayList<String> list1 = new ArrayList();
        ArrayList<String> list2 = new ArrayList();
        ArrayList<String> list3 = new ArrayList();
        ArrayList<String>[] request = new ArrayList[4];
        // подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mybooks", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номер столбца.
            int TitleColIndex = c.getColumnIndex("title");
            int TitleColIndex1 = c.getColumnIndex("author");
            int TitleColIndex2 = c.getColumnIndex("date");
            int TitleColIndex3 = c.getColumnIndex("favorite");
            do {
                // Получаем значения по номеру столбца.
                list.add(c.getString(TitleColIndex));
                list1.add(c.getString(TitleColIndex1));
                list2.add(c.getString(TitleColIndex2));
                list3.add(c.getString(TitleColIndex3));
                // Переход на следующую строку, а если следующей нет (текущая - последняя), то выходим из цикла.
            } while (c.moveToNext());
        }
        request[0] = list;
        request[1] = list1;
        request[2] = list2;
        request[3] = list3;
        // Возврат списка.
        return request;
    }


    // Метод получения заголовков для построения списка.
    public ArrayList<String>[] likebooks() {
        // Создаем список.
        ArrayList<String> list = new ArrayList();
        ArrayList<String> list1 = new ArrayList();
        ArrayList<String> list2 = new ArrayList();
        ArrayList<String> list3 = new ArrayList();
        ArrayList<String>[] request = new ArrayList[4];
        String favoritedb = null;
        // подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mybooks", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номер столбца.
            int TitleColIndex = c.getColumnIndex("title");
            int TitleColIndex1 = c.getColumnIndex("author");
            int TitleColIndex2 = c.getColumnIndex("date");
            int TitleColIndex3 = c.getColumnIndex("favorite");
            do {
                favoritedb = c.getString(TitleColIndex3);
                if (favoritedb.equals("yes")) {
                // Получаем значения по номеру столбца.
                list.add(c.getString(TitleColIndex));
                list1.add(c.getString(TitleColIndex1));
                list2.add(c.getString(TitleColIndex2));
                list3.add(c.getString(TitleColIndex3)); }
                // Переход на следующую строку, а если следующей нет (текущая - последняя), то выходим из цикла.
            } while (c.moveToNext());
        }
        request[0] = list;
        request[1] = list1;
        request[2] = list2;
        request[3] = list3;
        // Возврат списка.
        return request;
    }



    // Метод удаления книги.
    public void deletebook(String title, String author) {
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("mybooks", "title = ? and author = ?", new String[]{title, author});
    }


    // Метод получения текста.
    public String dataforonebook(String title, String author) {
        String titledb = null;
        String authordb = null;
        String text = null;
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mybooks", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номера столбцов.
            int Title = c.getColumnIndex("title");
            int Title1 = c.getColumnIndex("author");
            int Title12 = c.getColumnIndex("text");


            // Выполнение цикла пока приняте в метод заголовки не будут равны зачениям из БД или до завершения БД.
            do     {
                // Назначаем строкам значения заголовков.
                titledb = c.getString(Title);
                authordb = c.getString(Title1);
                // Если строки равны, то записываем значение, иначе двигаемся на следующее поле.
                if (title.equals(titledb) & author.equals(authordb)) {
                    text=c.getString(Title12);
                }
                // Условие позволяет курсору двигаться до конца БД, пока значение не будет найдено.
            } while (c.moveToNext());
        }
        // Возврат списка.
        return text;
    }

    // Метод получения ключа.
    public String keyforbook(String title, String author) {
        String titledb = null;
        String authordb = null;
        String text = null;
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mybooks", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номера столбцов.
            int Title = c.getColumnIndex("title");
            int Title1 = c.getColumnIndex("author");
            int Title12 = c.getColumnIndex("key");


            // Выполнение цикла пока приняте в метод заголовки не будут равны зачениям из БД или до завершения БД.
            do     {
                // Назначаем строкам значения заголовков.
                titledb = c.getString(Title);
                authordb = c.getString(Title1);
                // Если строки равны, то записываем значение, иначе двигаемся на следующее поле.
                if (title.equals(titledb) & author.equals(authordb)) {
                    text=c.getString(Title12);
                }
                // Условие позволяет курсору двигаться до конца БД, пока значение не будет найдено.
            } while (c.moveToNext());
        }
        // Возврат списка.
        return text;
    }


    // Метод установки звездочки.
    public void Like(String title, String author) {
        String titledb = null;
        String authordb = null;
        String text = null;
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        // Создаем курсор.
        Cursor c = db.query("mybooks", null, null, null, null, null, null);
        // Если курсору есть куда двигаться.
        if (c.moveToFirst()) {
            // Определяем номера столбцов.
            int Title = c.getColumnIndex("title");
            int Title1 = c.getColumnIndex("author");
            int Title12 = c.getColumnIndex("favorite");


            // Выполнение цикла пока приняте в метод заголовки не будут равны зачениям из БД или до завершения БД.
            do     {
                // Назначаем строкам значения заголовков.
                titledb = c.getString(Title);
                authordb = c.getString(Title1);
                // Если строки равны, то записываем значение, иначе двигаемся на следующее поле.
                if (title.equals(titledb) & author.equals(authordb)) {
                    text=c.getString(Title12);
                }
                // Условие позволяет курсору двигаться до конца БД, пока значение не будет найдено.
            } while (c.moveToNext());
        }

        ContentValues cv = new ContentValues();
        if (text.equals("no")) {
            cv.put("favorite", "yes");
        } else {
            cv.put("favorite", "no");
        }

        db.update("mybooks", cv, "title = ? and author = ?", new String[]{title, author});
    }



    // Метод очистки БД.
    public void deleteAll() {
        // Подключаемся к БД.
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("mysights", null, null);
    }
}



