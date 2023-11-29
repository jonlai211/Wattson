package com.example.wattson;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Bottom Navigation
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_record) {
                    startActivity(new Intent(LibraryActivity.this, RecordActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_history) {
                    startActivity(new Intent(LibraryActivity.this, HistoryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_library) {
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    startActivity(new Intent(LibraryActivity.this, AccountActivity.class));
                    return true;
                }
                return false;
            }
        });
    }
}
