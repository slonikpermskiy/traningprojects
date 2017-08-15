package com.interestingplacesapp.dmitriy.appip.helpers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class JSONParser {

    HttpURLConnection c;
    BufferedReader br;
    byte[] data = null;
    InputStream is = null;
    JSONObject jObj = null;
    String json = "";

    // constructor
    public JSONParser() {
    }

    // HTTP POST or GET mehtod
    public JSONObject makeHttpRequest(String url, String method, String parameters) {

        jObj = null;
        json = "";

        // Making HTTP request
        try {
            if (method == "GET") {

                url = url + "?" + parameters;
                url = url.replaceAll(" ","%20");
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(30000);
                c.connect();}

            else if (method == "POST") {

                URL url3 = new URL(url);
                c = (HttpURLConnection) url3.openConnection();
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setDoInput(true);
                c.setConnectTimeout(10000);
                c.setReadTimeout(30000);
                c.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
                OutputStream os = c.getOutputStream();
                data = parameters.getBytes("UTF-8");
                os.write(data);
                data = null;
                c.connect();
                int responseCode= c.getResponseCode();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                is = c.getInputStream();
                byte[] buffer = new byte[8192]; // Такого вот размера буфер
                }

        } catch (Exception e) {
            jObj = null;
            json = "";
            Log.e("Error", e.toString());
        }

        try {
            br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            json = sb.toString();
        } catch (Exception e) {
            jObj = null;
            json = "";
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            jObj = null;
            json = "";
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }
}


