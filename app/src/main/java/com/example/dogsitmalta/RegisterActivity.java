package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //todo add all fields to sqllite db

        EditText nameField = findViewById(R.id.name);
        EditText surnameField = findViewById(R.id.surname);
        EditText emailField = findViewById(R.id.logemail);
        EditText passwordField = findViewById(R.id.logpass);
        Button   btnRegister = findViewById(R.id.regbut);

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,
                        MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
