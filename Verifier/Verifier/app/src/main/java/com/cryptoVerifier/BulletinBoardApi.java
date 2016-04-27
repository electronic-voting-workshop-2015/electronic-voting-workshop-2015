package com.cryptoVerifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Naor on 01/02/2016.
 */

public class BulletinBoardApi {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String BASE_URL = "http://46.101.148.106:4567/";

    public static final String MIXNET_REQUEST_URL = BASE_URL + "retrieveProofsFile";
    public static final String COMPLAINT_URL = BASE_URL + "publishComplaint";
    public static final String BB_VOTES_URL = BASE_URL + "getBBVotes/-1";
    public static final String PUBLIC_KEY_URL = BASE_URL + "getPublicKey?party_id=";
    public static final boolean DEBUG = false;


//    public static void test(final Context context) {
//
//        String url = "http://ip.jsontest.com/?callback=showIP";
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//        Response response = null;
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) throws IOException {
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Toast.makeText(context, response.body().string(), Toast.LENGTH_SHORT).show();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//            }
//        });
//        //return response.body().string();
//
//    }


    public static void enqueueGetRequest(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(DEBUG ? "http://validate.jsontest.com/?json=%5BJSON-code-to-validate%5D" : url)
                .build();
        client.newCall(request).enqueue(callback);
        //return response.body().string();

    }

    public static void sendComplaint(String complaint) {
        String jsonBody = null;
        try {
            JSONObject comp = new JSONObject();
            comp.put("complaint_messege,", complaint);
            jsonBody = new JSONObject().put("content", comp).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, jsonBody);

        Request request = new Request.Builder()
                .url(COMPLAINT_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                //Log.d("", "");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Log.d("", "");
            }
        });
    }
}
