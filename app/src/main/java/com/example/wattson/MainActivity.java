package com.example.wattson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Show RecordFragment when app starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RecordFragment()).commit();
        }
    }

    // BottomNavigationView listener
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.navigation_record) {
                        selectedFragment = new RecordFragment();
                    } else if (item.getItemId() == R.id.navigation_history) {
                        selectedFragment = new HistoryFragment();
                    } else if (item.getItemId() == R.id.navigation_library) {
                        selectedFragment = new LibraryFragment();
//                    } else if (item.getItemId() == R.id.navigation_account) {
//                        selectedFragment = new AccountFragment();
                    }

                    // Switch to selected fragment
                    if (selectedFragment != null) {
                        switchFragment(selectedFragment);
                    }

                    return true;
                }
            };

    private void switchFragment(Fragment fragment) {
        // Check if RecordFragment is currently active
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof RecordFragment) {
            ((RecordFragment) currentFragment).stopAndReset();
        }

        // Switch to selected fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}

