package com.books.dmitriy.reader.asynctasks;

import java.util.ArrayList;

public interface AsyncResponse {
    void GetBooksFinish(ArrayList<String> Titles, ArrayList<String> Authors, ArrayList<String> Links);
    void GetOneBookFinish(String title, String author, String key, String text);
    void GetBooksNotFinish();
    void LoadingShower();
}