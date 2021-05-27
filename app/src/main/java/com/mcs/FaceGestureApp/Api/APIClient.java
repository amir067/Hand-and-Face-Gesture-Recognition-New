package com.mcs.FaceGestureApp.Api;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class APIClient {

    private static String API_KEY="";
    private static String API_URL="";

    public APIClient(String API_KEY, String API_URL) {
        APIClient.API_KEY = API_KEY;
        APIClient.API_URL = API_URL;
    }
    public static IamAuthenticator authenticator = null;
    public static IamAuthenticator getClient() {
        if (authenticator == null) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpBulder = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor);
            OkHttpClient client = httpBulder.build();
            authenticator = new IamAuthenticator(API_KEY);
            TextToSpeech textToSpeech = new TextToSpeech(authenticator);
            textToSpeech.setServiceUrl(API_URL);
        }
        return authenticator;
    }

}



