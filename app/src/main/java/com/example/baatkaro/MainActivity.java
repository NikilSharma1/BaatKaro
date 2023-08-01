package com.example.baatkaro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.example.baatkaro.activities.Fields;
import com.example.baatkaro.activities.LoginActivity;
import com.example.baatkaro.activities.UsersListActivity;

public class MainActivity extends AppCompatActivity {
    private int FIVE_SECONDS = 1500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences=getSharedPreferences(Fields.PREFERENCES_NAME,MODE_PRIVATE);
        new Handler().postDelayed(() -> {
            if(sharedPreferences.getBoolean(Fields.SIGN_IN_STATUS,false)) {
                startActivity(new Intent(MainActivity.this, UsersListActivity.class));
                finish();
            }else{
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, FIVE_SECONDS);
    }
}