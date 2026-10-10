package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //connect the Email EditText to the id in the Layout file
        EditText emailField = findViewById(R.id.email);

        Intent intent = getIntent();

        emailField.setText(intent.getStringExtra("email"));

        Button goBackButton = findViewById(R.id.back);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // simply closes this activity and goes back LoginActivity
            }
        });
    }
}
