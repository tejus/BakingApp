package com.tejus.bakingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.tejus.bakingapp.model.Recipe;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView textView = findViewById(R.id.tv_detail);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("position")) {
            int position = bundle.getInt("position");
            Recipe recipe = Repository.getRecipe(this, position);
            textView.setText("Clicked item " + String.valueOf(position) + ": " + recipe.getName());
        } else {
            Toast.makeText(this, "Invalid recipe!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
