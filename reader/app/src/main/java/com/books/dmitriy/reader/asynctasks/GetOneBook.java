package com.books.dmitriy.reader.asynctasks;

import android.os.AsyncTask;

import com.books.dmitriy.reader.utils.EncodeDecode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

// Класс параллельного потока (загрузка книги).
public class GetOneBook extends AsyncTask<String, String, String> {

    String title, author, link, htmltext="";

    // Переменная Jsoup для скачивания html.
    org.jsoup.nodes.Document doc;
    public AsyncResponse delegate = null;

    public GetOneBook(AsyncResponse asyncResponse) {
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


        try {
            title = params[0];
            author = params [1];
            link = params [2];

            // Сброс переменной, для дальнейшего использования в условии (см. ниже), в целях проверки корректности ссылки (см. ниже).
            doc=null;
            // Получение html-кода с помощью библиотеки Jsoup. Необходимо скачать ее и положить в папку: app/libs.
            doc = Jsoup.connect(link).maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .timeout(5000)
                    .get();
        // Парсит только https://www.e-reading.club по классу "bookbody", т.к. просто body содержит в т.ч. код (скрипты и т.д.)
        Elements elements = doc.getElementsByClass("bookbody");
        if (elements.size() != 0){
                for (Element el : elements) {
                    htmltext = htmltext + el.toString();
                }
            } else {
                htmltext = doc.body().toString();}

        } catch (Exception e) {
            doc=null;
            cancel(true);
            if (isCancelled()) return null;
        }
        return null;
    }

    // Метод выполняется после выполнения параллельного потока.
    protected void onPostExecute(String result) {
        // Шифруем.
        EncodeDecode ed = new EncodeDecode();
        ArrayList<String> okresult = ed.encode(htmltext);
        // Сохраняем.
        delegate.GetOneBookFinish(title, author, okresult.get(0), okresult.get(1));
    }

    // Метод выполняется если произошла отмена выполнения основного метода.
    @Override
    protected void onCancelled() {
        super.onCancelled();
       delegate.GetBooksNotFinish();
    }
}

