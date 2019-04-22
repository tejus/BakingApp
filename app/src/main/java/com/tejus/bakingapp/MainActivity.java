package com.tejus.bakingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.tejus.bakingapp.model.Recipe;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Recipe> recipes = Repository.getRecipes(this);
        mListView = findViewById(R.id.lv_main);
        mAdapter = new MainAdapter(this, recipes);
        mListView.setAdapter(mAdapter);
    }
}
