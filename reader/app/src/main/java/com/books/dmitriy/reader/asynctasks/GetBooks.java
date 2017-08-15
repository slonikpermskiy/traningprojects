package com.books.dmitriy.reader.asynctasks;

import android.os.AsyncTask;

import com.books.dmitriy.reader.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



// Класс параллельного потока (загрузка списка книг).
public class GetBooks extends AsyncTask<String, String, String> {

    ArrayList<String> Titles = new ArrayList();
    ArrayList<String> Authors = new ArrayList();
    ArrayList<String> Links = new ArrayList();
    JSONParser jsonParser = new JSONParser();
    private static final String url_sight1 = "http://www.edu-sam.com/restapiinterestingplaces/api/getbooks.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BOOKS = "books";
    private static final String TAG_TITLE = "Title";
    private static final String TAG_AUTHOR = "Author";
    private static final String TAG_LINK = "Link";

    public AsyncResponse delegate = null;

    public GetBooks(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }

    // Метод запускается до старта параллельного потока.
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        delegate.LoadingShower();
    }

    // Основной метод параллельного потока.
    protected String doInBackground(String... params) {

        int success;
        try {

            // Получение JSON.
            JSONObject json = null;
            json = jsonParser.makeHttpRequest(url_sight1, "GET", "");
            // Проверка на успешность запроса (возвращается из REST API).
            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                // Получение массивов с данными.
                JSONArray books = json.getJSONArray(TAG_BOOKS); // JSON Array
                for (int x=0; x!=books.length(); x++) {
                    JSONObject country = books.getJSONObject(x);
                    Titles.add(country.getString(TAG_AUTHOR));
                    Authors.add(country.getString(TAG_TITLE));
                    Links.add(country.getString(TAG_LINK));
                }
            }else{
                Titles.clear();
                Authors.clear();
                Links.clear();
                // Книги не найдены. Запуск метода завершения потока.
                cancel(true);
                if (isCancelled()) return null;
            }
        } catch (Exception e) {
            Titles.clear();
            Authors.clear();
            Links.clear();
            cancel(true);
            if (isCancelled()) return null;
        }
        return null;
    }

    // Метод выполняется после выполнения параллельного потока.
    protected void onPostExecute(String result) {
        delegate.GetBooksFinish(Titles, Authors, Links);
    }

    // Метод выполняется если произошла отмена выполнения основного метода.
    @Override
    protected void onCancelled() {
        super.onCancelled();
        delegate.GetBooksNotFinish();
    }
}