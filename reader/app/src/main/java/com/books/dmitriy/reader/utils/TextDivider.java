package com.books.dmitriy.reader.utils;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;


public class TextDivider {

    int width, height, textsize;
    double div;
    String text, finish = "";
    ArrayList<String> pages = new ArrayList();


    // Конструктор.
    public TextDivider(Integer width, Integer height, Integer textsize, String text) {
        this.width = width;
        this.height = height;
        this.textsize = textsize;
        this.text = text;
        // Поправка по высоте текста (учет расстояний между строками).
        if (textsize == 28) {
            div = 1.25;
        } else if (textsize == 32) {
            div = 1.30;
        } else if (textsize == 36) {
            div = 1.35;
        } else if (textsize == 40) {
            div = 1.4;
        }
    }


    public String getpage(Integer page) {

        if(finish.equals("finish") && page >= pages.size()) {
        return finish;
        }  else if (page+1 > pages.size()) {
            getdividedtext();
            return pages.get(page);
        } else {
            return pages.get(page);
        }
    }

    // Метод деления текста на страницы.
    public void getdividedtext() {

        ArrayList<String> strings = new ArrayList();
        String prepare = "";
        int heightprepare = 0;


        while (true) {
            while (true) {

                int end = text.indexOf(" ", 0);
                if (end > 20 ) {
                    end = 10;}
                else if (end == 0) {
                    text = text.substring(1, text.length());
                    end = text.indexOf(" ", 0);
                }
                if (end != -1) {
                    prepare = prepare + text.substring(0, end + 1);
                    text = text.substring(end + 1, text.length());
                } else {
                    prepare = prepare + text;
                    text = "";
                    strings.add(prepare);
                    heightprepare = height + 500;
                    finish = "finish";
                    break;
                }

                Paint paint = new Paint();
                Rect bounds = new Rect();
                int text_height = 0;
                int text_width = 0;
                paint.setTypeface(Typeface.DEFAULT);
                paint.setTextSize(textsize);
                paint.getTextBounds(prepare, 0, prepare.length(), bounds);
                text_width = bounds.width();

                if (text_width > width) {
                    prepare = prepare.substring(0, prepare.length() - 1);
                    int end1 = prepare.lastIndexOf(" ");
                    if (end1 == -1) {end1 = 10;}
                    text = prepare.substring(end1 + 1, prepare.length()) + " " + text;
                    prepare = prepare.substring(0, end1) + " ";
                    strings.add(prepare);
                    paint.getTextBounds(prepare, 0, prepare.length(), bounds);
                    text_height = bounds.height();
                    heightprepare = (int) (heightprepare + text_height * div);
                    prepare = "";
                    break;

                }
            }
            if (heightprepare >= height-height*0.05) {
                heightprepare = 0;
                break;
                }
        }
        String y = "";
        for (int x = 0; x != strings.size(); x++) {
            y = y + strings.get(x);
        }
        int end2 = y.lastIndexOf(".");
        if (end2 == -1 | end2 < 800) {
            end2 = y.length() - 50;
        }
        if (y.length() < 800) {
            end2 = y.length()-1;
        }
        text = y.substring(end2+1, y.length()) + " " + text;
        y = y.substring(0, end2+1);
        pages.add(y);
        strings.clear();
    }
}
