package com.example.wattson;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String title = getIntent().getStringExtra("SELECTED_TITLE");

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        List<String> contents = dbHelper.getContentsByTitle(title);

        TextView contentView = findViewById(R.id.content_view);
        StringBuilder allContents = new StringBuilder();
        for (String content : contents) {
            allContents.append(content).append("\n\n");
        }
        contentView.setText(allContents.toString());
    }
}
