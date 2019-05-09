package com.tejus.bakingapp.data;

import android.util.Log;

import com.tejus.bakingapp.model.Recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {

    private static final String LOG_TAG = Repository.class.getSimpleName();

    private static final String BASE_URL = "https://d17h27t6h515a5.cloudfront.net/";
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static RetrofitEndpoints endpoints = retrofit.create(RetrofitEndpoints.class);

    public static List<Recipe> getRecipes() {
        Call<ArrayList<Recipe>> call = endpoints.getRecipesFromHttp();
        List<Recipe> result = new ArrayList<>();

        try {
            Response<ArrayList<Recipe>> response = call.execute();
            result = response.body();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Network call failed!");
            e.printStackTrace();
        }

        return result;
    }
}
