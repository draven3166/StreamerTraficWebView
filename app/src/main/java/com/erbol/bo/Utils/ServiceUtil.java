package com.erbol.bo.Utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceUtil {
    public static final String TAG = "ServiceUtil";

    public static String getDatajson(){
        HttpURLConnection httpConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = new StringBuilder();
        try {
            Log.e("URL", ConstantsUtil.URL_RADIO);
            URL url = new URL(ConstantsUtil.URL_RADIO);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                response.append(line);
            }
            bufferedReader.close();
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "GET error: " + Log.getStackTraceString(e));
            return null;
        }finally {
            if(httpConnection != null){
                httpConnection.disconnect();
            }
        }
    }

    public static String getDatajsonc(){
        HttpURLConnection httpConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = new StringBuilder();
        try {
            Log.e("URL", ConstantsUtil.URL_CONFLICT);
            URL url = new URL(ConstantsUtil.URL_CONFLICT);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                response.append(line);
            }
            //Log.d(TAG, "GET response code: " + String.valueOf(httpConnection.getResponseCode()));
            //Log.e(TAG, "JSON response: " + response.toString());
            bufferedReader.close();
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "GET error: " + Log.getStackTraceString(e));
            return null;
        }finally {
            if(httpConnection != null){
                httpConnection.disconnect();
            }
        }
    }

    public static String downloadUrl(String strUrl){
        HttpURLConnection httpConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(strUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                response.append(line);
            }
            bufferedReader.close();
            return response.toString();
        } catch (Exception e) {
            Log.e("Get error", Log.getStackTraceString(e));
            return null;
        }finally {
            if(httpConnection != null){
                httpConnection.disconnect();
            }
        }
    }

    public static StringBuilder readStreamNew(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb;
    }
}